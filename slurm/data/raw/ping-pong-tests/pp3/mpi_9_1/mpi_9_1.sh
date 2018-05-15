#!/bin/bash 
#SBATCH -N 9
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:02:30
#SBATCH --mem=2048
module load jdk-1.8.0_25
module load openmpi-1.8.4
mpirun -np 9 java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar MPIMain 9 false
