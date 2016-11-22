KAFKA_BIN=/tmp/services/kafka_2.11-0.10.1.0/bin

create_topic() {
if [ "local" = "$1" ];
then
host=`hostname --long`
else
host="$1"
fi
$KAFKA_BIN/kafka-topics.sh --create --zookeeper $host --topic $2 --replication-factor $3 --partitions $4
}

delete_topic() {
if [ "local" = "$1" ];
then
host=`hostname --long`
else
host="$1"
fi
$KAFKA_BIN/kafka-topics.sh --zookeeper $host -delete --topic $2
}

feed_kafka() {
if [ "local" = "$1" ];
then
host="`hostname --long`:9092"
else
host="$1"
fi
$KAFKA_BIN/kafka-console-producer.sh --broker-list $host --topic $2 < $3
}



if [ $# -lt 2 ];
then
echo "kafka_services.sh --[create-topic, delete-topic, feed-topic] [zookeeper host] [topic name]"
echo "e.g. kafka_services.sh --create-topic local test 1 1;
# here first '1' represents replication factor and the second '1' represents partitions. 'local' represents the localhost. Any other host can also be given"
echo "kafka_services.sh --delete-topic local test"
echo "kafka_services.sh --feed-topic local test file_name. # here 9092 is the port and the file_name is the file which is fed as input to the kafka"
exit
fi

if [ "--create-topic" = "$1" ];
then
  if [ $# -ne 5 ];
  then
  exit;
  fi
  create_topic $2 $3 $4 $5
exit
fi

if [ "--delete-topic" = "$1" ];
then
  if [ $# -ne 3 ];
  then
  exit;
  fi
  delete_topic $2 $3
exit
fi

if [ "--feed-topic" = "$1" ];
then
  if [ $# -ne 4 ];
  then
  exit;
  fi
  feed_kafka $2 $3 $4
exit
fi
