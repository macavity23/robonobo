<#-- renders a list of items in proper grammatical text, without an oxford comma -->
<#macro commalist lst><#list lst as item><#if (item_index > 0 && item_index == (lst?size - 1 ))> and <#elseif (item_index > 0)>, </#if><#nested item></#list></#macro>

<#-- renders an s after the object based on the size of the list -->
<#macro hass obj lst>${obj}<#if (lst?size > 1 || lst?size == 0)>s</#if></#macro>

<#macro numitems obj lst>${lst?size} <@hass obj=obj lst=lst/></#macro>
