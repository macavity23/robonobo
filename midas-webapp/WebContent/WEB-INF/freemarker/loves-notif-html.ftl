<#include "macros.ftl">
<#include "html-header.ftl">

<p style="color:#111111">Your friend ${updateUser.friendlyName} (<a href="mailto:${updateUser.email}" style="color:#5b0d01">${updateUser.email}</a>) loves <@numitems obj="new artist" lst=artists/>: <@commalist lst=artists ; artist>${artist}</@commalist>.</p>

<span style="background-color:#333333;border:1px solid #222222;border-radius:5px;padding:5px 10px"><a style="color:#5b0d01;display:inline-block;font:10pt 'Lucida Grande','Lucida Sans Unicode',Arial,sans-serif;text-decoration:none;color:#fff;text-decoration:none;" href="${lovesUrl}">Open ${updateUser.friendlyName}'s Loves</a></span>

<#include "html-footer.ftl">
