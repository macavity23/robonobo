<#include "macros.ftl">

Your friends have new music available on robonobo!

<#list users as user>
       <#assign hasLib = (user.numLibTrax > 0)>
       <#assign hasPl = (user.playlists?size > 0)>
       <#assign hasLoves = (user.loves?size > 0)>
       <@compress single_line=true>
       ${user.friendlyName} (${user.email})
       <#if hasLib> 
            added ${user.numLibTrax} tracks to their library<#t>
            <#if hasPl><#t>
                 <#lt><#if hasLoves>, <#else> and </#if>
            <#elseif hasLoves> and 
            </#if><#t>
       </#if>
       <#if hasLoves>
            loved ${user.loves?size} new <@hass obj="artist" lst=user.loves/> (<@commalist lst=user.loves; artist>${artist}</@commalist> - ${user.lovesUrl})<#t>
            <#if hasPl> and </#if><#t>
       </#if><#t>
       <#if hasPl>
            updated their <@hass obj="playlist" lst=user.playlists/><#t>
            <@commalist lst=user.playlists; playlist>'${playlist.title}' (${playlist.url})</@commalist><#rt>
       </#if><#t>
       <#lt>.
       </@compress>


</#list>
You can see these changes immediately in robonobo<#if havePlaylistsOrLoves>, or by clicking on the links above</#if>.

<#include "text-footer.ftl">

