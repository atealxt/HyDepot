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

# for i in range(1, 10):
#     series = read_csv('o_' + str(i) + '.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
#     series.plot()

series = read_csv('o_1.csv', index_col=0)
series.plot()
pyplot.show()

