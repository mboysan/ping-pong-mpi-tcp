#!/bin/bash 
#SBATCH -N 5
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:01:30
#SBATCH --mem=2048
module load jdk-1.8.0_25
module load openmpi-1.8.4
mpirun --mca btl tcp,self -np 5 java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar MPIMain 30005 false
