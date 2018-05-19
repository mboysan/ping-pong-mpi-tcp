#!/bin/bash
#SBATCH -N 2
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:01:00

module load jdk-1.8.0_25
module load openmpi-1.8.4

echo "osu_latency test with java mpi"
mpirun java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyMPI

echo "osu_latency test with java mpi on tcp"
mpirun --mca btl tcp,self java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencyMPI