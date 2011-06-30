
${fromUser.friendlyName} (${fromUser.email}) wants to be your friend on robonobo.<#if playlist != null> To welcome you, they have sent you a playlist titled '${playlist.title}' with ${playlist.tracks.size} tracks.</#if>

If you become their friend, you will be able to see each others' libraries and playlists<#if playlist != null>, including '${playlist.title}'</#if>.

To accept the request, click here: ${acceptUrl}

To ignore their request, just delete this message.

<#include "text-footer.ftl">
