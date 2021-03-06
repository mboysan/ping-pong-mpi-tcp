#!/bin/bash
#SBATCH -N 2
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:01:00

module load jdk-1.8.0_25
module load openmpi-1.8.4

echo "osu_latency test with java socket (connect every time when sending data) on TCP"
mpirun java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencySocket

echo "osu_latency test with java socket (connection always open) on TCP"
mpirun java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar testframework/osu/pt2pt/OSULatencySocket2