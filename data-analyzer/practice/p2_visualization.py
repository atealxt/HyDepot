# Time Series Data Visualization
# https://machinelearningmastery.com/time-series-data-visualization-with-python/

from pandas import Series
from matplotlib import pyplot
from pandas import read_csv
from pandas import Series
from pandas import DataFrame
from pandas import TimeGrouper
from pandas import concat
from pandas.plotting import lag_plot
from pandas.plotting import autocorrelation_plot

series = read_csv('daily-minimum-temperatures.csv', parse_dates=[0], 
                  index_col=0, squeeze=True)
print(series.head())

# basic line plot
series.plot()
pyplot.show()
 
# dot plot
series.plot(style='k.')
pyplot.show()
 
# group line plot by years
groups = series.groupby(TimeGrouper('A'))
years = DataFrame()
for name, group in groups:
    years[name.year] = group.values
years.plot(subplots=True, legend=False)
pyplot.show()
 
# histogram plot
series.hist()
pyplot.show()

# density plot
series.plot(kind='kde')
pyplot.show()

# box plot group by year
groups = series.groupby(TimeGrouper('A'))
years = DataFrame()
for name, group in groups:
    years[name.year] = group.values
years.boxplot()
pyplot.show()

# box plot of the last year, group by month
one_year = series['1990']
groups = one_year.groupby(TimeGrouper('M'))
months = concat([DataFrame(x[1].values) for x in groups], axis=1)
months = DataFrame(months)
months.columns = range(1,13)
months.boxplot()
pyplot.show()

# heat map (year-columns and day-rows)
groups = series.groupby(TimeGrouper('A'))
years = DataFrame()
for name, group in groups:
    years[name.year] = group.values
years = years.T
pyplot.matshow(years, interpolation=None, aspect='auto')
pyplot.show()

# heat map of the last year (year-columns and day-rows)
one_year = series['1990']
groups = one_year.groupby(TimeGrouper('M'))
months = concat([DataFrame(x[1].values) for x in groups], axis=1)
months = DataFrame(months)
months.columns = range(1,13)
pyplot.matshow(months, interpolation=None, aspect='auto')
pyplot.show()

# lag plot (correlation between observations and their lag1 values.)
# this one is strong positive 
lag_plot(series)
pyplot.show()

# more lags comparing
values = DataFrame(series.values)
lags = 7
columns = [values]
for i in range(1,(lags + 1)):
    columns.append(values.shift(i))
dataframe = concat(columns, axis=1)
columns = ['t+1']
for i in range(1,(lags + 1)):
    columns.append('t-' + str(i))
dataframe.columns = columns
pyplot.figure(1)
for i in range(1,(lags + 1)):
    ax = pyplot.subplot(240 + i)
    ax.set_title('t+1 vs t-' + str(i))
    pyplot.scatter(x=dataframe['t+1'].values, y=dataframe['t-'+str(i)].values)
pyplot.show()

# autocorrelation plot (lag along the x-axis and the correlation on the y-axis).
# results in a number between -1(negative) and 1(positive), close to zero suggests a weak correlation, vise versa.
# this one means very seasonality
autocorrelation_plot(series)
pyplot.show()