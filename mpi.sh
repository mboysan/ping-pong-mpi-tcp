#!/bin/sh

# build java application
mvn clean install

# go to target and run MPIMain class
cd ./target
loopCount=1
for i in {1..$loopCount}
do
#    count=$((i * 10))
    count=3
    mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar MPIMain $@
done