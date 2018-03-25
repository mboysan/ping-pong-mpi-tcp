#!/bin/sh

# build java application
mvn clean install

# go to target and run MPIMain class
cd ./target
mpirun --oversubscribe -np 3 java -cp *jar-with-dependencies.jar MPIMain $@