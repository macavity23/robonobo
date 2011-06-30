<#include "html-header.ftl">

<p>${fromUser.friendlyName} (<a href="mailto:${fromUser.email}">${fromUser.email}</a>) has invited you to robonobo, the social music app.<#if playlist != null> To welcome you, they have sent you a playlist titled '${playlist.title}' with ${playlist.tracks.size} tracks.</#if></p>

<p>robonobo is an app for Windows, Mac and Linux that allows you to share music with your friends while supporting artists. See your friends' music libraries and playlists and listen instantly, while downloading the music files to your computer!</p>

<p><a href="${rbnbUrl}">Get more information about robonobo</a></p>

<table>
	<tr>
		<td><a href="${inviteUrl}"><span>Accept Invitation</span></a></td>
	</tr>
</table>

<#include "html-footer.ftl">
