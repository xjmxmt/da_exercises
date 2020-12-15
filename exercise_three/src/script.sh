#! /bin/bash

if [ $1 -eq "0" ]
then
  int=0
  while(( $int<=9 ))
  do
    echo "java AfekGafniMain 0 $int" &
    java AfekGafniMain 0 $int &
    let "int++"
  done
  exit 1
elif [ $1 -eq "1" ]
then
  int=0
  while(( $int<=9 ))
  do
    echo "java AfekGafniMain 1 $int" &
    java AfekGafniMain 1 $int &
    let "int++"
  done
  exit 1
elif [ $1 -eq "2" ]
then
  int=0
  while(( $int<=9 ))
  do
    echo "java AfekGafniMain 2 $int" &
    java AfekGafniMain 2 $int &
    let "int++"
  done
  exit 1
elif [ $1 -eq "3" ]
then
  int=0
  while(( $int<=4 ))
  do
    echo "java AfekGafniMain 3 $int" &
    java AfekGafniMain 3 $int &
    let "int++"
  done
  exit 1
fi