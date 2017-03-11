#!/bin/bash

MASTER=cp-2.exp7.spark-heron-pg0.utah.cloudlab.us

apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
 
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | \
    tee /etc/apt/sources.list.d/mesosphere.list
apt-get -y update
 
# Use `apt-cache showpkg mesos | grep [version]` to find the exact version.
apt-get -y install mesos

service  mesos-master start

wget -c https://apache.bintray.com/aurora/ubuntu-trusty/aurora-scheduler_0.15.0_amd64.deb
sudo dpkg -i aurora-scheduler_0.15.0_amd64.deb

sudo -u aurora mkdir -p /var/lib/aurora/scheduler/db
sudo -u aurora mesos-log initialize --path=/var/lib/aurora/scheduler/db

sed -i '/ZK_ENDPOINTS="127.0.0.1:2181"/c\ZK_ENDPOINTS="'$MASTER':2181"' /etc/default/aurora-scheduler

sed -i '/"name": "example",/c\"name": "aurora",' /etc/aurora/clusters.json

service aurora-scheduler start

wget -c https://apache.bintray.com/aurora/ubuntu-trusty/aurora-tools_0.15.0_amd64.deb
dpkg -i aurora-tools_0.15.0_amd64.deb


