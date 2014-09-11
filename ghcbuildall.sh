cd ds-hw1-shared
/usr/local/netbeans-7.2.1/java/maven/bin/mvn clean install
cd ..
cd ds-hw1-processrunner
/usr/local/netbeans-7.2.1/java/maven/bin/mvn clean
/usr/local/netbeans-7.2.1/java/maven/bin/mvn assembly:assembly -DdescriptorId=jar-with-dependencies
cd ..
cd ds-hw1-nodemanager
/usr/local/netbeans-7.2.1/java/maven/bin/mvn clean
/usr/local/netbeans-7.2.1/java/maven/bin/mvn assembly:assembly -DdescriptorId=jar-with-dependencies
cd ..
