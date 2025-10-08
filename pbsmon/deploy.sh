#!/usr/bin/env bash

#echo "compiling"
#mvn clean package
echo "deploying"
scp target/pbsmon2.war tomcat@segin.vm.cesnet.cz:/var/lib/tomcat9/webapps/
