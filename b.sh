#!/bin/sh
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=ropes.jar -DgroupId=org.ahmadsoft -DartifactId=ropes -Dversion=1.2.5 -Dpackaging=jar -DlocalRepositoryPath=lib
