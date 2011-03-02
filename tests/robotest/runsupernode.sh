#!/bin/bash

ROBOHOME=$(dirname $0)/
export cfg_robo_sonarServerUrl=http://5.0.0.1:8080/sonar-webapp/
export cfg_robo_metadataServerUrl=http://5.0.0.1:8080/midas-webapp/
export cfg_wang_bankUrl=http://5.0.0.1:8080/wang-server/
export cfg_robo_updateCheckUrl=http://5.0.0.1:8080/website/checkupdate
export cfg_mina_locateLocalNodes=false
export cfg_mina_listenUdpPort=23232
export cfg_mina_supernode=true

java -cp robonobo.jar com.robonobo.Robonobo -console
