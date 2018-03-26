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
from price import *

warnings.filterwarnings("ignore")

class predict_arima(object):

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
        
        # evaluate an ARIMA model for a given order (p,d,q) and return RMSE
        def evaluate_arima_model(X, arima_order):
            # prepare training dataset
            _train_size = int(len(X) * 0.75)
            _train, _test = X[0:_train_size], X[_train_size:]
            _history = [x for x in _train]
            # make predictions
            predictions = list()
            for t in range(len(_test)):
                # difference data
                model = ARIMA(_history, order=arima_order)
                model_fit = model.fit(trend='nc', disp=0)
                yhat = model_fit.forecast()[0]
                predictions.append(yhat)
                _history.append(_test[t])
            # calculate out of sample error
            mse = mean_squared_error(_test, predictions)
            rmse = sqrt(mse)
            return rmse
        
        # evaluate combinations of p, d and q values for an ARIMA model
        def evaluate_models(dataset, p_values, d_values, q_values):
            best_score, best_cfg = float("inf"), None
            for p in p_values:
                for d in d_values:
                    for q in q_values:
                        order = (p,d,q)
                        try:
                            mse = evaluate_arima_model(dataset, order)
                            if mse < best_score:
                                best_score, best_cfg = mse, order
                        except:
        #                     traceback.print_exc()
                            continue
            return best_cfg
        
        for predictStartDay in predictStartDays:
    
            if progress:
                print("predict from day " + str(predictStartDay))
    
        
            train = X[0:predictStartDay]
            history = [x for x in train]
            
            p_values = range(0, 5)
            d_values = range(0, 2)
            q_values = range(0, 3)
            best_cfg = evaluate_models(history, p_values, d_values, q_values)
        
            predictMove = False
            
            for predictDay in predictDays: 
            
    #             print("predict " + str(predictDay) + " days from day " + str(predictStartDay))
        
                if predictMove:
                    # already decide to move, no need to predict more days
                    break
        
                # step1
                # predict rw count for future days
        
                train = X[0:predictStartDay]
                history = [x for x in train]
                bias = None
                predictions = list()
        
                predErr = False
        
                for t in range(predictDay):    
                    try:
                        model = ARIMA(history, order=best_cfg)
                        model_fit = model.fit(trend='nc', disp=0)
                        if bias == None:
                            residuals = DataFrame(model_fit.resid)
                            bias = residuals.mean()[0]                    
                        yhat = model_fit.forecast()[0]
                        yhat = bias + yhat
                        predictions.append(ceil(yhat))
                        history.append(yhat)
                    except:
        #                 traceback.print_exc()
        #                 raise
                        predErr = True
                        break
        #         print("predicted rw counts: " + str(predictions))
                
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
                    
                    print("predict move at day " + str(predictStartDay) + " (forecast " + str(predictDay) + " days), real saving if move at that day: " + format(diff))
        #             print("predict " + str(predictDay) + " days from day " + str(predictStartDay))
        #             print("move at day: " + str(len(history)) + ", saving " + format(bestSaving))
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

    obj_arima = predict_arima()
    obj_arima.predict(X)