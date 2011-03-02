#!/bin/bash
nodeNum=0

ROBOHOME=$(dirname $0)/home
mkdir $ROBOHOME
export ROBOHOME
export cfg_robo_sonarServerUrl=http://5.0.0.1:8080/sonar-webapp/
export cfg_robo_metadataServerUrl=http://5.0.0.1:8080/midas-webapp/
export cfg_wang_bankUrl=http://5.0.0.1:8080/wang-server/
export cfg_robo_updateCheckUrl=http://5.0.0.1:8080/website/checkupdate
export cfg_mina_listenUdpPort=(( 23233 + $nodeNum ))
export cfg_mina_locateLocalNodes=false
export cfg_mina_maxOutboundBps=25600

java -cp robonobo.jar:plugin-robotest.jar com.robonobo.Robonobo -console
