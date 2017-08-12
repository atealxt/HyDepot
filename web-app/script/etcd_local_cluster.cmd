rem https://github.com/coreos/etcd/blob/master/Documentation/op-guide/clustering.md
rem https://github.com/coreos/etcd/blob/master/Documentation/dev-guide/local_cluster.md

rem --------------------------------------------------------------------------
rem Start Server

etcd --name infra0 --initial-advertise-peer-urls http://127.0.0.1:2380 ^
  --listen-peer-urls http://127.0.0.1:2380 ^
  --listen-client-urls http://127.0.0.1:2379 ^
  --advertise-client-urls http://127.0.0.1:2379 ^
  --initial-cluster-token etcd-cluster-1 ^
  --initial-cluster infra0=http://127.0.0.1:2380,infra1=http://127.0.0.2:2382,infra2=http://127.0.0.3:2384 ^
  --initial-cluster-state new

etcd --name infra1 --initial-advertise-peer-urls http://127.0.0.2:2382 ^
  --listen-peer-urls http://127.0.0.2:2382 ^
  --listen-client-urls http://127.0.0.2:2381 ^
  --advertise-client-urls http://127.0.0.2:2381 ^
  --initial-cluster-token etcd-cluster-1 ^
  --initial-cluster infra0=http://127.0.0.1:2380,infra1=http://127.0.0.2:2382,infra2=http://127.0.0.3:2384 ^
  --initial-cluster-state new

etcd --name infra2 --initial-advertise-peer-urls http://127.0.0.3:2384 ^
  --listen-peer-urls http://127.0.0.3:2384 ^
  --listen-client-urls http://127.0.0.3:2383 ^
  --advertise-client-urls http://127.0.0.3:2383 ^
  --initial-cluster-token etcd-cluster-1 ^
  --initial-cluster infra0=http://127.0.0.1:2380,infra1=http://127.0.0.2:2382,infra2=http://127.0.0.3:2384 ^
  --initial-cluster-state new

rem --------------------------------------------------------------------------
rem Start Client
set ETCDCTL_API=3
etcdctl --write-out=table --endpoints=localhost:2379 member list