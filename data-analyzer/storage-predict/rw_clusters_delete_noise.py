import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re
from rw_predict_linreg import *
from rw_predict_arima import *
import multiprocessing
from decimal import *

if __name__ == "__main__":
    
    print("get data from db")

    dbname = "project_m"
    host = "127.0.0.1"
    username = "root"
    password = "root"

    dict_km_score = {}
    sql_cluster_score = """
        select km3.km3, km3.cnt_km3, km3.cnt_type2, km3.cnt_km3 / km3.cnt_type2 as Pn3, kmall.kmall as cnt_all, km3.cnt_km3 / kmall.kmall as Qn3
         from (
        select km3, count(*) as cnt_km3, 
        (select count(km3) from logrw tt where tt.`type` = 2) as cnt_type2
         from logrw t where t.`type` = 2 group by t.km3
        ) km3
         , (select tt.km3, count(km3) as kmall from logrw tt where km3 is not null group by tt.km3) kmall
         where km3.km3 = kmall.km3
                        """
    try:
        cnx = pymysql.connect(user=username, password=password, host=host, database=dbname)
        cursor = cnx.cursor()
        for km in range(3, 11):
#             print("km" + str(km))
            sql = sql_cluster_score.replace("km3", "km" + str(km))
            cursor.execute(sql)
            rows = cursor.fetchall()
            kms = {}
            for row in rows:
#                 print(row)
                kms[row[0]] = [row[1], row[2], row[3], row[4], row[5]]
            dict_km_score["km" + str(km)] = kms
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()
#     print(dict_km_score)

    # for loop all example data, to see lines have low score, which means noise data!
    threshold = 0.1 
    noises = {}
    for key, value in dict_km_score.items():
        for k, v in value.items():
            if v[2] < Decimal("0.1") or v[4] < Decimal("0.1"):
                print("found noise data on " + key + " " + str(k) + " " + str(v))
                if key in noises:
                    noises[key].append(k)
                else:
                    noises[key] = [k]

    # mark noise data
    cursor = None
    cnx = None
    rows = None
    try:
        cnx = pymysql.connect(user=username, password=password, host=host, database=dbname)
        cursor = cnx.cursor()
        cursor.execute('SELECT id, "", "", km3, km4, km5, km6, km7, km8, km9, km10 FROM logrw where type = 2')
        rows = cursor.fetchall()
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()

    for row in rows:
        for idx_km in range(3, 11):
            key = "km" + str(idx_km)
            if (key) in noises and row[idx_km] in noises[key]:
                print(str(row[0]) + " is high probability noise. " + str(key) + " " + str(row[idx_km]))
        