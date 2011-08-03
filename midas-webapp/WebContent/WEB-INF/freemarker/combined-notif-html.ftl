<#include "html-header.ftl">

<p style="color:#111111">Your friends have new music available on robonobo!</p>
<#list users as user>
       <p style="color:#111111">${user.friendlyName} (<a href="mailto:${user.email}" style="color:#5b0d01">${user.email}</a>)
       <#if (user.numLibTrax > 0)> added ${user.numLibTrax} tracks to their library<#if (user.playlists?size > 0)>, and</#if></#if><#if (user.playlists?size > 0)> updated their playlist<#if (user.playlists?size > 1)>s</#if><#list user.playlists as playlist><#if (playlist_index > 0 && playlist_index == (user.playlists?size - 1 ))> and<#elseif (playlist_index > 0)>,</#if> <a style="color:#5b0d01" href="${playlist.url}">${playlist.title}</a></#list></#if>.
       </p>
</#list>

<p style="color:#111111">You can see these changes immediately in robonobo<#if havePlaylists>, or by clicking on the playlist links above</#if>.</p>

<#include "html-footer.ftl">
