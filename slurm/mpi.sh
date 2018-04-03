#!/bin/bash
 module load openmpi-1.8.4
 gId=0
 for nNODES in {1..5}; do
    for nTASKS in {1..1}; do
    gId=$((gId+1))
    np=$((${nNODES}*${nTASKS}))
    dir_name="m0mpi_${nNODES}_${nTASKS}"
    mkdir "${dir_name}"
    f_name="${dir_name}/mpi_${nNODES}_${nTASKS}.sh"
    f_name_wo_dir="mpi_${nNODES}_${nTASKS}.sh"
        touch ${f_name}
    echo "#!/bin/bash " >> ${f_name}
#    echo "#SBATCH -p testing" >> ${f_name}
    echo "#SBATCH -N ${nNODES}" >> ${f_name}
    echo "#SBATCH --ntasks-per-node=${nTASKS}" >> ${f_name}
    echo "#SBATCH --cpus-per-task=4" >> ${f_name}
    echo "#SBATCH -t 00:01:00" >> ${f_name}
    echo "#SBATCH --mem=25000" >> ${f_name}
    echo "module load jdk-1.8.0_25" >> ${f_name}
    echo "module load openmpi-1.8.4" >> ${f_name}
    echo "mpirun -np ${np} java -cp ../ping-pong-mpi-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar MPIMain ${gId} false" >> ${f_name}

    cd ${dir_name}
        sbatch ${f_name_wo_dir}
    cd ..
    done
done