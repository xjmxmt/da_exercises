#! /bin/bash

if [ $1 -eq "3" ]
then
  int=0
  while(( $int<=2 ))
  do
    echo "java MaekawaMain 3 requestset_3.txt $int" &
    java MaekawaMain 3 requestset_3.txt $int &
    let "int++"
  done
elif [ $1 -eq "7" ]
then 
  int=0
  while(( $int<=6 ))
  do
    echo "java MaekawaMain 7 requestset_7.txt $int" &
    java MaekawaMain 7 requestset_7.txt $int &
    let "int++"
  done
fi
exit 1