<#include "macros.ftl">
<#include "html-header.ftl">

<p style="color:#111111">Your friends have new music available on robonobo!</p>
<#list users as user>
       <#assign hasLib = (user.numLibTrax > 0)>
       <#assign hasPl = (user.playlists?size > 0)>
       <#assign hasLoves = (user.loves?size > 0)>

       <@compress single_line=true>
       <p style="color:#111111">${user.friendlyName} (<a href="mailto:${user.email}" style="color:#5b0d01">${user.email}</a>)
       <#if hasLib> 
            added ${user.numLibTrax} tracks to their library<#t>
            <#if hasPl><#t>
                 <#lt><#if hasLoves>, <#else> and </#if>
            <#elseif hasLoves> and 
            </#if><#t>
       </#if>
       <#if hasLoves>
            loved ${user.loves?size} new <@hass obj="artist" lst=user.loves/> (<a href="${user.lovesUrl}"><@commalist lst=user.loves; artist>${artist}</@commalist></a>)<#t>
            <#if hasPl> and </#if><#t>
       </#if><#t>
       <#if hasPl>
            updated their <@hass obj="playlist" lst=user.playlists/><#t>
            <@commalist lst=user.playlists; playlist><a style="color:#5b0d01" href="${playlist.url}">${playlist.title}</a></@commalist><#rt>
       </#if><#t>
       .</p><#t>
       </@compress>
</#list>

<p style="color:#111111">You can see these changes immediately in robonobo<#if havePlaylistsOrLoves>, or by clicking on the links above</#if>.</p>

<#include "html-footer.ftl">
