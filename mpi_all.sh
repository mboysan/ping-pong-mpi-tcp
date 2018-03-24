#!/bin/sh

# build java application
if [ "compile=true" = "$1" ]; then
    mvn clean install
fi

# go to target and run mpi.MPIMain class
cd ./target
mpirun --oversubscribe -np 3 java -cp *jar-with-dependencies.jar mpi/MPIMain $@