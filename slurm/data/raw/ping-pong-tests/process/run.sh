#!/bin/sh
#SBATCH -N 1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=2
#SBATCH -t 00:15:00
#SBATCH --mem=2048

module load R-3.4.1

cd ..

workingDir=ww
curDir=$(pwd)
echo $curDir

for dir in "$curDir"/"$workingDir"*/;
do
  	cd $dir
        cp -v ../process/csv_produce.sh $dir
        cp -v ../process/datamine01.R $dir
        ./csv_produce.sh
        Rscript ./datamine01.R
done

cd $curDir
Rscript ./process/process_all.R
