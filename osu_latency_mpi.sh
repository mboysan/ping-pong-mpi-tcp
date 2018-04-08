#!/bin/sh

# Used to run pt2pt osu_latency tests with mpi.

# build java application
mvn clean install

# go to target and run MPIMain class
cd ./target
count=2
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyMPI

# Comment the above command and uncomment the following to run mpi on TCP/IP stack.
#mpirun --oversubscribe --mca btl tcp,self -np $count java -cp *jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyMPI

# arguments (optional):
# arg1 = options.iterations
# arg2 = options.skip