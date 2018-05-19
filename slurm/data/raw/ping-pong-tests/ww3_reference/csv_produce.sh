#!/bin/sh

for file in ./*/*.out
do
	dir=$(dirname "$file")
	cat "$file" | grep "pingAllIntermediate,full-load" > "$dir"/rdtrp.csv
	cat "$file" | grep "pingSingle," > "$dir"/p2p.csv
done