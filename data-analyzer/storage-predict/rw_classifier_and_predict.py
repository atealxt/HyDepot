import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re
from rw_classifier_dtw import *
from rw_predict_linreg import *
from rw_predict_arima import *

def sampleByRowId(rwData, rowIds, dayRange):

    train,test = [], []
    for idx, value in enumerate(rwData):
        if (idx + 1) in rowIds:
            test.append(np.append(value[0:dayRange], value[-1:]))
        else:            
            train.append(np.append(value[0:dayRange], value[-1:]))
    train,test = np.array(train), np.array(test)
    print("test rows: " + str(test) + ", day range: " + str(dayRange))
    return train, test

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
    rowIds = [5]
    train, test = sampleByRowId(rwData, rowIds, 8)
    classifier.predict(train, test, 4, False)
    print('classify result: ' + str(classifier.preds))

    for rId in rowIds:
        x = rwData[rId - 1]
        x= x[0:(len(x)-1)]
#         print(x)
        x = x.reshape(-1,1)
        print("try linear regression...")
        obj_linreg = predict_linreg()
        obj_linreg.predict(x, stopIfFound=True)
        print("try arima...")
        obj_arima = predict_arima()
        obj_arima.predict(x, stopIfFound=True)

        if obj_arima.predictSaving > obj_linreg.predictSaving:
            print("arima beats linear regression")
        elif obj_arima.predictSaving < obj_linreg.predictSaving:
            print("linear regression beats arima")
        else:
            print("duce")