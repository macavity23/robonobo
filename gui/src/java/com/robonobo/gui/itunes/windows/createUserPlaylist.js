var args = getArgs();
if(args.Count() < 2) {
    error("Must supply user folder and playlist name as arguments");
    quit(1);
}
var fldrName = args(0);
var playlistName = args(1);
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

userFldr.CreatePlaylist(playlistName);


