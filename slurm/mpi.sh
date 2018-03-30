#!/bin/sh

cd ..
cd ./target

echo "slurm/mpi init"
mpirun -np 1 java -cp *jar-with-dependencies.jar MPIMain $@
echo "slurm/mpi end"