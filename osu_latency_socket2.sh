#!/bin/sh

# Used to run the pt2pt osu_latency tests for the Java TCP Sockets.

mvn clean install

cd ./target
count=2
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencySocket2 1000
#mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencySocket2 750 250 2048 131072

# arguments (optional):
# arg1 = options.iterations
# arg2 = options.skip
# arg3 = min size
# arg4 = max size