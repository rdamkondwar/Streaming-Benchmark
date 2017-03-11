#!/bin/bash

setup_spark() {
    echo "Downloading Spark"
    wget http://d3kbcqa49mib13.cloudfront.net/spark-2.0.1-bin-hadoop2.7.tgz

    echo "Extracting Spark"
    tar -xzf spark-2.0.1-bin-hadoop2.7.tgz

    echo "Done - Spark"
}
setup_spark

cp props/metrics.properties spark-2.0.1-bin-hadoop2.7/conf/metrics.properties

mkdir /users/rohitsd/hdfs
mkdir /users/rohitsd/hdfs_nn_dirs
mkdir /users/rohitsd/hdfs_dn_dirs
