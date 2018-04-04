#!/bin/sh

mvn clean install
# run a single process

cd ./target
count=2
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyTCP