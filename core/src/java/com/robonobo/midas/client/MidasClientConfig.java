package com.robonobo.midas.client;

import java.io.Serializable;
import java.util.*;

import javax.swing.plaf.TextUI;

import com.robonobo.common.util.TextUtil;

import static com.robonobo.common.util.TextUtil.*;

public class MidasClientConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private String baseUrl;

	public MidasClientConfig() {
	}

	public MidasClientConfig(String baseUrl) {
		if(!baseUrl.endsWith("/"))
			baseUrl = baseUrl + "/";
		this.baseUrl = baseUrl;
	}

	public String getPlaylistUrl(long playlistId) {
		return baseUrl + "playlists/" + Long.toHexString(playlistId);
	}

	public String getSharePlaylistUrl(long playlistId, Collection<Long> friendIds, Collection<String> emails) {
		StringBuffer sb = new StringBuffer(baseUrl).append("share-playlist/share?plid=");
		sb.append(Long.toHexString(playlistId));
		if(friendIds.size() > 0) {
			sb.append("&friendids=");
			boolean first=true;
			for (Long friendId : friendIds) {
				if(first)
					first = false;
				else
					sb.append(",");
				sb.append(Long.toHexString(friendId));
			}
		}
		if(emails.size() > 0) {
			sb.append("&emails=");
			boolean first=true;
			for (String email : emails) {
				if(first)
					first = false;
				else
					sb.append(",");
				sb.append(urlEncode(email));
			}
		}
		return sb.toString();
	}
	
	public String getPlaylistServiceUpdateUrl(String service, long playlistId, String msg) {
		return baseUrl + "playlists/" + Long.toHexString(playlistId) + "/post-update?service="+service+"&msg="+urlEncode(msg);
	}
	
	public String getStreamUrl(String streamId) {
		return baseUrl + "streams/" + streamId;
	}

	public String getUserUrl(String userEmail) {
		return baseUrl + "users/byemail/" + urlEncode(userEmail);
	}
	
	public String getUserUrl(long userId) {
		return baseUrl + "users/byid/" + Long.toHexString(userId);
	}
	
	public String getLibraryUrl(long userId, Date since) {
		String url = baseUrl + "library/"+Long.toHexString(userId);
		if(since != null)
			url += "?since="+since.getTime();
		return url;
	}
	
	public String getLibraryAddUrl(long userId) {
		return baseUrl + "library/"+Long.toHexString(userId)+"/add";
	}
	
	public String getLibraryDelUrl(long userId) {
		return baseUrl + "library/"+Long.toHexString(userId)+"/del";
	}
	
	public String getUserConfigUrl(long userId) {
		return baseUrl + "userconfig/"+Long.toHexString(userId);
	}
	
	public String getSearchQueryUrl(String query, int firstResult) {
		String result = baseUrl + "search?type=stream&q=" + TextUtil.urlEncode(query);
		if (firstResult > 0)
			result += "&start=" + String.valueOf(firstResult);
		return result;
	}
}
