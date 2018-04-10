import matplotlib.pylab as plt
import numpy as np
import random
import pymysql
import collections

class ts_cluster(object):
    def __init__(self,num_clust):
        '''
        num_clust is the number of clusters for the k-means algorithm
        assignments holds the assignments of data points (indices) to clusters
        centroids holds the centroids of the clusters
        '''
        self.num_clust=num_clust
        self.assignments={}
        self.centroids=[]
        
    def k_means_clust(self,data,num_iter,w,progress=False):
        '''
        k-means clustering algorithm for time series data.  dynamic time warping Euclidean distance
         used as default similarity measure. 
        '''
        self.centroids=random.sample(list(data[1:len(data)]),self.num_clust)
#         centroids = []
#         centroids.extend([data[0], data[1], data[99]])
#         self.centroids = centroids
#         data[0], data[4] = data[4], data[0]
#         data[1], data[2] = data[2], data[1]
        
        for n in range(num_iter):
            if progress:
                print("iteration " + str(n+1))
            #assign data points to clusters
            self.assignments={}
            for ind,i in enumerate(data):
                if ind == 0:
                    pass
                min_dist=float('inf')
                closest_clust=None
                for c_ind,j in enumerate(self.centroids):
                    if self.LB_Keogh(i,j,5)<min_dist:
                        cur_dist=self.DTWDistance(i,j,w)
                        if cur_dist<min_dist:
                            min_dist=cur_dist
                            closest_clust=c_ind
                if closest_clust in self.assignments:
                    self.assignments[closest_clust].append(ind)
                else:
                    self.assignments[closest_clust]=[]
        
            #recalculate centroids of clusters
            for key in self.assignments:
                clust_sum=0
                if len(self.assignments[key]) == 0:
                    continue
                for k in self.assignments[key]:
                    clust_sum=clust_sum+data[k]
                self.centroids[key]=[m/len(self.assignments[key]) for m in clust_sum]
            

    def get_centroids(self):
        return self.centroids
        
    def get_assignments(self):
        return self.assignments
        
    def plot_centroids(self, round=False):
        _centroids = self.centroids
        if round:
            _centroids = np.around(self.centroids)
        for idx, item in enumerate(_centroids):
            line = plt.plot(item, label='Type ' + str(idx + 1))
        plt.legend()
        plt.title("Access Trends")
        plt.xlabel('Days')
        plt.ylabel('R/W Counts')
        plt.show()
        
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
    
if __name__ == "__main__":

    print("get data from db")

    dbname = "project_m"
    host = "127.0.0.1"
    username = "root"
    password = "root"

    rw = []
    cursor = None
    cnx = None
    try:
        cnx = pymysql.connect(user=username, password=password, host=host, database=dbname)
        cursor = cnx.cursor()
        cursor.execute('SELECT rw_detail FROM logrw')
        rows = cursor.fetchmany(150)
        for row in rows:
            var = [int(x) for x in row[0].split(',')]
            rw.append(var)
    finally:
        if cursor:
            cursor.close()
        if cnx:
            cnx.close()

    rwData = np.array(rw)

    print("clustering")

    for num_cluster in range(3, 11):

    #     clusters = ['blue', 'red', 'green', 'orange', 'cyan', 'purple', 'black']
        cluster = ts_cluster(num_cluster) #test cluster, to see which class has more arima, which has none.
        cluster.k_means_clust(rwData, 20, 10, True)
        
        assignments = []
        for v in cluster.assignments.values():
            for vv in v:
                assignments.append(vv)
        for i in range(150):
            if i not in assignments:
                print(i + 1)
                
    #     print(cluster.assignments)
    #     cluster.plot_centroids()
        print("num_cluster: " + str(num_cluster))
    
        try:
            cnx = pymysql.connect(user=username, password=password, host=host, database=dbname, autocommit=True)
            cursor = cnx.cursor()
            for key in cluster.assignments.keys():
                for value in cluster.assignments[key]:
#                     print(str(key) + " " + str(value+1))
                    cursor.execute('UPDATE logrw SET km' + str(num_cluster) + ' = %s WHERE id = %s', [key, value + 1])
        finally:
            if cursor:
                cursor.close()
            if cnx:
                cnx.close()
    
#         arima_row_ids = [2,5,6,9,12,15,17,18,19,36,39,48,56,58,71,75,80,86,103,114,129,140]
#         arima_cnt = {}
#         for key in cluster.assignments.keys():
#             arima_cnt[key] = 0
#         for arima_id in arima_row_ids:
#             for key in cluster.assignments.keys():
#                 if (arima_id-1) in cluster.assignments[key]:
#                     arima_cnt[key] = arima_cnt[key] + 1 
#                     break
#         print("cnt: " + str(sorted(arima_cnt.items())))
#         all_cnt = {}
#         for key in cluster.assignments.keys():
#             all_cnt[key] = 0
#         for key in cluster.assignments.keys():
#             all_cnt[key] = len(cluster.assignments[key]) 
#         print("all cnt: " + str(sorted(all_cnt.items())))