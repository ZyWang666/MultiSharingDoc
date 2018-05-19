# CSE223B-Project
To compile: 
    $ mvn compile

To build executable: 
    $ mvn package

To compile and run tests:
    $ mvn install

To run server:
    $ sh target/bin/webapp

To clean:
    $ mvn clean

To install without tests:
    $ mvn install -DskipTests

To deploy local lib/repo:
    $ mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=<PATH-TO-JAR-FILE> -DgroupId=org.ahmadsoft -DartifactId=ropes -Dversion=1.2.5 -Dpackaging=jar -DlocalRepositoryPath=<PATH-TO-LIB-DIRECTORY>