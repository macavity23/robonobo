var args = getArgs();
if(args.Count() < 3) {
    error("Must supply user folder, playlist name and fully-qualified path to track as arguments");
    quit(1);
}
var fldrName = args(0);
var playlistName = args(1);
var trackPath = args(2);
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
var playlist = null;
for(i=1;i<=playlists.Count;i++) {
    pl = playlists.Item(i);
    if(pl.Name == playlistName && pl.Parent && pl.Parent.PlaylistID == userFldr.PlaylistID) {
        playlist = pl;
        break;
    }
}
if(playlist == null) {
    error("Playlist not found");
    quit(1);
}
playlist.AddFile(trackPath);