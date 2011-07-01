<#include "html-header.ftl">

<p>${fromUser.friendlyName} has shared a robonobo playlist with you, titled '${playlist.title}', with ${playlist.streamIds?size} tracks.</p>

<p>If you have robonobo installed, this playlist will be visible straight away. If you received this mail recently, you may have to click 'Refresh friends and playlists' from the 'Network' menu in robonobo to see the playlist.</p>

<table>
	<tr>		
		<td><a href="${playlistUrl}"><span>View Playlist Details</span></a></td>
	</tr>
</table>

<#include "html-footer.ftl">
