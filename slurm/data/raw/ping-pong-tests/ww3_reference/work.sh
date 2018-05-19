#!/bin/sh

nodeCount=10

for i in `seq 1 10`; do
    id=$((i*nodeCount))
    echo "id set -> $id"

    mkdir ww$i
    cp -avr ./*.sh ./ww$i
    cp -avr ./*.jar ./ww$i
    
    cd ww$i
    echo "running... $(pwd)/all.sh"
    ./all.sh $id
    cd ..
done
