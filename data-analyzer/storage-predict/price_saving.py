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
from price import *

# for i in range(1, 10):
#     series = read_csv('o_' + str(i) + '.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
#     series.plot()

series = read_csv('o_1.csv', index_col=0)
# series.plot()
# pyplot.show()

sizeInMB = SIZE_10M;

X = series.values
X = X.astype('float32')

X = [x[0] for x in X]

# Price Saving in God model:
p = price1(X)
print('\"Day\"\t\"R/W\"\t\"$Saving per 10k\"')
for t in range(0, len(X) - 1):
    # price before move
    p1 = price1(X[0:t + 1])
    # price after move
    p2 = price2(X[t + 1:])
    pp = p1 + p2
    diff = p - pp
    print(str(t + 1) + "\t" + str(int(X[t])) + "\t" + format(10000 * diff)) 


