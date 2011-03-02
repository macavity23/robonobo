set linefeed to ASCII character 10
set outputList to {}
set myPlaylistRefs to {}

tell application "iTunes"
	set allUserPlaylists to (get a reference to (every user playlist))
	repeat with pRef in allUserPlaylists
		try
         set isRbnbList to false
			set p to (contents of pRef)
         if exists parent of p then
            set par to (parent of p)
            if (exists parent of par) and (parent of par is folder playlist "robonobo") then
               set isRbnbList to true
            end if
         end if
			set pKind to (special kind of p)
			set pSmart to (smart of p)
			if (not isRbnbList) and (not pSmart) and (pKind is none) then
				copy pRef to the end of myPlaylistRefs
			end if
		end try
	end repeat
	
	repeat with pRef in myPlaylistRefs
		try
			set p to (contents of pRef)
			set pName to (name of p)
			set pTracks to (every file track of p)
			set trackPaths to {}
			repeat with pTrack in pTracks
				try
					set trackLoc to location of pTrack
					set trackPath to (the POSIX path of trackLoc)
					copy trackPath to the end of trackPaths
				end try
			end repeat
			if (count trackPaths) > 0
				copy (linefeed & pName) to the end of outputList
				repeat with path in trackPaths
					copy (linefeed & path) to the end of outputList
				end repeat
				copy linefeed to the end of outputList
			 end if
		end try
	end repeat
end tell
return outputList
