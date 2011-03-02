var iTunes = getITunes();
var libPlaylist = iTunes.LibraryPlaylist;
var tracks = libPlaylist.Tracks;
for(i=1;i<=tracks.Count;i++) {
    var track = tracks.Item(i);
    output(track.Location);
}
