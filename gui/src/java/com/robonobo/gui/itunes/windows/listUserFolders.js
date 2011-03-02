var iTunes = getITunes();
var mainLibrary = iTunes.LibrarySource;
var playlists = mainLibrary.Playlists;
var roboFldr = getRoboFolder(iTunes);
for(i=1;i<=playlists.Count;i++) {
    var pl = playlists.Item(i);
    if(pl.Parent && pl.Parent.PlaylistID == roboFldr.PlaylistID) {
        output(pl.Name);
    }
}

