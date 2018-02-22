import matplotlib.pylab as plt
import numpy as np
import random
import pymysql
import rw_predict_linreg
import rw_predict_arima

class ts_cluster(object):
    def __init__(self,colors):
        '''
        num_clust is the number of clusters for the k-means algorithm
        assignments holds the assignments of data points (indices) to clusters
        centroids holds the centroids of the clusters
        '''
        self.colors = colors
        self.num_clust=len(colors)
        self.assignments={}
        self.centroids=[]
        
    def k_means_clust(self,data,num_iter,w,progress=False):
        '''
        k-means clustering algorithm for time series data.  dynamic time warping Euclidean distance
         used as default similarity measure. 
        '''
        self.centroids=random.sample(list(data),self.num_clust)
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
            line = plt.plot(item, label='Central ' + str(idx), color=self.colors[idx])
        plt.legend()
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
        rows = cursor.fetchmany(100)
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

    clusters = ['red', 'blue', 'green', 'cyan', 'orange', 'purple', 'black']
    cluster = ts_cluster(clusters[0:3])
    cluster.k_means_clust(rwData, 10, 4, True)
    cluster.plot_centroids()

#     roundedCentroids = np.around(cluster.centroids)
    # test predict methods for the 3 cluster
#     for x in roundedCentroids:
#         print(x)
#         x = x.reshape(-1,1)
#         print("try linear regression...")
#         rw_predict_linreg.predict(x, stopIfFound=True)
#         print("try arima...")
#         rw_predict_arima.predict(x, stopIfFound=True)




