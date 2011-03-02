// Outputs, for each non-empty, non-robonobo user playlist, the playlist name, a newline, then the fq path to each track on its own line, then a blank line
var iTunes = getITunes();
var mainLibrary = iTunes.LibrarySource;
var playlists = mainLibrary.Playlists;
var roboFldr = getRoboFolder(iTunes);
// Grab our robonobo user folders, so that we can exclude their children
var userFldrIds = new Array;
var fldrIdx = 0;
for (i = 1; i <= playlists.Count; i++) {
	var pl = playlists.Item(i);
	if (pl.Parent && pl.Parent.PlaylistID == roboFldr.PlaylistID) {
		userFldrIds[fldrIdx++] = pl.PlaylistID;
	}
}

for (i = 1; i <= playlists.Count; i++) {
	var pl = playlists.Item(i);
	var tracks = pl.Tracks;
	if ((pl.Kind == 2) && (!pl.Smart) && (tracks.Count > 0)) {
		// Check that this isn't a robonobo playlist
		if (pl.Parent) {
			var isRobo = 0;
			var parentId = pl.Parent.PlaylistID;
			for (j = 0; j < userFldrIds.length; j++) {
				if (parentId == userFldrIds[j]) {
					isRobo = 1;
					break;
				}
			}
			if (isRobo == 1) {
				continue;
			}
		}
		output(pl.Name);
		for (j = 1; j <= tracks.Count; j++) {
			var track = tracks.Item(j);
			output(track.Location);
		}
		output('');
	}
}
