#!/bin/bash

# The job should run on the testing partition
#SBATCH -p testing

# Required compute nodes
#SBATCH -N 1

# Required tasks per node
#SBATCH --ntasks-per-node=1

# The maximum walltime of the job
#SBATCH -t 00:00:10

#module load openmpi-2.1.0
module load openmpi-1.8.4
module load jdk-1.8.0_25

echo "-------------- uname -r"
uname -r
echo "-------------- hostname"
hostname
echo "-------------- hostname -I"
hostname -I
echo "-------------- ifconfig"
ifconfig
echo "-------------- curl ifconfig.co"
curl ifconfig.co
echo "-------------- JOB"
java -cp *-jar-with-dependencies.jar TCPMainSingleJVM

#mpirun -np 1 java -cp *jar-with-dependencies.jar MPIMain $@