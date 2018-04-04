#!/bin/sh

mvn clean install
# run a single process

cd ./target
count=3
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar TCPMainMultiJVM $count 0 false