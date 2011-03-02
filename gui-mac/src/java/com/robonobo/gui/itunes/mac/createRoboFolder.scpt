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

-- MAINLINE
my getOrCreateRoboFldr()
