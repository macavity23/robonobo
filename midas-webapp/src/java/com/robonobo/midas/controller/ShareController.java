package com.robonobo.midas.controller;

import static com.robonobo.common.util.TextUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.User;
import com.robonobo.midas.LocalMidasService;
import com.robonobo.midas.MessageService;
import com.robonobo.midas.model.MidasFriendRequest;
import com.robonobo.midas.model.MidasInvite;
import com.robonobo.midas.model.MidasPlaylist;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.remote.service.MailService;
import com.robonobo.remote.service.MailServiceImpl;
import com.robonobo.remote.service.MidasService;

@Controller
public class ShareController extends BaseController {
	@Autowired
	private MessageService message;

	@RequestMapping(value = "/share-playlist/share")
	@Transactional(rollbackFor = Exception.class)
	public void doShare(@RequestParam("plid") String plIdStr, @RequestParam(value = "friendids", required = false) String friendIdStrs,
			@RequestParam(value = "emails", required = false) String emails, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		long plId = Long.parseLong(plIdStr, 16);
		MidasPlaylist p = midas.getPlaylistById(plId);
		if (p == null) {
			send404(req, resp);
			return;
		}
		MidasUser authUser = getAuthUser(req);
		if (authUser == null || !p.getOwnerIds().contains(authUser.getUserId())) {
			send401(req, resp);
			return;
		}
		List<String> friendIds = isNonEmpty(friendIdStrs) ? Arrays.asList(friendIdStrs.split(",")) : new ArrayList<String>();
		List<String> newFriendEmailParams = isNonEmpty(emails) ? Arrays.asList(emails.split(",")) : new ArrayList<String>();
		// We'll have three categories of users that might need updating:
		// 1. Existing friends of this user who need to be added to this
		// playlist
		Set<MidasUser> newPlaylistUsers = new HashSet<MidasUser>();
		// 2. Existing robonobo users who are not friends of this user - send
		// them a friend request
		Set<MidasUser> newFriends = new HashSet<MidasUser>();
		// 3. Non-users of robonobo - send them invite emails
		Set<String> inviteEmails = new HashSet<String>();
		// Go through our parameters, figuring out which users are in our 3
		// categories
		for (String emailParam : newFriendEmailParams) {
			String email = urlDecode(emailParam);
			MidasUser shareUser = midas.getUserByEmail(email);
			if (shareUser == null) {
				inviteEmails.add(email);
			} else if (authUser.getFriendIds().contains(shareUser.getUserId())) {
				// They are already a friend, deal with them in a moment...
				friendIds.add(Long.toString(shareUser.getUserId()));
			} else {
				// Friend request
				newFriends.add(shareUser);
			}
		}
		// Check we have enough invites
		// TODO rem'd this out as we're not using invites for the moment
		// if (inviteEmails.size() > 0) {
		// if (inviteEmails.size() > authUser.getInvitesLeft()) {
		// // Client should have checked this already, so just chuck an error
		// log.error("User " + authUser.getEmail() + " tried to share playlist " + p.getTitle()
		// + ", but has insufficient invites");
		// throw new IOException("Not enough invites left!");
		// }
		// authUser.setInvitesLeft(authUser.getInvitesLeft() - inviteEmails.size());
		// midas.saveUser(authUser);
		// }
		// Users specified via user id must already be friends
		for (String friendIdStr : friendIds) {
			long friendId = Long.parseLong(friendIdStr, 16);
			if (!authUser.getFriendIds().contains(friendId)) {
				send401(req, resp);
				return;
			}
			MidasUser friend = midas.getUserById(friendId);
			if (friend == null) {
				send404(req, resp);
				return;
			}
			if (friend.getPlaylistIds().contains(p.getPlaylistId()))
				continue;
			newPlaylistUsers.add(friend);
		}
		// Existing friends - add them as playlist owners, and send them a
		// notification
		for (MidasUser friend : newPlaylistUsers) {
			if (log.isDebugEnabled())
				log.debug("User " + authUser.getEmail() + " sharing playlist " + p.getTitle() + " with existing friend " + friend.getEmail());
			friend.getPlaylistIds().add(p.getPlaylistId());
			friend.setUpdated(getUpdatedDate(friend.getUpdated()));
			p.getOwnerIds().add(friend.getUserId());
			midas.saveUser(friend);
			message.sendPlaylistShare(authUser, friend, p);
		}
		p.setUpdated(getUpdatedDate(p.getUpdated()));
		midas.savePlaylist(p);
		// New friends
		for (MidasUser newFriend : newFriends) {
			if (log.isDebugEnabled())
				log.debug("User " + authUser.getEmail() + " sharing playlist " + p.getTitle() + " with new friend " + newFriend.getEmail());
			message.sendFriendRequest(authUser, newFriend, p);
		}
		// Invites
		for (String invitee : inviteEmails) {
			if (log.isDebugEnabled())
				log.debug("User " + authUser.getEmail() + " sharing playlist " + p.getTitle() + " with invited robonobo user " + invitee);
			message.sendInvite(authUser, invitee, p);
		}
		writeToOutput(p.toMsg(), resp);
	}

	@RequestMapping(value = "/add-friends")
	@Transactional(rollbackFor = Exception.class)
	public void doAddFriends(@RequestParam("emails") String emails, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser authUser = getAuthUser(req);
		if (authUser == null) {
			send401(req, resp);
			return;
		}
		List<String> emailParams = Arrays.asList(emails.split(","));
		// We'll have two categories of users that might need updating:
		// 1. Existing robonobo users who are not friends of this user - send
		// them a friend request
		List<Long> friendIds = new ArrayList<Long>();
		// 2. Non-users of robonobo - send them invite emails
		List<String> inviteEmails = new ArrayList<String>();
		// Go through our parameters, figuring out which users are in our categories
		for (String emailParam : emailParams) {
			String email = urlDecode(emailParam);
			MidasUser shareUser = midas.getUserByEmail(email);
			if (shareUser == null) {
				// Invite
				inviteEmails.add(email);
			} else if (authUser.getFriendIds().contains(shareUser.getUserId())) {
				// They're already a friend, doofus
			} else {
				// Friend request
				friendIds.add(shareUser.getUserId());
			}
		}
		midas.addFriends(authUser.getUserId(), friendIds, inviteEmails);
	}
}
