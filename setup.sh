#!/bin/bash

DOWNLOAD_DIR=/tmp/download
SERVICES_DIR=/tmp/services

#SPARK variables
SPARK_DIR=$SERVICES_DIR/spark-2.0.1-bin-hadoop2.7
SPARK_MASTER_NODE_NAME=$(hostname -A | tr -d ' ')
SPARK_MASTER_PORT=7077

#Kafka variables
KAFKA_DIR=$SERVICES_DIR/kafka_2.11-0.10.1.0/
PARTITIONS=1
TOPIC=test

#Zookeeper variables
#ZK_HOST="rockhopper-07.cs.wisc.edu"
ZK_HOST="localhost"
ZK_PORT="2181"
ZK_CONNECTIONS="$ZK_HOST:$ZK_PORT"

cleanup() {
    rm -rf $DOWNLOAD_DIR
    rm -rf $SERVICES_DIR
    rm -rf ~/.herondata

    mkdir $DOWNLOAD_DIR
    mkdir $SERVICES_DIR
}

setup_spark() {
    echo "Downloading Spark"
    wget -q -O $DOWNLOAD_DIR/spark-2.0.1-bin-hadoop2.7.tgz http://d3kbcqa49mib13.cloudfront.net/spark-2.0.1-bin-hadoop2.7.tgz

    echo "Extracting Spark"
    tar -xzf $DOWNLOAD_DIR/spark-2.0.1-bin-hadoop2.7.tgz -C $SERVICES_DIR/

    echo "Done - Spark"
}

setup_kafka() {
    echo "Downloading Kafka"
    wget -q -O $DOWNLOAD_DIR/kafka_2.11-0.10.1.0.tgz http://ftp.wayne.edu/apache/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz

    echo "Extracting Kafka"
    tar -xzf $DOWNLOAD_DIR/kafka_2.11-0.10.1.0.tgz -C $SERVICES_DIR/

    echo "Done - Kafka"
}

setup_heron() {
    echo "Downloading Heron"
    wget -q -O $DOWNLOAD_DIR/heron-client-install-0.14.5-ubuntu.sh https://github.com/twitter/heron/releases/download/0.14.5/heron-client-install-0.14.5-ubuntu.sh 
    wget -q -O $DOWNLOAD_DIR/heron-tools-install-0.14.5-ubuntu.sh https://github.com/twitter/heron/releases/download/0.14.5/heron-tools-install-0.14.5-ubuntu.sh 

    echo "Installing Heron"
    chmod +x $DOWNLOAD_DIR/heron-client-install-0.14.5-ubuntu.sh
    chmod +x $DOWNLOAD_DIR/heron-tools-install-0.14.5-ubuntu.sh
    $DOWNLOAD_DIR/heron-client-install-0.14.5-ubuntu.sh --prefix=/tmp/services/Heron --heronrc=/tmp/services/Heron/heron/etc/heron.heronrc
    $DOWNLOAD_DIR/heron-tools-install-0.14.5-ubuntu.sh --prefix=/tmp/services/Heron 
    export PATH=/tmp/services/Heron/bin:$PATH
    echo "Done - Heron"

}

setup() {
    #Clean UP
    cleanup

    setup_kafka
    setup_spark
    setup_heron
}

create_kafka_topic() {
    local count=`$KAFKA_DIR/bin/kafka-topics.sh --describe --zookeeper "$ZK_CONNECTIONS" --topic $TOPIC 2>/dev/null | grep -c $TOPIC`
    if [[ "$count" = "0" ]];
    then
        $KAFKA_DIR/bin/kafka-topics.sh --create --zookeeper "$ZK_CONNECTIONS" --replication-factor 1 --partitions $PARTITIONS --topic $TOPIC
    else
        echo "Kafka topic $TOPIC already exists"
    fi
}

start_kafka_service() {
    echo "Starting Zookeeper"
    $KAFKA_DIR/bin/zookeeper-server-start.sh -daemon $KAFKA_DIR/config/zookeeper.properties
    echo "Starting Kafka"
    $KAFKA_DIR/bin/kafka-server-start.sh -daemon $KAFKA_DIR/config/server.properties
}

start_spark_service() {
    echo "Starting Spark Master"
    $SPARK_DIR/sbin/start-master.sh
    echo "Starting Spark Slave with MASTER $SPARK_MASTER_NODE_NAME:$SPARK_MASTER_PORT"
    local SPARK_MASTER=$SPARK_MASTER_NODE_NAME:$SPARK_MASTER_PORT
    $SPARK_DIR/sbin/start-slave.sh "spark://"$SPARK_MASTER
}

stop_services() {
    echo "Stopping Spark Master"
    $SPARK_DIR/sbin/stop-master.sh
    
    echo "Stopping Spark Slave"
    $SPARK_DIR/sbin/stop-slave.sh
    
    echo "Stopping Kafka"
    #$KAFKA_DIR/bin/kafka-server-stop.sh
    ps ax | grep -i 'kafka' | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM

    echo "Stopping Zookeeper"
    ps ax | grep -i 'kafka' | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM
    #$KAFKA_DIR/bin/zookeeper-server-stop.sh
}


main() {
    # setup
    local action=$1
    if [ "SETUP" = "$action" ];
    then
      if [ $# -eq 2 ];
      then
      cleanup
        for arg in "${@}"; do
          case $arg in 
            SETUP)
            ;;
            HERON)
          setup_heron
            ;;
            SPARK)
          setup_spark
            ;;
            KAFKA)
          setup_kafka
            ;;
            *)
            echo "Invalid Argument \"$arg\" to SETUP ";
            exit
            ;;
          esac
        done
      else
    setup
      fi
    elif [ "START_SV" = "$action" ];
    then
	start_kafka_service
	create_kafka_topic
	start_spark_service
    elif [ "STOP_SV" = "$action" ];
    then
	stop_services
    elif [ "CLEANUP" = "$action" ];
    then
	cleanup
    fi
}

help() {
    # echo $SPARK_MASTER_NODE_NAME
    echo "Starting Spark Slave with MASTER $SPARK_MASTER_NODE_NAME:$SPARK_MASTER_PORT"
    echo "./setup.sh [SETUP [SPARK, KAFKA, HERON], START_SV, STOP_SV, CLEANUP]"
}

if [ $# -gt 0 ];
then
    main ${@}
else
    help
fi
