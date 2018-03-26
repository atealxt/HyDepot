import matplotlib.pylab as plt
import numpy as np
import random
import pymysql

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
        cursor.execute('SELECT rw_detail FROM logrw')
        cursor.scroll(110)
        rows = cursor.fetchmany(40)
        for row in rows:
            var = [int(x) for x in row[0].split(',')]
            rw.append(var)
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()

    rwData = np.array(rw)

    for idx, item in enumerate(rwData):
        line = plt.plot(item, label='Data ' + str(idx), color='red')
        plt.legend()
        plt.show()