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
from math import ceil
import warnings
import traceback
import numpy
import matplotlib.pyplot as plt
import numpy as np
from sklearn import datasets, linear_model
from sklearn.metrics import mean_squared_error, r2_score
from price import *

warnings.filterwarnings("ignore")

# for i in range(1, 10):
#     series = read_csv('o_' + str(i) + '.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
#     series.plot()

series = read_csv('o_2.csv', index_col=0)
# series.plot()
# pyplot.show()

X = series.values
X = X.astype('float32')

days = numpy.zeros(shape=(len(X), 1))
for t in range(0, len(days)):
    days[t] = [t + 1]
rw = [x[0] for x in X]

predLen = 60

# Split the data into training/testing sets
X_train = days[:-predLen]
X_test = days[-predLen:]

# Split the targets into training/testing sets
y_train = rw[:-predLen]
y_test = rw[-predLen:]

# Create linear regression object
regr = linear_model.LinearRegression()

# Train the model using the training sets
regr.fit(X_train, y_train)

# Make predictions using the testing set
y_pred = regr.predict(X_test)

# The coefficients
print('Coefficients: \n', regr.coef_)
# The mean squared error
print("Mean squared error: %.2f"
      % mean_squared_error(y_test, y_pred))
# Explained variance score: 1 is perfect prediction
print('Variance score: %.2f' % r2_score(y_test, y_pred))

# Plot outputs
plt.plot(X_train, y_train,  color='black')
plt.plot(X_test, y_test,  color='black')
plt.plot(X_test, y_pred, color='blue', linewidth=3)

plt.xticks(())
plt.yticks(())

plt.show()