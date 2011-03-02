set linefeed to ASCII character 10
set listOfPaths to {}
tell application "iTunes"
	set tracklist to every file track of library playlist 1
	repeat with plTrack in tracklist
		try
			set trackLoc to location of plTrack
			set trackPath to POSIX path of trackLoc
			copy (linefeed & trackPath) to the end of listOfPaths
		end try
	end repeat
end tell
return listOfPaths
