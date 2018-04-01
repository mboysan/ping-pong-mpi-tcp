#!/bin/bash

# The job should run on the testing partition
#SBATCH -p testing

# Required compute nodes
#SBATCH -N 1

# Required tasks per node
#SBATCH --ntasks-per-node=1

# Required CPUs per task
#SBATCH --cpus-per-task=4

# The maximum walltime of the job
#SBATCH -t 00:00:20

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
echo "------ LD_LIB_PATH"
echo $LD_LIBRARY_PATH
echo "------ Java program"

nNodes=$SLURM_NNODES
nTasks=$SLURM_NTASKS
tEnd=$(($nTasks-1))
#totalTasks=$(($nNodes*nTasks))

echo "nNodes: $SLURM_NNODES"
echo "nTasks: $SLURM_NTASKS"

echo "0-$tEnd java -cp ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar TCPMainMultiJVM $nTasks %t false" > tcp_config.sh

srun --multi-prog tcp_config.sh