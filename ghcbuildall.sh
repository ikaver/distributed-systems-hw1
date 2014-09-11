cd ds-hw1-shared
/usr/local/lib/netbeans/java/maven/bin/mvn clean install
cd ..
cd ds-hw1-processrunner
/usr/local/lib/netbeans/java/maven/bin/mvn clean
/usr/local/lib/netbeans/java/maven/bin/mvn assembly:assembly -DdescriptorId=jar-with-dependencies
cd ..
cd ds-hw1-nodemanager
/usr/local/lib/netbeans/java/maven/bin/mvn clean
/usr/local/lib/netbeans/java/maven/bin/mvn assembly:assembly -DdescriptorId=jar-with-dependencies
cd ..
