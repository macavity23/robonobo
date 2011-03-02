// Pass in the user folder, playlist name and a file containing one track (as a fully-qualified path to media file) per line
var args = getArgs();
if(args.Count() < 3) {
    error("Must supply user folder, playlist name and fully-qualified path to track listing file as arguments");
    quit(1);
}
var fldrName = args(0);
var playlistName = args(1);
var trackListPath = args(2);

// Make a list of tracks in our supplied tracklist
var fso = new ActiveXObject("Scripting.FileSystemObject");
var trackListFile = fso.GetFile(trackListPath);
var is = trackListFile.OpenAsTextStream(1,0); // Open for reading only
var newTracks = new Array();
var i=0;
while(!is.AtEndOfStream) {
	newTracks[i++] = is.ReadLine();
}
is.Close();

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

// Iterate, comparing the tracks in the playlist  
var currentTracks = playlist.Tracks;
var addFrom = 0;
for(i=0;i<currentTracks.Count;i++) {
	if(i >= newTracks.length) {
		// The current playlist includes everything in the supplied list (at least)... we're done
		WScript.Quit(0);
	}
	// Array starts at 0, itunes playlists start at 1, sigh
	var curTrack = currentTracks.Item(i+1);
	if(curTrack.Location != newTracks[i]) {
		// Tracks don't match.  Delete all playlist tracks from this one onwards
		// (count backwards to leave indices unchanged)
		for(j=currentTracks.Count;j>i;j--) {
			var delTrack = currentTracks.Item(j);
			delTrack.Delete();
		}
		break;
	}
	addFrom++;
}

// Add all remaining tracks
for(i=addFrom;i<newTracks.length;i++) {
	playlist.AddFile(newTracks[i]);
}