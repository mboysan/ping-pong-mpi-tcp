#!/bin/sh

# used to run the tests multiple times. creates subdirectories with the names
# provided with the workingDir parameter. Copies the other scripts and the
# jar file to those directories and runs the tests all together.

workingDir=tt
nodeCount=10

for i in `seq 1 50`; do
    id=$((i*nodeCount))
    echo "id set -> $id"

    mkdir "$workingDir"$i
    cp -avr ./*.sh ./"$workingDir"$i
    cp -avr ./*.jar ./"$workingDir"$i
    
    cd "$workingDir"$i
    echo "running... $(pwd)/all.sh"
    ./all.sh $id
    cd ..
done
