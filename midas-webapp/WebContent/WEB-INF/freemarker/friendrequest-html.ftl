<#include "html-header.ftl">

<p>${fromUser.friendlyName} (<a href="mailto:${fromUser.email}">${fromUser.email}</a>) wants to be your friend on robonobo.<#if playlist??> To welcome you, they have sent you a playlist titled '${playlist.title}' with ${playlist.streamIds?size} tracks.</#if></p>

<p>If you become their friend, you will be able to see each others' music libraries and playlists<#if playlist??>, including '${playlist.title}'</#if>.</p>

<table>
	<tr>
		<td><a href="${acceptUrl}"><span>Confirm Friend Request</span></a></td>
		<td><a href="${rbnbUrl}account"><span>See All Requests</span></a></td>
	</tr>
</table>

<#include "html-footer.ftl">
