#!/bin/bash
#Run with sudo
apt-get -y install openjdk-8-jdk

touch /etc/profile.d/rohitsd.sh
echo "export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64" >> /etc/profile.d/rohitsd.sh

echo 'export LANGUAGE="en_US.UTF-8"' >> /etc/profile.d/rohitsd.sh
echo 'export LC_ALL="en_US.UTF-8"' >> /etc/profile.d/rohitsd.sh

export LANGUAGE="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"


source /etc/profile.d/rohitsd.sh

apt-get install -y tar wget git
apt-get install -y build-essential python-dev libcurl4-nss-dev libsasl2-dev libsasl2-modules maven libapr1-dev libsvn-dev zlib1g-dev
apt-get install -y zip libunwind-setjmp0-dev zlib1g-dev unzip