package com.robonobo.midas.controller;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.TextUtil;
import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.User;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.midas.*;
import com.robonobo.midas.model.*;
import com.robonobo.remote.service.MidasService;

@Controller
public class PlaylistController extends BaseController {
	@Autowired
	EventService event;
	@Autowired
	FacebookService facebook;
	@Autowired
	TwitterService twitter;

	@RequestMapping(value = "/playlists/{pIdStr}", method = RequestMethod.GET)
	public void getPlaylist(@PathVariable("pIdStr") String pIdStr, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long playlistId = Long.parseLong(pIdStr, 16);
		MidasPlaylist p = midas.getPlaylistById(playlistId);
		if (p == null) {
			send404(req, resp);
			return;
		}
		// Check to see if this user is allowed to see the playlist - is it public, or if not, is s/he
		// the owner, or a friend of the owner?
		User u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		boolean allowed = p.getVisibility().equals(Playlist.VIS_ALL);
		if (!allowed) {
			ownerLoop: for (Long ownerId : p.getOwnerIds()) {
				if (ownerId.equals(u.getUserId())) {
					allowed = true;
					break;
				}
				if (p.getVisibility().equals(Playlist.VIS_FRIENDS)) {
					User owner = midas.getUserById(ownerId);
					for (Long friendId : owner.getFriendIds()) {
						if (u.getUserId() == friendId) {
							allowed = true;
							break ownerLoop;
						}
					}
				}
			}
		}
		if (allowed) {
			log.info("Returning playlist " + playlistId + " to " + u.getEmail());
			writeToOutput(p.toMsg(), resp);
		} else
			send401(req, resp);
	}

	@RequestMapping(value = "/playlists/{pIdStr}", method = RequestMethod.PUT)
	@Transactional(rollbackFor = Exception.class)
	public void putPlaylist(@PathVariable("pIdStr") String pIdStr, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long playlistId = Long.parseLong(pIdStr, 16);
		MidasUser u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		MidasPlaylist currentP = (playlistId <= 0) ? null : midas.getPlaylistById(playlistId);
		PlaylistMsg.Builder pBldr = PlaylistMsg.newBuilder();
		readFromInput(pBldr, req);
		PlaylistMsg pMsg = pBldr.build();
		MidasPlaylist mp = new MidasPlaylist(pMsg);
		if (currentP == null) {
			// New playlist
			mp.getOwnerIds().clear();
			mp.getOwnerIds().add(u.getUserId());
			mp.setUpdated(now());
			mp = midas.newPlaylist(mp);
			u.getPlaylistIds().add(mp.getPlaylistId());
			u.setUpdated(now());
			midas.saveUser(u);
			writeToOutput(mp.toMsg(), resp);
			log.info(u.getEmail() + " created playlist " + mp.getPlaylistId());
			event.playlistCreated(u, mp);
		} else {
			// Existing playlist
			if (!currentP.getOwnerIds().contains(u.getUserId())) {
				send401(req, resp);
				return;
			}
			currentP.copyFrom(mp);
			currentP.setUpdated(getUpdatedDate(currentP.getUpdated()));
			midas.savePlaylist(currentP);
			writeToOutput(currentP.toMsg(), resp);
			log.info(u.getEmail() + " updated playlist " + playlistId);
			event.playlistUpdated(u, currentP);
		}
	}

	@RequestMapping(value = "/playlists/{pIdStr}", method = RequestMethod.DELETE)
	@Transactional(rollbackFor = Exception.class)
	public void deletePlaylist(@PathVariable("pIdStr") String pIdStr, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long playlistId = Long.parseLong(pIdStr, 16);
		MidasUser u = getAuthUser(req);
		MidasPlaylist p = midas.getPlaylistById(playlistId);
		if (p == null)
			return;
		if (u == null || !p.getOwnerIds().contains(u.getUserId())) {
			send401(req, resp);
			return;
		}
		if (p.getOwnerIds().size() == 1) {
			// If they were the only owner, just delete the whole thing
			midas.deletePlaylist(p);
			u.getPlaylistIds().remove(playlistId);
			u.setUpdated(now());
			midas.saveUser(u);
			log.info(u.getEmail() + " deleted playlist " + playlistId);
		} else {
			// Otherwise, just remove them from the owner list
			p.getOwnerIds().remove(u.getUserId());
			p.setUpdated(getUpdatedDate(p.getUpdated()));
			midas.savePlaylist(p);
			u.getPlaylistIds().remove(playlistId);
			u.setUpdated(now());
			midas.saveUser(u);
			log.info("Removed user " + u.getEmail() + " from owners of playlist " + playlistId);
		}
		event.playlistDeleted(u, p);
	}

	@RequestMapping("/playlists/{pIdStr}/post-update")
	public void postPlaylistUpdate(@PathVariable("pIdStr") String pIdStr, @RequestParam("service") String service,
			@RequestParam(value = "msg", required = false) String msg, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long playlistId = Long.parseLong(pIdStr, 16);
		Playlist p = midas.getPlaylistById(playlistId);
		service = service.toLowerCase();
		MidasUser u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		boolean allowed = false;
		if(p.getOwnerIds().contains(u.getUserId()))
			allowed = true;
		else if(p.getVisibility().equalsIgnoreCase("all"))
			allowed = true;
		else if(p.getVisibility().equalsIgnoreCase("friends")) {
			for (long ownerId : p.getOwnerIds()) {
				User owner = midas.getUserById(ownerId);
				if(owner.getFriendIds().contains(u.getUserId())) {
					allowed = true;
					break;
				}
			}
		}
		if(!allowed) {
			send401(req, resp);
			return;
		}
		if(msg != null)
			msg = urlDecode(msg);
		MidasUserConfig muc = midas.getUserConfig(u);
		if ("facebook".equals(service))
			facebook.postPlaylistUpdateToFacebook(muc, p, msg);
		else if ("twitter".equals(service))
			twitter.postPlaylistUpdateToTwitter(muc, p, msg);
		else
			log.error("Error: user " + u.getEmail() + " tried to update their playlist to service: '" + service);
		event.playlistPosted(u, p, service);
	}
}
