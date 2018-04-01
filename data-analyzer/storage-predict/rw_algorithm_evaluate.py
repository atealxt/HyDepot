import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re
from rw_classifier_dtw import *
from rw_predict_linreg import *
from rw_predict_arima import *
import multiprocessing

def sampleByType(rwData, trainRate = 0.8, dayRange = 30):

    map = {}
    for d in rwData:
        if d[len(d) - 1] not in map:
            map[d[len(d) - 1]] = []
        map[d[len(d) - 1]].append(d)
    train,test,test_original = [], [], []
    for key, value in map.items():
        train_size = int(len(value) * trainRate)
        for v in value[0:train_size]:
            train.append(np.append(v[0:dayRange], v[-1:]))
        for v in value[train_size:]:
            test.append(np.append(v[0:dayRange], v[-1:]))
            test_original.append(v[0:len(v)-1])
    train,test = np.array(train), np.array(test)
    print("train rate: " + str(trainRate) + ", day range: " + str(dayRange) + ", train size: " + str(len(train)) + ", test size: " + str(len(test)))
    return train, test, test_original

def execute_predict_linreg(x):
    obj_linreg = predict_linreg()
    obj_linreg.predict(x, stopIfFound=True, predictStartDays=range(7, 30))
    return obj_linreg

def execute_predict_arima(x):
    obj_arima = predict_arima()
    obj_arima.predict(x, stopIfFound=True, predictStartDays=range(7, 30))
    return obj_arima

def calc(q_in, calc_result):
    result = []
    while True:
        item = q_in.get()
        if item is None:
            break
        x, idx, len, pred = item[0], item[1], item[2], item[3]
        print("data" + str(idx + 1) + "/" + str(len) + ": " + str(x) + ", pred: " + str(pred))
        x = x.reshape(-1,1)

        obj_linreg = execute_predict_linreg(x)
        saving_linreg = obj_linreg.predictSaving
        obj_arima = execute_predict_arima(x)
        saving_arima = obj_arima.predictSaving
        
        price = obj_arima.price1Obs # same as linreg's

        if pred == 4:
            saving_optimized = 0
        elif pred == 2:
            saving_optimized = obj_arima.predictSaving
        else:
            saving_optimized = obj_linreg.predictSaving

        calc_result.append([price, saving_linreg, saving_arima, saving_optimized])
        q_in.task_done()

if __name__ == "__main__":
    
    print("get data from db")

    dbname = "project_m"
    host = "127.0.0.1"
    username = "root"
    password = "root"

    dataSize = 150
    rw = []
    cursor = None
    cnx = None
    try:
        cnx = pymysql.connect(user=username, password=password, host=host, database=dbname)
        cursor = cnx.cursor()
        cursor.execute('SELECT rw_detail, type FROM logrw')
        rows = cursor.fetchmany(dataSize)
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

    print("classifying")
    
    classifier = ts_classifier(False)
    trainRate = 0.95
    dayRange = 6
    train, test, test_original = sampleByType(rwData, trainRate, dayRange)
    classifier.predict(train, test, 4, False)
    
    report = classifier.performance(test[:,-1])
    print(report)

    q_in = multiprocessing.JoinableQueue()
    for idx, item in enumerate(classifier.preds):
        x = test_original[idx]
        q_in.put([x, idx, len(test), item])

    manager = multiprocessing.Manager()
    calc_result = manager.list()
    c1 = multiprocessing.Process(target=calc, args=(q_in, calc_result))
    c1.daemon = True
    c2 = multiprocessing.Process(target=calc, args=(q_in, calc_result))
    c2.daemon = True
    c3 = multiprocessing.Process(target=calc, args=(q_in, calc_result))
    c3.daemon = True
    c1.start()
    c2.start()
    c3.start()

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

    print("Original Price: %.6f" % originalPrice)
    print("Predict Saving with ARIMA: %.6f" % predictSaving_arima)
    print("Predict Saving with Linear Regression: %.6f" % predictSaving_linreg)
    print("Optimized Predict Saving: %.6f" % predictSaving_optimized)