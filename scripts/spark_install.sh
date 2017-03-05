#!/bin/bash

setup_spark() {
    echo "Downloading Spark"
    wget http://d3kbcqa49mib13.cloudfront.net/spark-2.0.1-bin-hadoop2.7.tgz

    echo "Extracting Spark"
    tar -xzf spark-2.0.1-bin-hadoop2.7.tgz

    echo "Done - Spark"
}

