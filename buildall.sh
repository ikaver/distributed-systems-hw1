cd ds-hw1-shared
mvn clean install
cd ..
cd ds-hw1-processrunner
mvn clean
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
cd ..
cd ds-hw1-nodemanager
mvn clean
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
cd ..
