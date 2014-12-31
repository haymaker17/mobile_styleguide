#!/bin/bash

export ANT_HOME=$PWD/../libs/apache-ant-1.9.4
export GRADLE_HOME=$PWD/../libs/gradle-1.11
export PATH=$PATH:$ANT_HOME/bin:$GRADLE_HOME/bin
export LOGS=$PWD/../logs
export CNQR_ANDROID=$PWD/../../

echo "Cleaning out logs..."
rm $LOGS/*concur*.logs
date >> $LOGS/concur-base-build.logs
date >> $LOGS/concur-base-javadocs.logs
date >> $LOGS/concur-base-junits.logs
date >> $LOGS/concur-platform-build.logs
date >> $LOGS/concur-platform-javadocs.logs
date >> $LOGS/concur-platform-junits.logs

cd $CNQR_ANDROID

echo "Building ConcurBase..."
cd base
ant clean >> $LOGS/concur-base-build.logs
ant debug >> $LOGS/concur-base-build.logs
echo "Generating ConcurBase Javadocs..."
ant javadoc >> $LOGS/concur-base-javadocs.logs
echo "Running ConcurBase JUnits..."
cd ../base_test
gradle build >> $LOGS/concur-base-junits.logs


echo "Building ConcurPlatform..."
cd ../platform
ant clean >> $LOGS/concur-platform-build.logs
ant debug >> $LOGS/concur-platform-build.logs
echo "Generating ConcurPlatform Javadocs..."
ant javadoc >> $LOGS/concur-platform-javadocs.logs
echo "Running ConcurPlatform JUnits..."
cd ../platform_test
gradle build >> $LOGS/concur-platform-junits.logs

echo "#### FINISHED!!! ####"
