#!/bin/bash

cd ..
cd ./target

#SBATCH -N 1
#SBATCH --ntasks-per-node=1

module load openmpi-3.0.0

echo "slurm/mpi init"
mpirun -np 1 java -cp *jar-with-dependencies.jar MPIMain $@
echo "slurm/mpi end"