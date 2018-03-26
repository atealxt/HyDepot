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

class predict_linreg(object):

    def __init__(self):
        self.bestDay=None
        self.bestSaving=None
        self.predictDay=None
        self.predictSaving=-1

    def get_bestDay(self):
        return self.bestDay
    def get_bestSaving(self):
        return self.bestSaving
    def get_predictDay(self):
        return self.predictDay
    def get_predictSaving(self):
        return self.predictSaving

    def predict(self, X, progress=True, stopIfFound=False, predictStartDays=range(8, 30), predictDays=range(7, 30)):
    
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
        self.bestDay=bestDayObs
        self.bestSaving=bestSavingObs
        
        for predictStartDay in predictStartDays:
        
            predictMove = False
            
            for predictDay in predictDays: 
            
                if predictMove:
                    # already decide to move, no need to predict more days
                    break
        
                train = X[0:predictStartDay]
                history = [x[0] for x in train]
                
                # step1
                # predict rw count for future days
        
                days = numpy.zeros(shape=(predictStartDay + predictDay, 1))
                for t in range(0, len(days)):
                    days[t] = [t + 1]
                
                # Split the data into training/testing sets
                X_train = days[:-predictDay]
                X_test = days[-predictDay:]
                
                # Split the targets into training/testing sets
                y_train = history
                
                # Create linear regression object
                regr = linear_model.LinearRegression()
                
                # Train the model using the training sets
                regr.fit(X_train, y_train)
                
                # Make predictions using the testing set
                predictions = regr.predict(X_test).tolist()
        
                # step2
                # calc the best date to move, if it is today, move! (then go to step3)
                history = [x[0] for x in train]
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
                    
                    print("predict move at day " + str(predictStartDay) + " (forecast " + str(predictDay) + " days), real saving if move at that day: " + format(diff))
                    self.predictDay=predictStartDay
                    self.predictSaving=diff
                    if stopIfFound:
                        return

if __name__ == "__main__":

    # for i in range(1, 10):
    #     series = read_csv('o_' + str(i) + '.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
    #     series.plot()
    
    series = read_csv('o_cluster_3.csv', index_col=0)
    # series.plot()
    # pyplot.show()
    
    X = series.values
    X = X.astype('float32')

    obj_arima = predict_linreg()
    obj_arima.predict(X)
