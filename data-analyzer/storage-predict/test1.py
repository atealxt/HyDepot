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

series = read_csv('data1.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
print(series.head())
# print(series.keys())
# series.plot()
# pyplot.show()

######### create ARIMA base on 30 days data #########
# calc the autocorrelation, known that 1-3 has positive correlation. 
# Significant for the first lags, which means AR(p) = 1
# autocorrelation_plot(series)
# pyplot.show()

X = series.values
X = X.astype('float32')
train_size = 30
train, test = X[0:train_size], X[train_size:]
history = [x for x in train]

# pyplot.figure()
# pyplot.subplot(211)
# plot_acf(history, ax=pyplot.gca())
# pyplot.subplot(212)
# plot_pacf(history, ax=pyplot.gca())
# # The ACF shows a significant lag for 1 day, the PACF shows a significant lag for 1 day too.
# # Both the ACF and PACF show a drop-off at the same point, perhaps suggesting a mix of AR and MA.
# # A good starting point for the p and q values is also 1.
# pyplot.show()

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
                    print('ARIMA%s RMSE=%.3f' % (order,mse))
                except:
#                     traceback.print_exc()
                    continue
    print('Best ARIMA%s RMSE=%.3f' % (best_cfg, best_score))
    return best_cfg

# evaluate parameters (not test much ranges case of overfitting)
p_values = range(0, 3)
d_values = range(0, 2)
q_values = range(0, 3)
warnings.filterwarnings("ignore")
best_cfg = evaluate_models(history, p_values, d_values, q_values)
# now we know the best is not (1,0,1) but (2,0,1) 

# test ARIMA
# fit model:
# lag value to 2 for autoregression, and uses a moving average model of 1.
model = ARIMA(history, order=best_cfg)
model_fit = model.fit(disp=0)
print(model_fit.summary())
# plot residual errors
residuals = DataFrame(model_fit.resid)
# residuals.plot()
# pyplot.show()
# residuals.plot(kind='kde')
# # this suggesting the errors are Gaussian, and centered nearby zero.
# pyplot.show()
print(residuals.describe())
bias = residuals.mean()[0]

def format(num):
    return "{:10.5f}".format(num)

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

# TODO fix this patch!
best_cfg = (1,0,1)

for t in range(30, len(X)):
    train_size = t
    train, test = X[0:train_size], X[train_size:]
    history = [x for x in train]
    
    rwCountPredicted = 0;
    rwCountExpected = 0;

    predictions = list()
    for t in range(len(test)):
        try:
            model = ARIMA(history, order=best_cfg)
            model_fit = model.fit(trend='nc', disp=0)
            yhat = model_fit.forecast()[0]
            yhat = bias + yhat
            predictions.append(yhat)
            obs = test[t]
#             print('date=%s, predicted=%f, expected=%f' % (series.keys()[t], yhat, obs))
        
#             if (yhat > 0):
#                 rwCountPredicted += round(yhat);
            rwCountPredicted += yhat;
#             print(yhat)
            rwCountExpected += obs
#             history.append(obs)
            history.append(round(yhat))
        except:
            traceback.print_exc()
            raise

#     error = mean_squared_error(test, predictions)
#     print('Test MSE: %.3f' % error)
#     pyplot.plot(test)
#     pyplot.plot(predictions, color='red')
#     pyplot.show()

    rwCountPredicted = round(rwCountPredicted)
    if (rwCountPredicted < 0):
        rwCountPredicted = 0;
#     print(str(rwCountPredicted) + " " + str(rwCountExpected))

    # calc price
    existDaysInClass1 = train_size
    storagePrice1 = STORAGE_PRICE_UNIT_CLASS1 * (sizeInMB / 1024.0) * ((31 + t) / 30.0);
    existDaysInClass2 = len(X) - existDaysInClass1;
    storagePrice2 = STORAGE_PRICE_UNIT_CLASS1 * (sizeInMB / 1024.0) * (existDaysInClass1 / 30.0)
    + STORAGE_PRICE_UNIT_CLASS2 * max(sizeInMB / 1024, MIN_UNIT_SIZE_IN_KB_CLASS2 / 1024.0 / 1024.0
                                      ) * (max(existDaysInClass2, 30) / 30.0)
    operationPrice1 = OPERATION_PRICE_READ_UNIT_CLASS1 * rwCountPredicted;
    operationPrice2 = OPERATION_PRICE_UNIT_CLASS2 * rwCountPredicted;
    price1 = storagePrice1 + operationPrice1;
    price2 = storagePrice2 + operationPrice2 + PRICE_CHANGE_CLASS;
    # if class2 price is lower, move to class2
    if (price1 > price2):
        out = "A file size " + str(sizeInMB) + "MB stayed in class1 in " + str(existDaysInClass1) + " days, ";
        out += "will cost LESS if moved to class2 and stay " + str(existDaysInClass2) + " days ";
        out += "with read/write " + str(rwCountPredicted) + " times. ";
        out += "Cost1: " + format(price1) + ", Cost2: " + format(price2) + ". ";
        saved = (price1 - price2) / price1 * 100;
        out += "Saved " + format(saved) + "%";
        print(out)
        ######### verify with the real data #########
        realPriceIfStayInClass1 = 0.0
        realPriceIfMovedTOClass2 = 0.0
        operationPrice1 = OPERATION_PRICE_READ_UNIT_CLASS1 * rwCountExpected;
        operationPrice2 = OPERATION_PRICE_UNIT_CLASS2 * rwCountExpected;
        realPriceIfStayInClass1 = storagePrice1 + operationPrice1;
        realPriceIfMovedTOClass2 = storagePrice2 + operationPrice2 + PRICE_CHANGE_CLASS;
        print("real price: realPriceIfStayInClass1: " + format(realPriceIfStayInClass1) + ", realPriceIfMovedTOClass2: " + format(realPriceIfMovedTOClass2))
    # we can found that bigger file, more precisely.
#         TODO decreasing the incorrect predict / cost by: 
        # file size or other threshold; 
        # days window  
        # move back detection
    
#     else:
#         # otherwise, keep in class1 and update the model.
#         out = "A file size " + str(sizeInMB) + "MB stayed in class1 in " + str(existDaysInClass1) + " days, ";
#         out += "will cost MORE if moved to class2 and stay " + str(existDaysInClass2) + " days ";
#         out += "with read/write " + str(rwCountPredicted) + " times. ";
#         out += "Cost1: " + format(price1) + ", Cost2: " + format(price2) + ". ";
#         saved = (price1 - price2) / price1 * 100;
#         out += "Saved " + format(saved) + "%";
#         print(out)


#### Follows is a step by step prediction, just for demo

# train_size = 30
# train, test = X[0:train_size], X[train_size:]
# history = [x for x in train]
#  
# predictions = list()
# for t in range(len(test)):
#     model = ARIMA(history, order=best_cfg)
#     model_fit = model.fit(trend='nc', disp=0)
#     yhat = model_fit.forecast()[0]
#     yhat = bias + yhat
#     predictions.append(yhat)
#     obs = test[t]
#     print('date=%s, predicted=%f, expected=%f' % (series.keys()[train_size + t], yhat, obs))
#     history.append(obs)
#  
# error = mean_squared_error(test, predictions)
# print('Test MSE: %.3f' % error)
# # plot
# pyplot.plot(test)
# pyplot.plot(predictions, color='red')
# pyplot.show()
