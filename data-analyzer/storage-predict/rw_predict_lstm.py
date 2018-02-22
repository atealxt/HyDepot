from pandas import DataFrame
from pandas import Series
from pandas import concat
from pandas import read_csv
from pandas import datetime
from sklearn.metrics import mean_squared_error
from sklearn.preprocessing import MinMaxScaler
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM
from math import sqrt
from matplotlib import pyplot
import numpy
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

# frame a sequence as a supervised learning problem
def timeseries_to_supervised(data, lag=1):
    df = DataFrame(data)
    columns = [df.shift(i) for i in range(1, lag+1)]
    columns.append(df)
    df = concat(columns, axis=1)
    df.fillna(0, inplace=True)
    return df

# create a differenced series
def difference(dataset, interval=1):
    diff = list()
    for i in range(interval, len(dataset)):
        value = dataset[i] - dataset[i - interval]
        diff.append(value)
    return Series(diff)

# invert differenced value
def inverse_difference(history, yhat, interval=1):
    return yhat + history[-interval]

# scale train and test data to [-1, 1]
def scale(train):
    # fit scaler
    scaler = MinMaxScaler(feature_range=(-1, 1))
    scaler = scaler.fit(train)
    # transform train
    train = train.reshape(train.shape[0], train.shape[1])
    train_scaled = scaler.transform(train)
    return scaler, train_scaled

# inverse scaling for a forecasted value
def invert_scale(scaler, X, value):
    new_row = [x for x in X] + [value]
    array = numpy.array(new_row)
    array = array.reshape(1, len(array))
    inverted = scaler.inverse_transform(array)
    return inverted[0, -1]

# fit an LSTM network to training data
def fit_lstm(train, batch_size, nb_epoch, neurons):
    X, y = train[:, 0:-1], train[:, -1]
    X = X.reshape(X.shape[0], 1, X.shape[1])
    model = Sequential()
    model.add(LSTM(neurons, batch_input_shape=(batch_size, X.shape[1], X.shape[2]), stateful=True))
    model.add(Dense(1))
    model.compile(loss='mean_squared_error', optimizer='adam')
    for i in range(nb_epoch):
        model.fit(X, y, epochs=1, batch_size=batch_size, verbose=0, shuffle=False)
        model.reset_states()
    return model

# make a one-step forecast
def forecast_lstm(model, batch_size, X):
    X = X.reshape(1, 1, len(X))
    yhat = model.predict(X, batch_size=batch_size)
    return yhat[0,0]

def predict(X, progress=True, stopIfFound=False):

    # real saving in God model:
    obs = [x[0] for x in X]
    price1Obs = price1(obs)
    bestDayObs = None
    bestSavingObs = 0
    for t in range(0, len(obs) - 1):
        # price before move
        p1 = price1(obs[0:t + 1])
        # price after move
        p2 = price2(obs[t + 1:])
        pp = p1 + p2
        diff = price1Obs - pp
        if diff > bestSavingObs:
            bestSavingObs = diff
            bestDayObs = t + 1
    print("Best move day: " + str(bestDayObs) + ", saving " + format(bestSavingObs))

    # transform data to be stationary
    raw_values = series.values
    diff_values = difference(raw_values, 1)
    
    # transform data to be supervised learning
    supervised = timeseries_to_supervised(diff_values, 1)
    supervised_values = supervised.values
    
    for predictStartDays in range(8, 30):
    
        predictMove = False
        raw_values_predict = raw_values[0:predictStartDays]
        
        for predictDays in range(7, 30): 
        
            if progress:
                print("predict " + str(predictDays) + " days from day " + str(predictStartDays))
        
            if predictMove:
                # already decide to move, no need to predict more days
                break
    
            # step1
            # predict rw count for future days
    
            # split data into train and test-sets
            train = supervised_values[0:predictStartDays]
            train_raw_values = raw_values[0:predictStartDays]
            
            # transform the scale of the data
            scaler, train_scaled = scale(train)
            
            # fit the model
            lstm_model = fit_lstm(train_scaled, 1, 1500, 1)
            # forecast the entire training dataset to build up state for forecasting
            train_reshaped = train_scaled[:, 0].reshape(len(train_scaled), 1, 1)
            yhat = lstm_model.predict(train_reshaped, batch_size=1)
            yhat = yhat[len(yhat) - 1]
            # walk-forward validation on the test data
            predictions = list()
            for i in range(predictDays):
                # make one-step forecast
                X = yhat
                X = X.reshape(1, 1, 1)
                yhat = (lstm_model.predict(X, batch_size=1))[0,0]
                
                # invert scaling
                predict = yhat
                predict = invert_scale(scaler, X, predict)
                # invert differencing
                predict = predict + raw_values_predict[-1]
                # store forecast
                predictions.append(ceil(predict))
    #             raw_values_predict.append(predict)
                raw_values_predict = np.append(raw_values_predict, predict)
    
    
        
    # TODO RMSE for all models.
    
    
    
    
    
    
    
    
            # step2
            # calc the best date to move, if it is today, move! (then go to step3)
            history = [x[0] for x in train_raw_values]
            Y = history + predictions
            p = price1(Y)
            bestSaving = -1
            for t in range(len(history), len(Y)):
                # price before move
                p1 = price1(Y[0:t + 1])
                # price after move
                p2 = price2(Y[t + 1:])
                pp = p1 + p2
                diff = p - pp
                if diff > 0.000005:
                    if bestSaving < diff:
                        if t > len(history):
                            # not today, so won't move.
                            break
                        bestSaving = diff
    
            # step3
            # compare with the real best date
            if bestSaving != -1:
                predictMove = True
                
                # price before move
                p1 = price1(obs[0:len(history)])
                # price after move
                p2 = price2(obs[len(history):])
                pp = p1 + p2
                diff = price1Obs - pp
                
                print("predict move at day " + str(predictStartDays) + " (forecast " + str(predictDays) + " days), real saving if move at that day: " + format(diff))
                if stopIfFound:
                    return

if __name__ == "__main__":

    # for i in range(1, 10):
    #     series = read_csv('o_' + str(i) + '.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
    #     series.plot()
    
    series = read_csv('o_5.csv', index_col=0)
    # series.plot()
    # pyplot.show()
    
    X = series.values
    X = X.astype('float32')

    predict(X)