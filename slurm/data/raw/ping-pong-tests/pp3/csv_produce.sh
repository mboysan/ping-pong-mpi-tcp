#!/bin/sh

for file in ./*/*.out
do
	cat "$file" | grep "pingAllIntermediate,full-load" > ./"$file"_rdtrp.csv
	cat "$file" | grep "pingSingle," > ./"$file"_p2p.csv
done
