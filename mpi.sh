#!/bin/sh

# build java application
mvn clean install

# go to target and run MPIMain class
cd ./target
count=1
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar MPIMain 2 true