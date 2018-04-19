import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re
import multiprocessing
import sys
from rw_classifier_dtw import *
from rw_predict_linreg import *
from rw_predict_arima import *

def execute_predict_linreg(x):
    obj_linreg = predict_linreg()
    obj_linreg.predict(x, stopIfFound=True, predictStartDays=range(8, 30), progress=False)
    return obj_linreg

def execute_predict_arima(x):
    obj_arima = predict_arima()
    obj_arima.predict(x, stopIfFound=True, predictStartDays=range(8, 30), progress=False)
    return obj_arima

def calc(q_in, calc_result):
    result = []
    while True:
        item = q_in.get()
        if item is None:
            break
        x, idx, len = item[0], item[1], item[2]
        print("data" + str(idx) + "/" + str(len))
        x = x.reshape(-1,1)

        obj_linreg = execute_predict_linreg(x)
        saving_linreg = obj_linreg.predictSaving
        obj_arima = execute_predict_arima(x)
        saving_arima = obj_arima.predictSaving
        if obj_arima.predictSaving > obj_linreg.predictSaving:
            print("data" + str(idx) + " arima beats linear regression")
        elif obj_arima.predictSaving < obj_linreg.predictSaving:
            print("data" + str(idx) + " linear regression beats arima")
        else:
            print("data" + str(idx) + " duce")
        q_in.task_done()

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
        cursor.execute('SELECT rw_detail, type FROM logrw where id between 151 and 500 order by id asc')
        rows = cursor.fetchall()
        for row in rows:
            var = [int(x) for x in row[0].split(',')]
            rw.append(var)
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()

    rwData = np.array(rw)

    q_in = multiprocessing.JoinableQueue()
    for idx, item in enumerate(rwData):
        q_in.put([item, idx + 151, len(rwData)])

    manager = multiprocessing.Manager()
    calc_result = manager.list()
    processes = 2
    for i in range(processes):
        c = multiprocessing.Process(target=calc, args=(q_in, calc_result))
        c.daemon = True
        c.start()
    q_in.join()

