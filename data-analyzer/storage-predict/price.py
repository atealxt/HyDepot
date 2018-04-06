from pandas import read_csv
from pandas import datetime
from matplotlib import pyplot
from pandas import DataFrame
from pandas import concat
from sklearn.metrics import mean_squared_error
from pandas import Series
from pandas.plotting import lag_plot
from pandas.plotting import autocorrelation_plot
from statsmodels.graphics.tsaplots import plot_acf
from statsmodels.graphics.tsaplots import plot_pacf
from statsmodels.tsa.ar_model import AR
from statsmodels.tsa.arima_model import ARIMA
from math import sqrt
import warnings
import traceback
import numpy

STORAGE_PRICE_UNIT_CLASS1 = 0.023
STORAGE_PRICE_UNIT_CLASS2 = 0.0125
MIN_UNIT_SIZE_IN_KB_CLASS2 = 128
OPERATION_PRICE_READ_UNIT_CLASS1 = 0.004 / 1000
OPERATION_PRICE_UNIT_CLASS2 = 0.01 / 1000
PRICE_CHANGE_CLASS = 0.01 / 1000

SIZE_500K = 500 / 1024.0;
SIZE_1M = 1;
SIZE_10M = 10;
sizeInMB = SIZE_10M;

def format(num):
    return "{:10.5f}".format(num)

def price1(X):
    existDaysInClass1 = len(X)
    storagePrice1 = STORAGE_PRICE_UNIT_CLASS1 * (sizeInMB / 1024.0) * ((existDaysInClass1) / 30.0);
    rwCount = 0
    for t in range(0, len(X)):
        if X[t] >= 0:
            rwCount += X[t]
    operationPrice1 = OPERATION_PRICE_READ_UNIT_CLASS1 * rwCount
    price1 = storagePrice1 + operationPrice1;
    return price1    

def price2(X):
    existDaysInClass2 = len(X);
    storagePrice2 = STORAGE_PRICE_UNIT_CLASS2 * max(sizeInMB / 1024.0, MIN_UNIT_SIZE_IN_KB_CLASS2 / 1024.0 / 1024.0
                                                    ) * (max(existDaysInClass2, 30) / 30.0)
    rwCount = 0
    for t in range(0, len(X)):
        if X[t] >= 0:
            rwCount += X[t]
    operationPrice2 = OPERATION_PRICE_UNIT_CLASS2 * rwCount;
    price2 = storagePrice2 + operationPrice2 + PRICE_CHANGE_CLASS;
    return price2
