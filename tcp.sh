#!/bin/sh

# build java application
mvn clean install

# copy tcp multi jvm script, TCPProcess will run this script to run each process in a separate JVM.
cp -avr ./tcp_multi_jvm.sh ./target/tcp_multi_jvm.sh

# go to target
cd ./target

nTasks=5
java -cp *jar-with-dependencies.jar TCPProcess $nTasks