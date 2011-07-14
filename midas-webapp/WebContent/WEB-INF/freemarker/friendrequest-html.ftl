<#include "html-header.ftl">

<p>${fromUser.friendlyName} (<a href="mailto:${fromUser.email}" style="color:#5b0d01">${fromUser.email}</a>) wants to be your friend on robonobo.<#if playlist??> To welcome you, they have sent you a playlist titled '${playlist.title}' with ${playlist.streamIds?size} tracks.</#if></p>
<p style="margin-bottom:20px">If you become their friend, you will be able to see each others' music libraries and playlists.</p>
<span style="background-color:#333333;border:1px solid #222222;border-radius:5px;padding:5px 10px"><a style="display:inline-block;font:10pt 'Lucida Grande','Lucida Sans Unicode',Arial,sans-serif;text-decoration:none;color:#fff;text-decoration:none;" href="${acceptUrl}">Confirm Friend Request</a></span>
<span style="margin-left:10px;background-color:#333333;border:1px solid #222222;border-radius:5px;padding:5px 10px"><a style="display:inline-block;font:10pt 'Lucida Grande','Lucida Sans Unicode',Arial,sans-serif;text-decoration:none;color:#fff;text-decoration:none;" href="${rbnbUrl}account">See All Requests</a></span>

<#include "html-footer.ftl">
