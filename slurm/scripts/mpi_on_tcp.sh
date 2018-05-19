#!/bin/bash

# Used to run the mpi related tests on InfiniBand interconnect. Modify this script to run the mpi application
# on TCP/IP stack. Also modify the location of the *.jar file for your needs.

gId=0
if [ $# -eq 1 ]
  then
    gId=$1
fi
gId=$((gId*1000))

 for nNODES in {1..10}; do
    for nTASKS in {1..1}; do
    gId=$((gId+1))
    echo "job _mpiontcp_ with gid: $gId"
    np=$((${nNODES}*${nTASKS}))
    dir_name="mpiontcp500_${nNODES}_${nTASKS}"
    mkdir "${dir_name}"
    f_name="${dir_name}/mpi_${nNODES}_${nTASKS}.sh"
    f_name_wo_dir="mpi_${nNODES}_${nTASKS}.sh"
        touch ${f_name}
    echo "#!/bin/bash " > ${f_name}
#    echo "#SBATCH -p testing" >> ${f_name}
    echo "#SBATCH -N ${nNODES}" >> ${f_name}
    echo "#SBATCH --ntasks-per-node=${nTASKS}" >> ${f_name}
    echo "#SBATCH --cpus-per-task=4" >> ${f_name}
    echo "#SBATCH -t 00:01:30" >> ${f_name}
    echo "#SBATCH --mem=1024" >> ${f_name}
    echo "module load jdk-1.8.0_25" >> ${f_name}
    echo "module load openmpi-1.8.4" >> ${f_name}
    echo "mpirun --mca btl tcp,self -np ${np} java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar MPIMain ${gId} false" >> ${f_name}

    cd ${dir_name}
        sbatch ${f_name_wo_dir}
    cd ..
    done
done
