
${fromUser.friendlyName} (${fromUser.email}) wants to be your friend on robonobo.<#if playlist??> To ingratiate themselves, they have shared a playlist with you, titled '${playlist.title}' with ${playlist.streamIds?size} tracks.</#if>

If you become their friend, you will be able to see each others' music libraries and playlists<#if playlist??>, including '${playlist.title}'</#if>.

To accept the request, click here: ${acceptUrl}

To ignore their request, just delete this message.

<#include "text-footer.ftl">
