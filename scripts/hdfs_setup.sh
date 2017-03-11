#!/bin/bash

mkdir /users/rohitsd/hdfs
mkdir /users/rohitsd/hdfs_nn_dirs
mkdir /users/rohitsd/hdfs_dn_dirs

hdfs --config $HADOOP_CONF_DIR namenode -format exp7

hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start namenode
hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode
