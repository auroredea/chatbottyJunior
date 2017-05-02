#!flask/bin/python
from flask import Flask, request
import sys, os
import datetime
from TFIDFPredictor import TFIDFPredictor
from RNNPredictor import RNNPredictor
import argparse


app = Flask(__name__)

@app.route('/rnn', methods=['POST'])
def rnn():
    requestBody = request.json

    context = requestBody["context"]
    # question = requestBody["question"]
    print("rnn is called ")
    questionSeq = []  # Will be contain the question as seen by the encoder
    answer = rnnmodel.predict(context)

    return answer

@app.route('/tfidf', methods=['POST'])
def tfidf():
    requestBody = request.json

    context = requestBody["context"]
    # question = requestBody["question"]
    before = datetime.datetime.now()
    response = tfidfmodel.predict(context)
    after = datetime.datetime.now()
    delta = after - before
    print("Response time :" + str(delta))
    return response

if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    # Global options
    globalArgs = parser.add_argument_group('Global options')
    globalArgs.add_argument('--data-path', metavar='PATH', required=True,
                            help='The path to ubuntu train dataset')
    globalArgs.add_argument('--project-root-dir', metavar='PATH', required=True,
                            help='The path to the project root directory (/...../chatbotty)')
    globalArgs.add_argument('--model-name', metavar='PATH', required=True,
                            help='the model to use, cornell or ubuntu')
    args = parser.parse_args(sys.argv[1:])

    data_path = args.data_path
    kmeans_path = os.path.join(args.project_root_dir, "model", "kmeans_pipeline")
    rnn_path = os.path.join(args.project_root_dir, "model", args.model_name)

    print("Data Path is :" + data_path)
    print("Loading Data...")
    # TFIDF predictor
    tfidfmodel = TFIDFPredictor(data_path)
    print("Loading Data succeeded")
    print("Training TFIDF Model...")
    tfidfmodel.train(kmeans_path)
    print("Training TFIDF Model succeeded")

    print("Loading RNN Model...")
    rnnmodel = RNNPredictor(rnn_path)
    print("Loading RNN Model succeeded")
    app.run(debug=False, use_reloader=False)
