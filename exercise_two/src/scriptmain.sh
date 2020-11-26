#! /bin/bash

basepath=$(cd `dirname $0`; pwd)
echo "Current path: $basepath"

echo "$1"
if [ $1 -eq "3" ]
then
  echo "java Main 3"
  java Main 3
elif [ $1 -eq "7" ]
then
  echo "java Main 7"
  java Main 7
else
  echo "WRONG ARGUMENT, SHOULD BE 3 OR 7."
  exit 1
fi