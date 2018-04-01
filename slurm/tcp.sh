#!/bin/bash
 module load openmpi-2.1.0
 for nNODES in {1..2}; do
    for nTASKS in {1..3}; do
    np=$((${nNODES}*${nTASKS}))
    dir_name="tcp_${nNODES}_${nTASKS}"
    mkdir "${dir_name}"
    f_name="${dir_name}/tcp_${nNODES}_${nTASKS}.sh"
    f_name_wo_dir="tcp_${nNODES}_${nTASKS}.sh"
        touch ${f_name}
    echo "#!/bin/bash " >> ${f_name}
    echo "#SBATCH -p testing" >> ${f_name}
    echo "#SBATCH -N ${nNODES}" >> ${f_name}
    echo "#SBATCH --ntasks-per-node=${nTASKS}" >> ${f_name}
    echo "#SBATCH --cpus-per-task=4" >> ${f_name}
    echo "#SBATCH -t 00:00:30" >> ${f_name}
    cat tcp_body.sh >> ${f_name}

    cd ${dir_name}
        sbatch ${f_name_wo_dir}
    cd ..
    done
done