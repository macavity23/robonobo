<#include "macros.ftl">

Your friend ${updateUser.friendlyName} (${updateUser.email}) loves <@numitems obj="new artist" lst=artists/>: <@commalist lst=artists; artist>${artist}</@commalist>.

To visit their loves, open the robonobo app, or click here: ${lovesUrl}.

<#include "text-footer.ftl">
