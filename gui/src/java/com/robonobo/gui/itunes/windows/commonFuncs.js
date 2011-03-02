function getArgs() {
    return WScript.Arguments;
}

function output(str) {
    WScript.Echo(str);
}

function debug(str) {
    WScript.Echo("*** DEBUG "+str);
}

function info(str) {
    WScript.Echo("*** INFO "+str);
}

function error(str) {
    WScript.Echo("*** ERROR "+str);
}

function getITunes() {
    return WScript.CreateObject("iTunes.Application");
}

function getRoboFolder(iTunes) {
    var mainLibrary = iTunes.LibrarySource;
    var playlists = mainLibrary.playlists;
    return playlists.ItemByName('robonobo');
}

function quit(retCode) {
    WScript.Quit(retCode);
}