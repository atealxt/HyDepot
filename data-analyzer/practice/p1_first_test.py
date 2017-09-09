# How to Load and Explore Time Series Data
# https://machinelearningmastery.com/load-explore-time-series-data-python/

# Load birth data using read_csv
from pandas import read_csv

series = read_csv('daily-total-female-births-in-cal.csv', header=0, parse_dates=[0], index_col=0, squeeze=True)
print(type(series))
print(series.head())
