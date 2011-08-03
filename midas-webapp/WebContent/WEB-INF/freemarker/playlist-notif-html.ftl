<#include "html-header.ftl">

<p style="color:#111111">${updateUser.friendlyName} (<a href="mailto:${updateUser.email}" style="color:#5b0d01">${updateUser.email}</a>) has updated their playlist '${playlist.title}' (${playlist.streamIds?size} tracks).</p>
<span style="background-color:#333333;border:1px solid #222222;border-radius:5px;padding:5px 10px"><a style="color:#5b0d01;display:inline-block;font:10pt 'Lucida Grande','Lucida Sans Unicode',Arial,sans-serif;text-decoration:none;color:#fff;text-decoration:none;" href="${playlistUrl}">Open Playlist</a></span>

<#include "html-footer.ftl">
