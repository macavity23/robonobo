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
import com.robonobo.midas.model.MidasFriendRequest;
import com.robonobo.midas.model.MidasInvite;
import com.robonobo.midas.model.MidasPlaylist;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.remote.service.MailService;
import com.robonobo.remote.service.MailServiceImpl;
import com.robonobo.remote.service.MidasService;

@Controller
public class SharePlaylistController extends BaseController {
	@Autowired
	private MailService mail;

	@RequestMapping(value = "/share-playlist/share")
	@Transactional(rollbackFor=Exception.class)
	public void doShare(@RequestParam("plid") String plIdStr,
			@RequestParam(value = "friendids", required = false) String friendIdStrs, 
			@RequestParam(value="emails", required=false) String emails,
			HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
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
		List<String> newFriendEmailParams = isNonEmpty(emails) ? Arrays.asList(emails.split(","))
				: new ArrayList<String>();
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
//		if (inviteEmails.size() > 0) {
//			if (inviteEmails.size() > authUser.getInvitesLeft()) {
//				// Client should have checked this already, so just chuck an error
//				log.error("User " + authUser.getEmail() + " tried to share playlist " + p.getTitle()
//						+ ", but has insufficient invites");
//				throw new IOException("Not enough invites left!");
//			}
//			authUser.setInvitesLeft(authUser.getInvitesLeft() - inviteEmails.size());
//			midas.saveUser(authUser);
//		}
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
				log.debug("User " + authUser.getEmail() + " sharing playlist " + p.getTitle() + " with existing friend "
						+ friend.getEmail());
			friend.getPlaylistIds().add(p.getPlaylistId());
			friend.setUpdated(getUpdatedDate(friend.getUpdated()));
			p.getOwnerIds().add(friend.getUserId());
			midas.saveUser(friend);
			sendNotifyPlaylistShare(authUser, friend, p);
		}
		p.setUpdated(getUpdatedDate(p.getUpdated()));
		midas.savePlaylist(p);
		// New friends
		for (MidasUser newFriend : newFriends) {
			if (log.isDebugEnabled())
				log.debug("User " + authUser.getEmail() + " sharing playlist " + p.getTitle() + " with new friend "
						+ newFriend.getEmail());
			MidasFriendRequest friendReq = midas.createOrUpdateFriendRequest(authUser, newFriend, p);
			sendFriendRequest(friendReq, authUser, newFriend, p);
		}
		// Invites
		for (String invitee : inviteEmails) {
			if (log.isDebugEnabled())
				log.debug("User " + authUser.getEmail() + " sharing playlist " + p.getTitle() + " with invited robonobo user "
						+ invitee);
			MidasInvite invite = midas.createOrUpdateInvite(invitee, authUser, p);
			sendInvite(invite, authUser, p);
		}
		writeToOutput(p.toMsg(), resp);
	}

	protected void sendNotifyPlaylistShare(User fromUser, User toUser, Playlist p) throws IOException {
		String subject = fromUser.getFriendlyName() + " shared a playlist with you: " + p.getTitle();
		StringBuffer bod = new StringBuffer();
		bod.append(fromUser.getFriendlyName()).append("(").append(fromUser.getEmail()).append(")");
		bod.append(" shared a robonobo playlist with you.\n\n");
		bod.append("Title: ").append(p.getTitle()).append("\n");
		if (isNonEmpty(p.getDescription()))
			bod.append("Description: ").append(p.getDescription());
		bod.append("\n\n(from robonobo mailmonkey)\n");
		mail.sendMail(fromName(), fromEmail(), toUser.getFriendlyName(), toUser.getEmail(),
				fromUser.getFriendlyName(), fromUser.getEmail(), subject, bod.toString());
	}

	private String fromEmail() {
		return appConfig.getInitParam("fromEmail");
	}

	private String fromName() {
		return appConfig.getInitParam("fromName");
	}

	protected void sendFriendRequest(MidasFriendRequest req, User fromUser, User toUser, Playlist p) throws IOException {
		String subject = fromUser.getFriendlyName() + " would like to be your friend on robonobo";
		StringBuffer bod = new StringBuffer();
		bod.append(fromUser.getFriendlyName()).append("(").append(fromUser.getEmail()).append(")");
		bod.append(" has shared a playlist with you, and would like to become your friend on robonobo.  This means that they will see playlists that you have made public, and you will see theirs.\n\n");
		bod.append("Playlist title: ").append(p.getTitle()).append("\n");
		if (isNonEmpty(p.getDescription()))
			bod.append("Description: ").append(p.getDescription());
		bod.append("\n\nTo add ").append(fromUser.getFriendlyName()).append(" as a friend, click this link:\n\n");
		bod.append(appConfig.getInitParam("friendReqUrlBase")).append(req.getRequestCode());
		bod.append("\n\nCopy and paste this into your browser if clicking does not work.  To ignore this request, just delete this email.");
		bod.append("\n\n(from robonobo mailmonkey)\n");
		mail.sendMail(fromName(), fromEmail(), toUser.getFriendlyName(), toUser.getEmail(),
				fromUser.getFriendlyName(), fromUser.getEmail(), subject, bod.toString());
	}

	protected void sendInvite(MidasInvite invite, User fromUser, Playlist p) throws IOException {
		String subject = fromUser.getFriendlyName() + " has invited you to robonobo";
		StringBuffer bod = new StringBuffer();
		bod.append(fromUser.getFriendlyName()).append("(").append(fromUser.getEmail()).append(")");
		bod.append(" has invited you to robonobo, the social music application that lets you share your music with friends while supporting artists.  As a welcome present, they have sent you a playlist:\n\n");
		bod.append("Title: ").append(p.getTitle()).append("\n");
		if (isNonEmpty(p.getDescription()))
			bod.append("Description: ").append(p.getDescription()).append("\n");
		bod.append("\nTo accept the invitation and start using robonobo, click this link:\n\n");
		bod.append(appConfig.getInitParam("inviteUrlBase")).append(invite.getInviteCode());
		bod.append("\n\nCopy and paste this into your browser if clicking does not work.  To ignore this invitation, just delete this email.");
		bod.append("\n\n(from robonobo mailmonkey)\n");
		mail.sendMail(fromName(), fromEmail(), null, invite.getEmail(), fromUser.getFriendlyName(),
				fromUser.getEmail(), subject, bod.toString());
	}
}
