#! /bin/bash

basepath=$(cd `dirname $0`; pwd)
echo "Current path: $basepath"
echo "java Main $1"
java Main $1