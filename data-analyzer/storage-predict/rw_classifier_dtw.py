import random
import pymysql
from sklearn.metrics import classification_report
import matplotlib.pylab as plt
import numpy as np
import re

class ts_classifier(object):
    
    def __init__(self,plotter=False):
        '''
        preds is a list of predictions that will be made.
        plotter indicates whether to plot each nearest neighbor as it is found.
        '''
        self.preds=[]
        self.plotter=plotter
    
    def predict(self,train,test,w,progress=False):
        '''
        1-nearest neighbor classification algorithm using LB_Keogh lower 
        bound as similarity measure. Option to use DTW distance instead
        but is much slower.
        '''
        for ind,i in enumerate(test):
            if progress:
                print(str(ind+1) + ' points classified')
            min_dist=float('inf')
            closest_seq=[]
    
            for j in train:
                if self.LB_Keogh(i,j[:-1],5)<min_dist:
                    dist=self.DTWDistance(i,j[:-1],w)
                    if dist<min_dist:
                        min_dist=dist
                        closest_seq=j
            self.preds.append(closest_seq[-1])
            
            if self.plotter: 
                plt.plot(i)
                plt.plot(closest_seq[:-1])
                plt.legend(['Test Series','Nearest Neighbor in Training Set'])
                plt.title('Nearest Neighbor in Training Set - Prediction ='+str(closest_seq[-1]))
                plt.show()
        
        
    def performance(self,true_results):
        '''
        If the actual test set labels are known, can determine classification
        accuracy.
        '''
        return classification_report(true_results,self.preds)
    
    def get_preds(self):
        return self.preds
    
    
    def DTWDistance(self,s1,s2,w=None):
        '''
        Calculates dynamic time warping Euclidean distance between two
        sequences. Option to enforce locality constraint for window w.
        '''
        DTW={}
    
        if w:
            w = max(w, abs(len(s1)-len(s2)))
    
            for i in range(-1,len(s1)):
                for j in range(-1,len(s2)):
                    DTW[(i, j)] = float('inf')
            
        else:
            for i in range(len(s1)):
                DTW[(i, -1)] = float('inf')
            for i in range(len(s2)):
                DTW[(-1, i)] = float('inf')
        
        DTW[(-1, -1)] = 0
    
        for i in range(len(s1)):
            if w:
                for j in range(max(0, i-w), min(len(s2), i+w)):
                    dist= (s1[i]-s2[j])**2
                    DTW[(i, j)] = dist + min(DTW[(i-1, j)],DTW[(i, j-1)], DTW[(i-1, j-1)])
            else:
                for j in range(len(s2)):
                    dist= (s1[i]-s2[j])**2
                    DTW[(i, j)] = dist + min(DTW[(i-1, j)],DTW[(i, j-1)], DTW[(i-1, j-1)])
            
        return np.sqrt(DTW[len(s1)-1, len(s2)-1])
       
    def LB_Keogh(self,s1,s2,r):
        '''
        Calculates LB_Keough lower bound to dynamic time warping. Linear
        complexity compared to quadratic complexity of dtw.
        '''
        LB_sum=0
        for ind,i in enumerate(s1):
            
            lower_bound=min(s2[(ind-r if ind-r>=0 else 0):(ind+r)])
            upper_bound=max(s2[(ind-r if ind-r>=0 else 0):(ind+r)])
            
            if i>upper_bound:
                LB_sum=LB_sum+(i-upper_bound)**2
            elif i<lower_bound:
                LB_sum=LB_sum+(i-lower_bound)**2
        
        return np.sqrt(LB_sum)

# select t.`type`, count(*) from logrw t where t.`type` is not null group by t.`type`    
def sampleByType(rwData, trainRate = 0.8, dataRange = 30):

    map = {}
    for d in rwData:
        if d[len(d) - 1] not in map:
            map[d[len(d) - 1]] = []
        map[d[len(d) - 1]].append(d)
    train,test = [], []
    for key, value in map.items():
        train_size = int(len(value) * trainRate)
        for v in value[0:train_size]:
            train.append(np.append(v[0:dataRange], v[-1:]))
        for v in value[train_size:]:
            test.append(np.append(v[0:dataRange], v[-1:]))
    train,test = np.array(train), np.array(test)
    print("train rate: " + str(trainRate) + ", data range: " + str(dataRange) + ", train size: " + str(len(train)) + ", test size: " + str(len(test)))
    return train, test

if __name__ == "__main__":
    
    print("get data from db")

    dbname = "project_m"
    host = "127.0.0.1"
    username = "root"
    password = "root"

    dataSize = 150
    rw = []
    cursor = None
    cnx = None
    try:
        cnx = pymysql.connect(user=username, password=password, host=host, database=dbname)
        cursor = cnx.cursor()
        cursor.execute('SELECT rw_detail, type FROM logrw')
        rows = cursor.fetchmany(dataSize)
        for row in rows:
            var = [int(x) for x in row[0].split(',')]
            var.append(row[1])
            rw.append(var)
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()

    rwData = np.array(rw)

    print("classifying")
    
    fig = plt.figure()
    fig.suptitle("Classification F1 Score Trends")
    trainRates = [0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95]
    for idxRate, trainRate in enumerate(trainRates):
        
        type1, type2, type3, type4 = [], [], [] ,[]
        sum_f1_score = []
        
        for dataRange in range(5, 31):
    
            classifier = ts_classifier(False)
            train, test = sampleByType(rwData, trainRate, dataRange)
            classifier.predict(train, test, 4, False)
            
            report = classifier.performance(test[:,-1])
            print(report)
            
            lines = report.split('\n')
            
            f1_score1 = re.sub('\s+', ' ', lines[2]).split(' ')[4]
            f1_score2 = re.sub('\s+', ' ', lines[3]).split(' ')[4]
            f1_score3 = re.sub('\s+', ' ', lines[4]).split(' ')[4]
            f1_score4 = re.sub('\s+', ' ', lines[5]).split(' ')[4]
            
            type1.append(f1_score1)
            type2.append(f1_score2)
            type3.append(f1_score3)
            type4.append(f1_score4)
            sum_f1_score.append(float(f1_score1) + float(f1_score2) + float(f1_score3) + float(f1_score4))
        
        plt.subplot(4, 2, idxRate + 1)
        plt.plot(range(5, 31), type1, label='Type 1', color='blue')
        plt.plot(range(5, 31), type2, label='Type 2', color='red')
        plt.plot(range(5, 31), type3, label='Type 3', color='green')
        plt.plot(range(5, 31), type4, label='Type 4', color='orange')
        plt.plot(range(5, 31), sum_f1_score, label='Sum', color='black')
        plt.title("Train Rate " + str(int(trainRate * 100)) + "%", fontsize=10)
        if idxRate > 5:
            plt.xlabel('Train Days', fontsize=10)
        if idxRate % 2 == 0:
            plt.ylabel('F1 Score', fontsize=10)

    plt.legend()
#     fig.text(0.5, 0.04, 'Train Days', ha='center')
#     fig.text(0.04, 0.5, 'F1 Score', va='center', rotation='vertical')
    fig.tight_layout()
    plt.show()
        
