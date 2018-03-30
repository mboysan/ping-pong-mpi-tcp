#!/bin/bash

cd ..
cd ./target

# The job should run on the testing partition
#SBATCH -p testing

# Required compute nodes
#SBATCH -N 1

# Required tasks per node
#SBATCH --ntasks-per-node=1

# The maximum walltime of the job
#SBATCH -t 00:00:10

module load openmpi-2.1.0

echo "slurm/mpi init"
mpirun -np 1 java -cp *jar-with-dependencies.jar MPIMain $@
echo "slurm/mpi end"