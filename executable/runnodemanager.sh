#!/bin/bash
#Runs the node manager.
#Usage sh runnodemanager.sh <jar-file>
[ $# -lt 1 ] && { echo "Usage: $1 jar file"; exit 1; }
java -jar "$1"
