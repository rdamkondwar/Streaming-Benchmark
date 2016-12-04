MASTER="cp-1.cluster.spark-heron-pg0.utah.cloudlab.us"
echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export PATH=/usr/lib/jvm/java-8-oracle/jre/bin:\$PATH" >> ~/.bashrc

source ~/.bashrc
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
 
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | \
  sudo tee /etc/apt/sources.list.d/mesosphere.list
sudo apt-get -y update
 
# Use `apt-cache showpkg mesos | grep [version]` to find the exact version.
sudo apt-get -y install mesos

sudo echo "zk://$MASTER:2181/mesos" > /etc/mesos/zk

sudo service mesos-slave start

sudo apt-get install -y python2.7 wget
 
# NOTE: This appears to be a missing dependency of the mesos deb package and is needed
# for the python mesos native bindings.
sudo apt-get -y install libcurl4-nss-dev
 
wget -c https://apache.bintray.com/aurora/ubuntu-trusty/aurora-executor_0.15.0_amd64.deb
sudo dpkg -i aurora-executor_0.15.0_amd64.deb

sudo service thermos start
