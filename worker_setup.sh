#!/bin/bash

MASTER="cp-2"
#echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle
#export PATH=/usr/lib/jvm/java-8-oracle/jre/bin:\$PATH" >> ~/.bashrc

#source ~/.bashrc
apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
 
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | \
  tee /etc/apt/sources.list.d/mesosphere.list
apt-get -y update
 
# Use `apt-cache showpkg mesos | grep [version]` to find the exact version.
apt-get -y install mesos

echo "zk://$MASTER:2181/mesos" > /etc/mesos/zk

service mesos-slave start

apt-get install -y python2.7 wget
 
# NOTE: This appears to be a missing dependency of the mesos deb package and is needed
# for the python mesos native bindings.
#sudo apt-get -y install libcurl4-nss-dev
 
wget -c https://apache.bintray.com/aurora/ubuntu-trusty/aurora-executor_0.15.0_amd64.deb
dpkg -i aurora-executor_0.15.0_amd64.deb

#apt-get install zip libunwind-setjmp0-dev zlib1g-dev unzip -y

service thermos start
