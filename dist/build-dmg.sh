#!/bin/bash
 
if [[ $# < 4 ]]
then
    echo "Usage: build-dmg.sh <skel-dir> <dmg title> <dmg sz in kb> <final dmg name>" >&2
    exit 2
fi

source=$1
title=$2
size=$3
finalDMGName=$4

tmpDMGName=/tmp/pack-temp-$$.dmg

hdiutil create -srcfolder "${source}" -volname "${title}" -fs HFS+ -fsargs "-c c=64,a=16,e=16" -format UDRW -size ${size}k ${tmpDMGName}

device=$(hdiutil attach -readwrite -noverify -noautoopen ${tmpDMGName} | egrep '^/dev/' | sed 1q | awk '{print $1}')

chmod -Rf go-w /Volumes/"${title}"
sync
sync

echo '
   tell application "Finder"
     tell disk "'${title}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 830, 390}
           set theViewOptions to the icon view options of container window
           set arrangement of theViewOptions to not arranged
           set icon size of theViewOptions to 128
           set background picture of theViewOptions to file ".background:background.png"
           make new alias file at container window to POSIX file "/Applications" with properties {name:"Applications"}
           set position of item "robonobo.app" of container window to {20, 150}
           set position of item "Applications" of container window to {325, 150}
           set position of item ".background" of container window to {10, 400}
           set position of item ".Trashes" of container window to {60, 400}
           set position of item ".fseventsd" of container window to {110, 400}
           close
           open
           update without registering applications
           set position of item ".DS_Store" of container window to {160, 400}
           delay 5
           eject
     end tell
   end tell
' | osascript

hdiutil convert ${tmpDMGName} -format UDZO -imagekey zlib-level=9 -o "${finalDMGName}"
rm -f ${tmpDMGName}
