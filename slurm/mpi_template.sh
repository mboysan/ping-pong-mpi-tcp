#!/bin/bash
#SBATCH -p testing
#SBATCH -N 1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:00:10
#SBATCH --mem=25000
module load jdk-1.8.0_25
module load openmpi-1.8.4
mpirun -np 1 java -cp ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar MPIMain 1 false