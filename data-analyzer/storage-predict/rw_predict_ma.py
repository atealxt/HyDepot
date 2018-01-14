from pandas import read_csv
from pandas import datetime
from matplotlib import pyplot
from pandas import DataFrame
from pandas import concat
from numpy import mean
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
from price import *

warnings.filterwarnings("ignore")

# for i in range(1, 10):
#     series = read_csv('o_' + str(i) + '.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
#     series.plot()

series = read_csv('o_4.csv', index_col=0)
# series.plot()
# pyplot.show()

X = series.values
X = X.astype('float32')

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

for predictStartDays in range(8, 30):

    train = X[0:predictStartDays]
    history = [x for x in train]
    
    predictMove = False
    
    for predictDays in range(7, 30): 
    
        print("predict " + str(predictDays) + " days from day " + str(predictStartDays))

        if predictMove:
            # already decide to move, no need to predict more days
            break

        # step1
        # predict rw count for future days

        train = X[0:predictStartDays]
        history = [x for x in train]
        predictions = list()
        
        window = 6

        predErr = False

        for t in range(predictDays):    
            try:
                length = len(history)
                yhat = mean([history[i] for i in range(length - window,length)])                    
                predictions.append(ceil(yhat))
                history.append(yhat)
            except:
                raise
                predErr = True
                break
        
        if predErr:
            continue
        
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
            
            print("predict move at day " + str(predictStartDays) + " (forecast " + str(predictDays) + " days), real saving if move at that day: " + format(diff))
#             print("predict " + str(predictDays) + " days from day " + str(predictStartDays))
#             print("move at day: " + str(len(history)) + ", saving " + format(bestSaving))