#!/bin/sh

# build java application
mvn clean install

# go to target and run TCPMain class
cd ./target
loopCount=10
for i in {1..$loopCount}
do
    count=$((i * 10))
    java -cp *jar-with-dependencies.jar TCPMain $count
done