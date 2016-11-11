#!/bin/bash

DOWNLOAD_DIR=download
SERVICES_DIR=services

#Clean UP
rm -rf $DOWNLOAD_DIR
rm -rf $SERVICES_DIR

mkdir $DOWNLOAD_DIR
mkdir $SERVICES_DIR

echo "Downloading Spark"
wget -q -O $DOWNLOAD_DIR/spark-2.0.1-bin-hadoop2.7.tgz http://d3kbcqa49mib13.cloudfront.net/spark-2.0.1-bin-hadoop2.7.tgz

echo "Extracting Spark"
tar -xzf $DOWNLOAD_DIR/spark-2.0.1-bin-hadoop2.7.tgz -C $SERVICES_DIR/

echo "Done - Spark"

echo "Downloading Kafka"
wget -q -O $DOWNLOAD_DIR/kafka_2.11-0.10.1.0.tgz http://ftp.wayne.edu/apache/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz

echo "Extracting Kafka"
tar -xzf $DOWNLOAD_DIR/kafka_2.11-0.10.1.0.tgz -C $SERVICES_DIR/

echo "Done - Kafka"
