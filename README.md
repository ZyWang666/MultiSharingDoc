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
    $ mvn deploy:deploy-file -Durl=file:lib/ -Dfile=ropes.jar -DgroupId=org.ahmadsoft.ropes -DartifactId=ropes -Dpackaging=jar -Dversion=1.2.5