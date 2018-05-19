#!/bin/sh

# deletes the jar files copied to the subdirectories. Run this script after 
# the tests are completed.

baseDir=tt

for i in `seq 1 1000`; do
    if [ -d "$baseDir"$i ]; then
        cd "$baseDir"$i
        rm -vr *.jar
        cd ..
    fi
done


