package com.xebia.moisdata.twitterbot

import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.typesafe.config.ConfigFactory
import dispatch._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

object StartChatBot extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val log = LoggerFactory.getLogger(getClass)

  lazy val pythonHostTfidf = url("http://127.0.0.1:5000/tfidf")
  lazy val pythonHostRNN = url("http://127.0.0.1:5000/rnn")

  val configFactory = ConfigFactory.load()
  val TOKEN = configFactory.getString("twitter.consumer.key")

  log.info(s"TOKEN: $TOKEN")

  val client = TwitterStreamingClient()
  val answerClient = TwitterRestClient()

  val chatbotName = "chatbotty"
  val hashTag = "lemoisdeladata"

  val stream = client.filterStatuses(tracks = Seq(chatbotName))(answerToTweet)

  private def answerToTweet: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet =>
      val messageJson = generateContextAndMessage(tweet).toJson
      val userName = getTweetUserArobase(tweet,"")

      log.info(s"performing request to ${pythonHostTfidf.toRequest.getUrl} with JSON body ${Json.stringify(messageJson)}")

      val reqRNN = Http(pythonHostRNN
        .addHeader("Content-Type", "application/json")
        .setBodyEncoding("UTF-8")
        .setBody(Json.stringify(messageJson))
        .POST
      )

      reqRNN.map { response =>
        val cleanedAnswer = cleanAnswer(response.getResponseBody)

        log.info(s"chatbot answering $cleanedAnswer")
        answerClient.createTweet(status = s"@$userName ".concat(cleanedAnswer), in_reply_to_status_id = Some(tweet.id))
      }
  }

  private def generateContextAndMessage(tweet: Tweet): Message = {
    val question = tweet.text.replace(s"@$chatbotName", "").replace(s"#$hashTag", "")
    val userName = getTweetUserArobase(tweet)
    Contexts.add(userName, question)

    val context = Contexts.toContext(userName).getOrElse(question)
    Message(context, question)
  }

  private def getTweetUserArobase(tweet: Tweet, default: String = "UNKNOWN"): String = {
    tweet.user match {
      case Some(user) => user.screen_name
      case None => default
    }
  }

  private def cleanAnswer(response: String): String = {
    response.replace("__eou__", "").replace("__eot__", "").concat(s" #$hashTag")
  }
}

