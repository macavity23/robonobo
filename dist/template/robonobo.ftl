#!/bin/sh
# robonobo execution script

java -client -Xmx128m -XX:MaxPermSize=48m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -cp robonobo-${version}.jar com.robonobo.Robonobo
 