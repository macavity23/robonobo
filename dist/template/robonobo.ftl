#!/bin/sh
# robonobo execution script

java -client -Xmx128m -XX:MaxPermSize=48m -cp robonobo-${version}.jar com.robonobo.Robonobo
 