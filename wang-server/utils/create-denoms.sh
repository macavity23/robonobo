#!/bin/bash

CLASSPATH=../target/classes:../../wang-client/target/classes:../../common/target/classes
for jar in ../../common/lib/*.jar ../../common-webapps/lib/*.jar ../WebContent/WEB-INF/lib/*.jar
do
	CLASSPATH=$CLASSPATH:$jar
done
#DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=7000,server=y,suspend=y"
java $DEBUG -cp $CLASSPATH com.robonobo.wang.server.utils.CreateDenoms $*

