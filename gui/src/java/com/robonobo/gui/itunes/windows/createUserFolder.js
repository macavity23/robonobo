var args = getArgs();
if(args.Count() == 0) {
    error("Must supply folder name as argument");
    quit(1);
}
var fldrName = args(0);
var iTunes = getITunes();
var roboFldr = getRoboFolder(iTunes);
roboFldr.CreateFolder(fldrName);
