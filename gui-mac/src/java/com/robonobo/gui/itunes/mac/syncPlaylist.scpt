global allPlaylists, roboFldr, userFldr, syncPlaylist

to getOrCreateRoboFldr()
	tell application "iTunes"
		set roboFldr to null
		try
			set roboFldr to (get folder playlist "robonobo")
		end try
		if roboFldr is null then
			set roboFldr to (make new folder playlist with properties {name:"robonobo"})
		end if
		return roboFldr
	end tell
end getOrCreateRoboFldr

to getOrCreateUserFldr(userName)
	tell application "iTunes"
		set userFldr to null
		repeat with playlistRef in allPlaylists
			set p to (contents of playlistRef)
			if (name of p) is userName and (parent of p) is roboFldr then
				set userFldr to p
			end if
		end repeat
		if userFldr is null then
			set userFldr to (make new folder playlist with properties {name:userName})
			move userFldr to roboFldr
		end if
		return userFldr
	end tell
end getOrCreateUserFldr

to getOrCreatePlaylist(playlistName)
	tell application "iTunes"
		set syncPlaylist to null
		repeat with playlistRef in allPlaylists
			set p to (contents of playlistRef)
			if (name of p) is playlistName and (parent of p) is userFldr then
				set syncPlaylist to p
			end if
		end repeat
		if syncPlaylist is null then
			set syncPlaylist to (make new user playlist with properties {name:playlistName})
			move syncPlaylist to userFldr
		end if
		return syncPlaylist
	end tell
end getOrCreatePlaylist

to fileAsList(filePath)
	set myFile to (open for access (POSIX file filePath))
	set allText to (read myFile for (get eof myFile))
	close access myFile
	set lineList to every paragraph of allText
	return lineList
end fileAsList

to syncPlaylist(userStr, playlistName, trackListFilePath)
	tell application "iTunes"
		set allPlaylists to (get a reference to (every playlist))
		my getOrCreateRoboFldr()
		my getOrCreateUserFldr(userStr)
		set myPlaylist to my getOrCreatePlaylist(playlistName)
		set trackLocList to my fileAsList(trackListFilePath)
		set plTracks to (every file track of myPlaylist)
		set i to 0
		-- Go through each track in the playlist, find where they differ
		set diffPoint to ((count plTracks) + 1)
		repeat with plTrack in plTracks
			set i to (i + 1)
			set wantTrackLoc to item i of trackLocList
			set wantTrackTxt to wantTrackLoc as string
			set actualTrackLoc to location of plTrack
			set actualTrackPath to (the POSIX path of actualTrackLoc)
			set actualTrackTxt to actualTrackPath as string
			if actualTrackTxt is not wantTrackTxt then
				set diffPoint to i
				exit repeat
			end if
		end repeat
		-- Delete everything in the playlist after the difference point
		if (count plTracks) > 0 and (diffPoint <= (count plTracks)) then
			repeat with delTrack in items diffPoint thru (count plTracks) of plTracks
				delete delTrack
			end repeat
		end if
		-- Add everything that's new
		if (count trackLocList) > 0 and (diffPoint <= (count trackLocList)) then
			repeat with addTrackPath in items diffPoint thru (count trackLocList) of trackLocList
				set pathTxt to addTrackPath as string
				if pathTxt is not "" then
					set addTrack to addTrackPath as POSIX file
					add addTrack to myPlaylist
				end if
			end repeat
		end if
	end tell
end syncPlaylist

-- MAINLINE
on run argv
	set user to item 1 of argv
	set pl to item 2 of argv
	set filePath to item 3 of argv
	my syncPlaylist(user, pl, filePath)
end run

