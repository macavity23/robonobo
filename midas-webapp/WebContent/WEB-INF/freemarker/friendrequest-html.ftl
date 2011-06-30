<#include "html-header.ftl">

<p>${fromUser.friendlyName} (<a href="mailto:${fromUser.email}">${fromUser.email}</a>) wants to be your friend on robonobo.<#if playlist != null> To welcome you, they have sent you a playlist titled '${playlist.title}' with ${playlist.tracks.size} tracks.</#if></p>

<p>If you become their friend, you will be able to see each others' libraries and playlists<#if playlist != null>, including '${playlist.title}'</#if>.</p>

<table>
	<tr>
		<td><a href="${acceptUrl}"><span>Confirm Friend Request</span></a></td>
		<td><a href="${rbnbUrl}account"><span>See all requests</span></a></td>
	</tr>
</table>

<#include "html-footer.ftl">
