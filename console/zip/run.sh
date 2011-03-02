#!/bin/bash

CLASSPATH=robonobo.jar
MEMOPTS="-client -Xmx96m -Xms16m -XX:MaxPermSize=32m -Xss64k -XX:NewRatio=2"
#DEBUG_LISTEN="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9190"
JVMARGS=$MEMOPTS $DEBUG_LISTEN

export CLASSPATH

java $JVMARGS com.robonobo.Robonobo
