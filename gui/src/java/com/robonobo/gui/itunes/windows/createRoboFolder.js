var iTunes = getITunes();
var roboFldr = getRoboFolder(iTunes);
if(roboFldr == null) {
    iTunes.createFolder('robonobo');
}