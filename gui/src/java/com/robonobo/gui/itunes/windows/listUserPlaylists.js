var args = getArgs();
if(args.Count() < 1) {
    error("Must supply user folder as argument");
    quit(1);
}
var fldrName = args(0);
var iTunes = getITunes();
var mainLibrary = iTunes.LibrarySource;
var playlists = mainLibrary.Playlists;
var roboFldr = getRoboFolder(iTunes);
var userFldr = null;
for(i=1;i<=playlists.Count;i++) {
    var pl = playlists.Item(i);
    if(pl.Name == fldrName && pl.Parent && pl.Parent.PlaylistID == roboFldr.PlaylistID) {
        userFldr = pl;
        break;
    }
}
if(userFldr == null) {
    error("User folder not found");
    quit(1);
}
for(i=1;i<=playlists.Count;i++) {
    pl = playlists.Item(i);
    if(pl.Parent && pl.Parent.PlaylistID == userFldr.PlaylistID) {
        output(pl.Name);
    }
}

