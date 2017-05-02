from chatbot import chatbot

class RNNPredictor:
    _model = None
    _response = None
    _args = None

    def __init__(self, model_path):
        self._model = chatbot.Chatbot(model_path)
        self._model.start([])
        self._response = "I didn't understand your question ?"

    def predict(self, context):
        answer = self._model.singlePredict(context)
        if not answer:
            print('Warning: sentence too long, sorry. Maybe try a simpler sentence.')

        reponse = self._model.textData.sequence2str(answer, clean=True)
        return reponse

if __name__ == '__main__':
    rnn = RNNPredictor('/Users/Mouloud/Documents/Xebia/LeMoisDeLaData/2017/nlp/chatbotty/model/cornell')
    rnn.predict("Hi there")