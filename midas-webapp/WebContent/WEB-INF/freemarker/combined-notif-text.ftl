
Your friends have new music available on robonobo!

<#list users as user>
${user.friendlyName} (${user.email})<#if (user.numLibTrax > 0)> added ${user.numLibTrax} tracks to their library<#if (user.playlists?size > 0)>, and</#if></#if><#if (user.playlists?size > 0)> updated their playlist<#if (user.playlists?size > 1)>s</#if><#list user.playlists as playlist><#if (playlist_index > 0 && playlist_index == (user.playlists?size - 1 ))> and<#elseif (playlist_index > 0)>,</#if> '${playlist.title}' (${playlist.url})</#list></#if>.

</#list>
You can see these changes immediately in robonobo<#if havePlaylists>, or by clicking on the playlist links above</#if>.

<#include "text-footer.ftl">
