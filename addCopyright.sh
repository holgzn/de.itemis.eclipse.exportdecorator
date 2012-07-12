#!/bin/bash

srcfolder="src/de/itemis"
copyright="copyright.txt"


copyrightlength=$(cat $copyright | wc -l)
temp=$(mktemp -t addCopyright)
for i in $(find $srcfolder -name '*.java'); do 
	head -n $copyrightlength $i > $temp  
	diff=$(diff -B $temp copyright.txt | wc -l) 
	if [ ! $diff -eq 0 ]; then
		echo $i
		cat copyright.txt $i >$i.new && mv $i.new $i
	fi
done
rm -f $temp
