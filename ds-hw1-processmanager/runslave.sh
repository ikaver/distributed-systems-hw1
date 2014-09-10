
#Runs a slave node on the given port. Also, start the rmi registry on the same port.
#Must be run from inside package-manager package
#Usage sh runslave.sh <port-number>
fuser -k $1/tcp
rmiregistry $1 &
mvn exec:exec -DportNumber=$1
