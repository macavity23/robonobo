#!/bin/sh

CLASSPATH=robonobo.jar
MEMOPTS="-client -Xmx96m -Xms16m -XX:MaxPermSize=32m -Xss64k -XX:NewRatio=2"
#DEBUG_LISTEN="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9190"
JVMARGS=$MEMOPTS $DEBUG_LISTEN

export CLASSPATH

export cfg_robo_metadataServerUrl=http://robonobo.com:9080/midas/
export cfg_robo_playlistUrlBase=http://robonobo.com:9080/website/p/
export cfg_robo_sonarServerUrl=http://robonobo.com:9080/sonar/
export cfg_robo_updateCheckUrl=http://robonobo.com:9080/website/checkupdate
export cfg_robo_userAccountUrl=http://robonobo.com:9080/website/account
export cfg_wang_bankUrl=http://robonobo.com:9080/wang/

java $JVMARGS com.robonobo.Robonobo
