#!/bin/bash

# Used to run the Java socket related tests. Uses the ./socket_body.sh script to inject its contents with the
# variables resolved at runtime.

 module load openmpi-2.1.0
 multicastPort=9090
 for nNODES in {1..5}; do
    for nTASKS in {1..1}; do
    multicastPort=$((multicastPort+1))
    np=$((${nNODES}*${nTASKS}))
    dir_name="mtcp_${nNODES}_${nTASKS}"
    mkdir "${dir_name}"
    f_name="${dir_name}/tcp_${nNODES}_${nTASKS}.sh"
    f_name_wo_dir="tcp_${nNODES}_${nTASKS}.sh"
        touch ${f_name}
    echo "#!/bin/bash " >> ${f_name}
#    echo "#SBATCH -p testing" >> ${f_name}
    echo "#SBATCH -N ${nNODES}" >> ${f_name}
    echo "#SBATCH --ntasks-per-node=${nTASKS}" >> ${f_name}
    echo "#SBATCH --cpus-per-task=4" >> ${f_name}
    echo "#SBATCH -t 00:01:00" >> ${f_name}
    echo "#SBATCH --mem=25000" >> ${f_name}
    echo "multicastPort=$multicastPort" >> ${f_name}
    cat tcp_body.sh >> ${f_name}

    cd ${dir_name}
        sbatch ${f_name_wo_dir}
    cd ..
    done
done