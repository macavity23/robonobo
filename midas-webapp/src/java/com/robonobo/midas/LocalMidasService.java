package com.robonobo.midas;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.api.model.Library;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.dao.*;
import com.robonobo.midas.model.*;
import com.robonobo.remote.service.MidasService;
import com.twmacinta.util.MD5;

@Service("midas")
public class LocalMidasService implements MidasService {
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private FacebookService facebook;
	@Autowired
	private TwitterService twitter;
	@Autowired
	private MessageService message;
	@Autowired
	private EventService event;
	@Autowired
	private FriendRequestDao friendRequestDao;
	@Autowired
	private InviteDao inviteDao;
	@Autowired
	private LibraryDao libraryDao;
	@Autowired
	private PlaylistDao playlistDao;
	@Autowired
	private StreamDao streamDao;
	@Autowired
	private UserConfigDao userConfigDao;
	@Autowired
	private UserDao userDao;
	Log log = LogFactory.getLog(getClass());
	private long lastPlaylistId = -1;

	@Transactional(readOnly = true)
	public List<MidasUser> getAllUsers() {
		return userDao.getAll();
	}

	public MidasUser getUserByEmail(String email) {
		return userDao.getByEmail(email);
	}

	public MidasUser getUserById(long userId) {
		return userDao.getById(userId);
	}

	@Transactional(rollbackFor = Exception.class)
	public MidasUser createUser(MidasUser user) {
		log.info("Creating user " + user.getEmail());
		user.setVerified(true);
		user.setUpdated(now());
		preventUserXSS(user);
		// This will have its user id set
		MidasUser createdUser = userDao.create(user);
		try {
			message.sendWelcome(createdUser);
		} catch (IOException e) {
			log.error("Error sending welcome mail to " + createdUser.getEmail(), e);
		}
		event.newUser(createdUser);
		return createdUser;
	}

