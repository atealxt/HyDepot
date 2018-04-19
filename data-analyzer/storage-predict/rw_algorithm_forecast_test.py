import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re
import multiprocessing
import sys
from rw_predict_linreg import *
from rw_predict_arima import *
from rw_classifier_dtw import *

def execute_predict_linreg(x):
    obj_linreg = predict_linreg()
    obj_linreg.predict(x, stopIfFound=True, predictStartDays=range(8, 30))
    return obj_linreg

def execute_predict_arima(x):
    obj_arima = predict_arima()
    obj_arima.predict(x, stopIfFound=True, predictStartDays=range(8, 30))
    return obj_arima

def calc(q_in, calc_result):
    result = []
    while True:
        item = q_in.get()
        if item is None:
            break
        x, idx, len = item[0], item[1], item[2]
        print("data" + str(idx + 1) + "/" + str(len) + ": " + str(x))
        x = x.reshape(-1,1)
        pred = x[x.size - 1]
        x = x[0:x.size - 1]

        obj_linreg = execute_predict_linreg(x)
        saving_linreg = obj_linreg.predictSaving
        obj_arima = execute_predict_arima(x)
        saving_arima = obj_arima.predictSaving
        if pred == 3:
            saving_optimized = 0
        elif pred == 1:
            saving_optimized = saving_linreg
        else:
            saving_optimized = saving_arima

        price = obj_linreg.price1Obs # same as arima's
        calc_result.append([price, saving_linreg, saving_arima, saving_optimized, item])

        q_in.task_done()

def sampleByType(rwData, trainRate = 0.8, dayRange = 30):

    map = {}
    for d in rwData:
        if d[len(d) - 1] not in map:
            map[d[len(d) - 1]] = []
        map[d[len(d) - 1]].append(d)
    train,test,test_complete = [], [], []
    for key, value in map.items():
        train_size = int(len(value) * trainRate)
        for v in value[0:train_size]:
            train.append(np.append(v[0:dayRange], v[-1:]))
        for v in value[train_size:]:
            test.append(np.append(v[0:dayRange], v[-1:]))
            test_complete.append(v)
    train,test,test_complete = np.array(train), np.array(test), np.array(test_complete)
    print("train rate: " + str(trainRate) + ", day range: " + str(dayRange) + ", train size: " + str(len(train)) + ", test size: " + str(len(test)))
    return train, test, test_complete

if __name__ == "__main__":
    
    print("get data from db")

    dbname = "project_m"
    host = "127.0.0.1"
    username = "root"
    password = "root"

    rw = []
    cursor = None
    cnx = None
    try:
        cnx = pymysql.connect(user=username, password=password, host=host, database=dbname)
        cursor = cnx.cursor()
        cursor.execute('SELECT rw_detail, type2 FROM logrw t where t.type is not null ')
        rows = cursor.fetchall()
        for row in rows:
            var = [int(x) for x in row[0].split(',')]
            var.append(row[1])
            rw.append(var)
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()

    rwData = np.array(rw)

    # predict unknown style data then calc price saving
    train, test, test_complete = sampleByType(rwData, 0.6, 6)
    classifier = ts_classifier(False)
    classifier.predict(train, test, 4, False)
    for idx, t in enumerate(test_complete):
        t[t.size - 1] = classifier.preds[idx]

    q_in = multiprocessing.JoinableQueue()
    for idx, item in enumerate(test_complete):
        q_in.put([item, idx, len(test)])

    manager = multiprocessing.Manager()
    calc_result = manager.list()
    processes = 2
    for i in range(processes):
        c = multiprocessing.Process(target=calc, args=(q_in, calc_result))
        c.daemon = True
        c.start()
    q_in.join()

    originalPrice = 0
    predictSaving_linreg = 0
    predictSaving_arima = 0
    predictSaving_optimized = 0
    for result in calc_result:
        originalPrice += result[0]
        predictSaving_linreg += result[1]
        predictSaving_arima += result[2]
        predictSaving_optimized += result[3]
        print("Item " + str(result[4]))

    print("Original Price: %.6f" % originalPrice)
    print("Predict Saving with Linear Regression: %.6f" % predictSaving_linreg)
    print("Predict Saving with ARIMA: %.6f" % predictSaving_arima)
    print("Optimized Predict Saving with hybrid linear&arima: %.6f" % predictSaving_optimized)