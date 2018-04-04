#!/bin/bash 
#SBATCH -N 3
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=4
#SBATCH -t 00:01:00
#SBATCH --mem=25000
multicastPort=9093
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

echo "0-$tEnd java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar TCPMainMultiJVM $nTasks %t false $multicastPort" > tcp_config.sh

srun --multi-prog tcp_config.sh