#!/bin/sh

CLASSPATH=robonobo.jar
MEMOPTS="-client -Xmx96m -Xms16m -XX:MaxPermSize=32m -Xss64k -XX:NewRatio=2"
#DEBUG_LISTEN="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9190"
JVMARGS=$MEMOPTS $DEBUG_LISTEN

export CLASSPATH

export cfg_robo_metadataServerUrl=http://midas.robonobo.com/
export cfg_robo_playlistUrlBase=http://rbnb.co/p/
export cfg_robo_sonarServerUrl=http://sonar.robonobo.com/
export cfg_robo_updateCheckUrl=http://robonobo.com/checkupdate
export cfg_robo_userAccountUrl=http://robonobo.com/account
export cfg_wang_bankUrl=http://bank.acceptwang.com/

java $JVMARGS com.robonobo.Robonobo
