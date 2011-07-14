<#include "html-header.ftl">

<p>${fromUser.friendlyName} has shared a robonobo playlist with you, titled '${playlist.title}', with ${playlist.streamIds?size} tracks.</p>
<p style="margin-bottom:20px">If you have robonobo installed, this playlist will be visible straight away. If you received this mail recently, you may have to click 'Refresh friends and playlists' from the 'Network' menu in robonobo to see the playlist.</p>
<span style="background-color:#333333;border:1px solid #222222;border-radius:5px;padding:5px 10px"><a style="display:inline-block;font:10pt 'Lucida Grande','Lucida Sans Unicode',Arial,sans-serif;text-decoration:none;color:#fff;text-decoration:none;" href="${playlistUrl}">View Playlist Details</a></span>

<#include "html-footer.ftl">
