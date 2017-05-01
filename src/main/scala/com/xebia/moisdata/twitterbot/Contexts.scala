package com.xebia.moisdata.twitterbot

import com.xebia.moisdata.twitterbot.StartChatBot.log

import scala.collection.mutable

object Contexts {

  lazy val contextQueue: mutable.Stack[(String, String)] = new mutable.Stack()

  def toContext(user: String): Option[String] = {
    if(contextQueue.isEmpty) None

    val userContextQueue = contextQueue
      .filter(context => context._1 == user)
      .map(context => context._2)

    if(userContextQueue.isEmpty) None
    else userContextQueue
          .take(10)
          .reverse
          .reduceRightOption((a, b) => s"$a $b")
  }

  def add(user: String, context: String): Unit = {
    log.debug(s"adding question $context for $user")

    contextQueue
      .push((user, context))
  }

}