#!/bin/sh

# build java application
mvn clean install

# go to target and run MPIMain class
cd ./target
count=2
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyMPI
#mpirun --oversubscribe --mca btl tcp,self -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyMPI