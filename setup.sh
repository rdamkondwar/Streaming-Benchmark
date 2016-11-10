#!/bin/bash

#Clean UP
rm -rf download
rm -rf services

mkdir download
mkdir services

echo "Downloading Spark"
wget -q -O download/spark-2.0.1-bin-hadoop2.7.tgz http://d3kbcqa49mib13.cloudfront.net/spark-2.0.1-bin-hadoop2.7.tgz

echo "Extracting Spark"
tar -xzf download/spark-2.0.1-bin-hadoop2.7.tgz -C services/

echo "Done - Spark"

echo "Downloading Kafka"
wget -q -O download/ http://ftp.wayne.edu/apache/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz

echo "Extracting Kafka"
tar -xzf download/kafka_2.11-0.10.1.0.tgz -C services/

echo "Done - Kafka"