	public MidasUser getUserAsVisibleBy(MidasUser targetU, MidasUser requestor) {
		// If this the user asking for themselves, give them everything. If
		// they're a friend, they get public and friend-visible playlists, but no friends.
		// Otherwise, they get a null object
		MidasUser result;
		if (targetU.equals(requestor)) {
			result = new MidasUser(targetU);
		} else if (targetU.getFriendIds().contains(requestor.getUserId())) {
			result = new MidasUser(targetU);
			result.setPassword(null);
			result.getFriendIds().clear();
			result.setInvitesLeft(0);
			Iterator<Long> iter = result.getPlaylistIds().iterator();
			while (iter.hasNext()) {
				Playlist p = playlistDao.loadPlaylist(iter.next());
				if (p.getVisibility().equals(Playlist.VIS_ME))
					iter.remove();
			}
		} else
			result = null;
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveUser(MidasUser user) {
		preventUserXSS(user);
		user.setUpdated(now());
		userDao.save(user);
	}

	private void preventUserXSS(MidasUser user) {
		user.setFriendlyName(escapeHtml(user.getFriendlyName()));
		user.setDescription(escapeHtml(user.getDescription()));
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteUser(long userId) {
		MidasUser u = userDao.getById(userId);
		// Go through all their playlists - if they are the only owner, delete it, otherwise remove them from the owners
		// list
		for (Long plId : u.getPlaylistIds()) {
			MidasPlaylist p = playlistDao.loadPlaylist(plId);
			p.getOwnerIds().remove(userId);
			if (p.getOwnerIds().size() == 0)
				playlistDao.deletePlaylist(p);
			else
				playlistDao.savePlaylist(p);
		}
		// Go through all their friends, delete this user from their friendids
		for (long friendId : u.getFriendIds()) {
			MidasUser friend = userDao.getById(friendId);
			friend.getFriendIds().remove(userId);
			userDao.save(friend);
		}
		// Delete any pending friend requests to this user
		List<MidasFriendRequest> frs = friendRequestDao.retrieveByRequestee(userId);
		for (MidasFriendRequest fr : frs) {
			friendRequestDao.delete(fr);
		}
		// Finally, delete the user itself
		userDao.delete(u);
	}

	public MidasPlaylist getPlaylistById(long playlistId) {
		return playlistDao.loadPlaylist(playlistId);
	}

	@Override
	public List<MidasPlaylist> getRecentPlaylists(long maxAgeMs) {
		return playlistDao.getRecentPlaylists(maxAgeMs);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public MidasPlaylist newPlaylist(MidasPlaylist playlist) {
		if (playlist.getPlaylistId() > 0)
			throw new Errot("newPlaylist called with non-new playlist!");
		long newPlaylistId;
		synchronized (this) {
			if (lastPlaylistId <= 0)
				lastPlaylistId = playlistDao.getHighestPlaylistId();
			if (lastPlaylistId == Long.MAX_VALUE)
				throw new Errot("playlist ids wrapped!"); // Unlikely
			else
				lastPlaylistId++;
			newPlaylistId = lastPlaylistId;
		}
		playlist.setPlaylistId(newPlaylistId);
		preventPlaylistXSS(playlist);
		savePlaylist(playlist);
		return playlist;
	}

	@Transactional(rollbackFor = Exception.class)
	public void savePlaylist(MidasPlaylist p) {
		if (p.getPlaylistId() <= 0)
			throw new Errot("playlist id is not set");
		preventPlaylistXSS(p);
		playlistDao.savePlaylist(p);
	}

	private void preventPlaylistXSS(MidasPlaylist p) {
		p.setTitle(escapeHtml(p.getTitle()));
		p.setDescription(escapeHtml(p.getDescription()));
	}

	@Transactional(rollbackFor = Exception.class)
	public void deletePlaylist(MidasPlaylist playlist) {
		playlistDao.deletePlaylist(playlist);
	}

	public MidasStream getStreamById(String streamId) {
		return streamDao.loadStream(streamId);
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveStream(MidasStream stream) {
		streamDao.saveStream(stream);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteStream(MidasStream stream) {
		streamDao.deleteStream(stream);
	}

	public Long countUsers() {
		return userDao.getUserCount();
	}

	@Transactional(rollbackFor = Exception.class)
	public MidasInvite createOrUpdateInvite(String email, MidasUser friend, MidasPlaylist pl) {
		MidasInvite result = inviteDao.retrieveByEmail(email);
		if (result == null) {
			// New invite
			result = new MidasInvite();
			result.setEmail(email);
			result.setInviteCode(generateEmailCode(email));
		}
		result.getFriendIds().add(friend.getUserId());
		if (pl != null)
			result.getPlaylistIds().add(pl.getPlaylistId());
		result.setUpdated(now());
		inviteDao.save(result);
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public MidasFriendRequest createOrUpdateFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist pl) {
		MidasFriendRequest result = friendRequestDao.retrieveByUsers(requestor.getUserId(), requestee.getUserId());
		if (result == null) {
			// New friend request
			result = new MidasFriendRequest();
			result.setRequestorId(requestor.getUserId());
			result.setRequesteeId(requestee.getUserId());
			result.setRequestCode(generateEmailCode(requestee.getEmail()));
		}
		if (pl != null)
			result.getPlaylistIds().add(pl.getPlaylistId());
		result.setUpdated(now());
		friendRequestDao.save(result);
		return result;
	}

	public MidasFriendRequest getFriendRequest(String requestCode) {
		return friendRequestDao.retrieveByRequestCode(requestCode);
	}

	@Transactional(rollbackFor = Exception.class)
	public String acceptFriendRequest(MidasFriendRequest req) {
		MidasUser requestor = userDao.getById(req.getRequestorId());
		if (requestor == null)
			return "Requesting user " + req.getRequestorId() + " not found";
		MidasUser requestee = userDao.getById(req.getRequesteeId());
		if (requestee == null)
			return "Requested user " + req.getRequesteeId() + " not found";
		for (Long plId : req.getPlaylistIds()) {
			MidasPlaylist p = playlistDao.loadPlaylist(plId);
			p.getOwnerIds().add(requestee.getUserId());
			playlistDao.savePlaylist(p);
			requestee.getPlaylistIds().add(plId);
		}
		requestor.getFriendIds().add(requestee.getUserId());
		userDao.save(requestor);
		requestee.getFriendIds().add(requestor.getUserId());
		userDao.save(requestee);
		friendRequestDao.delete(req);
		try {
			message.sendFriendConfirmation(requestor, requestee);
		} catch (IOException e) {
			log.error("Error sending friend confirmation", e);
		}
		event.friendRequestAccepted(requestor, requestee);
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	public void ignoreFriendRequest(MidasFriendRequest request) {
		friendRequestDao.delete(request);
	}

	public List<MidasFriendRequest> getPendingFriendRequests(long userId) {
		return friendRequestDao.retrieveByRequestee(userId);
	}

	@Transactional(rollbackFor = Exception.class)
	public void inviteAccepted(long acceptedUserId, String inviteCode) {
		MidasInvite invite = inviteDao.retrieveByInviteCode(inviteCode);
		if (invite != null) {
			MidasUser acceptedUser = getUserById(acceptedUserId);
			// Tell them about their new buddy
			for (Long friendId : invite.getFriendIds()) {
				MidasUser friend = getUserById(friendId);
				if (friend != null) {
					try {
						message.sendFriendConfirmation(friend, acceptedUser);
					} catch (IOException e) {
						log.error("Error sending friend confirmation", e);
					}
				}
			}
			event.inviteAccepted(acceptedUser, invite);
			inviteDao.delete(invite);
		}
	}

	public MidasInvite getInvite(String inviteCode) {
		return inviteDao.retrieveByInviteCode(inviteCode);
	}

	@Override
	public MidasInvite getInviteByEmail(String email) {
		return inviteDao.retrieveByEmail(email);
	}
	
	@Override
	public Library getLibrary(MidasUser u, Date since) {
		Library lib = libraryDao.getLibrary(u.getUserId());
		if (lib == null)
			return null;
		lib.setUserId(u.getUserId());
		if (since != null) {
			Iterator<Entry<String, Date>> it = lib.getTracks().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Date> e = it.next();
				if (since.after(e.getValue()))
					it.remove();
			}
		}
		return lib;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void putLibrary(Library lib) {
		libraryDao.saveLibrary(lib);
	}

	@Override
	public MidasUserConfig getUserConfig(MidasUser u) {
		MidasUserConfig result = userConfigDao.getUserConfig(u.getUserId());
		if (result == null) {
			result = new MidasUserConfig();
			result.setUserId(u.getUserId());
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void putUserConfig(MidasUserConfig newCfg) {
		// Update friends based on facebook & twitter deets
		MidasUser mu = userDao.getById(newCfg.getUserId());
		MidasUserConfig oldCfg = userConfigDao.getUserConfig(newCfg.getUserId());
		facebook.updateFriends(mu, oldCfg, newCfg);
		// TODO twitter
		userConfigDao.saveUserConfig(newCfg);
	}

	@Override
	public void addFriends(long userId, List<Long> friendIds, List<String> friendEmails) {
		MidasUser fromUser = getUserById(userId);
		if (fromUser == null) {
			log.error("addFriends failed: No user with id " + userId);
			return;
		}
		try {
			for (Long friendId : friendIds) {
				MidasUser friend = getUserById(friendId);
				if (friend == null) {
					log.error("addFriends failed for user " + fromUser.getEmail() + " and non existent user id " + friendId);
					continue;
				}
				message.sendFriendRequest(fromUser, friend, null);
				event.friendRequestSent(fromUser, friend);
			}
			for (String email : friendEmails) {
				MidasInvite invite = message.sendInvite(fromUser, email, null);
				event.inviteSent(fromUser, email, invite);
			}
		} catch (IOException e) {
			log.error("addFriends failed", e);
		}
	}

	@Override
	public String requestAccountTopUp(long userId) {
		MidasUser user = userDao.getById(userId);
		if (user == null)
			return "Error: no such user";
		try {
			message.sendTopUpRequest(user);
		} catch (IOException e) {
			log.error("Error requesting topup", e);
			return "Error processing request, please contact help@robonobo.com";
		}
		return "TopUp request received - please check your account in a few hours.";
	}

	private String generateEmailCode(String email) {
		MD5 hash = new MD5();
		hash.Update(email);
		hash.Update(getDateFormat().format(now()));
		return hash.asHex();
	}
}
