#!/bin/bash

# clear the total.csv
cat /dev/null > total.csv

# combine all the files keeping the header row from the first file only
counter=0
for FILE in $(ls jmeter* -1); do 
  if [ $counter == 0 ]; then
    # cat the file including the header--first time only
    cat $FILE >> total.csv
  else 
    # cat the file ignoring the header
    tail --lines=+2 $FILE >> total.csv
  fi
  counter=$((counter+1))
done

# summarise the result
echo "$counter files combined:"
head -5 total.csv
echo "..."
echo "..."
echo "..."
tail -6 total.csv
