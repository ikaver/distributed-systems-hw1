#!/bin/bash
#Runs a slave node on the given port. Also, start the rmi registry on the same port.
#Usage sh runslave.sh <jar-file> <port-number>

[ $# -lt 2 ] && { echo "Usage: $1 jar file $2 port number"; exit 1; }

export CLASSPATH="$CLASSPATH:$1"
echo "USING class path: $CLASSPATH"
fuser -k $2/tcp
rmiregistry $2 &
java -jar "$1" $2
