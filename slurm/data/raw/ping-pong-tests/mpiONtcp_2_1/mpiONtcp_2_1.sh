#!/bin/bash 
#SBATCH -N 2
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:01:00
module load jdk-1.8.0_25
module load openmpi-1.8.4
mpirun --mca btl tcp,self -np 2 java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar MPIMain 1 false
