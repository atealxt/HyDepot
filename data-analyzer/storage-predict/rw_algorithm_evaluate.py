import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re
from rw_predict_linreg import *
from rw_predict_arima import *
import multiprocessing

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
        if pred == 4:
            saving_optimized = 0
        elif pred == 2:
            saving_optimized = obj_arima.predictSaving
        else:
            saving_optimized = obj_linreg.predictSaving

        price = obj_arima.price1Obs # same as linreg's
#         if obj_arima.predictSaving > obj_linreg.predictSaving:
#             calc_result.append([price, saving_linreg, saving_arima, saving_optimized, item])
        calc_result.append([price, saving_linreg, saving_arima, saving_optimized, item])

        q_in.task_done()

if __name__ == "__main__":
    
    print("get data from db")

    dbname = "project_m"
    host = "127.0.0.1"
    username = "root"
    password = "root"

    dataSize = 2
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

    q_in = multiprocessing.JoinableQueue()
    for idx, item in enumerate(rwData):
        q_in.put([item, idx, dataSize])

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
    print("Predict Saving with ARIMA: %.6f" % predictSaving_arima)
    print("Predict Saving with Linear Regression: %.6f" % predictSaving_linreg)
    print("Optimized Saving: %.6f" % predictSaving_optimized)