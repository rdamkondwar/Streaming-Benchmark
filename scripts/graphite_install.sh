#!/bin/bash
touch /etc/profile.d/rohitsd.sh


#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
source /etc/profile.d/rohitsd.sh

apt-get -y install python-dev libcairo2-dev libffi-dev
apt-get -y install python-pip

pip install scandir
export PYTHONPATH="/opt/graphite/lib/:/opt/graphite/webapp/"
pip install https://github.com/graphite-project/whisper/tarball/master
pip install https://github.com/graphite-project/carbon/tarball/master
pip install https://github.com/graphite-project/graphite-web/tarball/master


echo "export GRAPHITE_ROOT=/opt/graphite" >> /etc/profile.d/rohitsd.sh
export GRAPHITE_ROOT=/opt/graphite

PYTHONPATH=$GRAPHITE_ROOT/webapp django-admin.py migrate --settings=graphite.settings --run-syncdb
PYTHONPATH=$GRAPHITE_ROOT/webapp django-admin.py collectstatic --noinput --settings=graphite.settings

cp $GRAPHITE_ROOT/conf/carbon.conf.example $GRAPHITE_ROOT/conf/carbon.conf
cp $GRAPHITE_ROOT/conf/storage-schemas.conf.example $GRAPHITE_ROOT/conf/storage-schemas.conf
cp $GRAPHITE_ROOT/conf/graphite.wsgi.example $GRAPHITE_ROOT/conf/graphite.wsgi
cp $GRAPHITE_ROOT/conf/aggregation-rules.conf.example $GRAPHITE_ROOT/conf/aggregation-rules.conf
cp $GRAPHITE_ROOT/webapp/graphite/local_settings.py.example $GRAPHITE_ROOT/webapp/graphite/local_settings.py

#Run sudo $GRAPHITE_ROOT/run-graphite-devel-server.py /opt/graphite