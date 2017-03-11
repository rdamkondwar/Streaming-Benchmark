#!/bin/bash

mkdir /users/rohitds/hdfs
mkdir /users/rohitds/hdfs_nn_dirs
mkdir /users/rohitds/hdfs_dn_dirs

hdfs --config $HADOOP_CONF_DIR namenode -format exp7

hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs start namenode
hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode
