var args = getArgs();
if(args.Count() == 0) {
    error("Must supply folder name as argument");
    quit(1);
}
var fldrName = args(0);
var iTunes = getITunes();
var mainLibrary = iTunes.LibrarySource;
var playlists = mainLibrary.Playlists;
var roboFldr = getRoboFolder(iTunes);
for(i=1;i<=playlists.Count;i++) {
    var pl = playlists.Item(i);
    if(pl.Name == fldrName && pl.Parent && pl.Parent.PlaylistID == roboFldr.PlaylistID) {
        pl.Delete();
        quit(0);
    }
}
error("Folder not found");
quit(1);

