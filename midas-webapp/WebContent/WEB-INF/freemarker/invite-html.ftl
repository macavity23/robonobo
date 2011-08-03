<#include "html-header.ftl">

<p style="color:#111111">${fromUser.friendlyName} (<a href="mailto:${fromUser.email}" style="color:#5b0d01">${fromUser.email}</a>) has invited you to robonobo, the social music app.<#if playlist??> To welcome you, they have sent you a playlist titled '${playlist.title}' with ${playlist.streamIds?size} tracks.</#if></p>
<p style="color:#111111">robonobo is an app for Windows, Mac and Linux that allows you to share music with your friends while supporting artists. See your friends' music libraries and playlists and listen instantly, while downloading the music files to your computer!</p>
<p style="margin-bottom:20px"><a href="${rbnbUrl}" style="color:#5b0d01">Get more information about robonobo</a></p>
<span style="background-color:#333333;border:1px solid #222222;border-radius:5px;padding:5px 10px"><a style="display:inline-block;font:10pt 'Lucida Grande','Lucida Sans Unicode',Arial,sans-serif;text-decoration:none;color:#fff;text-decoration:none;" href="${inviteUrl}">Accept Invitation</a></span>
          </div>
        </div>
        <div style="clear:left;text-align:center;font-size:8pt;padding:10px 20px;color:#efefef;background-color:#100f0f;border-top:1px solid #661504">
          <span>This email was sent to ${toEmail}.</span>
        </div>
      </div>
  </body>
</html>
