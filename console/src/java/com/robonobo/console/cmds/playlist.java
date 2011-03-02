package com.robonobo.console.cmds;

import static com.robonobo.common.util.TextUtil.*;

import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.doomdark.uuid.UUIDGenerator;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.FileUtil;
import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.PlaylistConfig;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.User;

public class playlist implements ConsoleCommand {

	public void printHelp(PrintWriter out) {
		out.println("'playlist list [mine|<email>]' lists playlists\n"
				+ "'playlist show [mine|<email>] <title>' lists the tracks in the specified playlist\n"
				+ "'playlist autodl [mine|<email>] <title> [true|false]' turns auto-downloading on or off for the playlist\n"
				+ "'playlist vis <title> [all|me|friends]' sets my playlist visibility\n"
				+ "'playlist create <title> <desc>' creates a playlist\n"
				+ "'playlist delete <title>' deletes playlist\n"
				+ "'playlist add <title> <streamid>' adds a share to the playlist\n"
				+ "'playlist remove <title> <streamid>' removes an item from the playlist\n"
				+ "'playlist share <title> <email>' shares a playlist with someone else");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RobonoboController control = console.getController();
		if (args.length < 2) {
			printHelp(out);
			return;
		}
		if (args[0].equals("list"))
			doList(control, args, out);
		else if (args[0].equals("show"))
			doShow(control, args, out);
		else if (args[0].equals("autodl"))
			doAutoDl(control, args, out);
		else if (args[0].equals("vis"))
			doVis(control, args, out);
		else if (args[0].equals("create"))
			doCreate(control, args, out);
		else if (args[0].equals("delete"))
			doDelete(control, args, out);
		else if (args[0].equals("add"))
			doAdd(control, args, out);
		else if (args[0].equals("remove"))
			doRemove(control, args, out);
		else if (args[0].equals("share"))
			doShare(control, args, out);
		else
			printHelp(out);
	}

	private void doCreate(RobonoboController control, String[] args, PrintWriter out) throws Exception {
		if (args.length < 3) {
			printHelp(out);
			return;
		}
		Playlist p = new Playlist();
		p.setTitle(args[1]);
		p.setDescription(args[2]);
		p.getOwnerIds().add(control.getMyUser().getUserId());
		control.addOrUpdatePlaylist(p);
	}

	private void doDelete(RobonoboController control, String[] args, PrintWriter out) throws Exception {
		Playlist p = getNamedPlaylist(control, control.getMyUser(), args[1]);
		if (p == null) {
			out.println("No such playlist '" + args[1] + "'");
			return;
		}
		control.nukePlaylist(p);
	}

	private void doAdd(RobonoboController control, String[] args, PrintWriter out) throws Exception {
		if (args.length < 3) {
			printHelp(out);
			return;
		}
		Playlist p = getNamedPlaylist(control, control.getMyUser(), args[1]);
		if (p == null) {
			out.println("No such playlist '" + args[1] + "'");
			return;
		}
		p.getStreamIds().add(args[2]);
		control.addOrUpdatePlaylist(p);
	}

	private void doRemove(RobonoboController control, String[] args, PrintWriter out) throws Exception {
		if (args.length < 3) {
			printHelp(out);
			return;
		}
		Playlist p = getNamedPlaylist(control, control.getMyUser(), args[1]);
		if (p == null) {
			out.println("No such playlist '" + args[1] + "'");
			return;
		}
		if (p.getStreamIds().remove(args[2]))
			control.addOrUpdatePlaylist(p);
		else
			out.println("Stream id '" + args[2] + "' is not in playlist '" + p.getTitle() + "'");
	}

	private void doVis(RobonoboController control, String[] args, PrintWriter out) throws Exception {
		if (args.length < 3) {
			printHelp(out);
			return;
		}
		Playlist p = getNamedPlaylist(control, control.getMyUser(), args[1]);
		if (p == null) {
			out.println("No such playlist '" + args[1] + "'");
			return;
		}
		p.setVisibility(args[2]);
		control.addOrUpdatePlaylist(p);
	}

	private void doAutoDl(final RobonoboController control, String[] args, PrintWriter out) {
		if (args.length < 4) {
			printHelp(out);
			return;
		}
		final User u = args[1].equalsIgnoreCase("mine") ? control.getMyUser() : control.getUser(args[1]);
		if (u == null) {
			out.println("No user found with email " + args[1]);
			return;
		}
		final Playlist p = getNamedPlaylist(control, u, args[2]);
		if (p == null) {
			out.println("No such playlist '" + args[2] + "'");
			return;
		}
		if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
			printHelp(out);
			return;
		}
		PlaylistConfig pc = control.getPlaylistConfig(p.getPlaylistId());
		pc.setItem("autoDownload", args[3].toLowerCase());
		control.putPlaylistConfig(pc);
		if (args[3].equalsIgnoreCase("true")) {
			control.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					control.checkPlaylistUpdate(p.getPlaylistId());
				}
			});
		}
	}

	private void doShow(RobonoboController control, String[] args, PrintWriter out) {
		if (args.length < 3) {
			printHelp(out);
			return;
		}
		final User u = args[1].equalsIgnoreCase("mine") ? control.getMyUser() : control.getUser(args[1]);
		if (u == null) {
			out.println("No user found with email " + args[1]);
			return;
		}
		final Playlist p = getNamedPlaylist(control, u, args[2]);
		if (p == null) {
			out.println("No such playlist '" + args[2] + "'");
			return;
		}
		out.println("Title: " + p.getTitle());
		out.println("Desc: " + p.getDescription());
		if (p.getStreamIds().size() == 0)
			out.println("No tracks");
		else {
			out.println(rightPadOrTruncate("Title", 32) + rightPadOrTruncate("Artist", 24)
					+ rightPadOrTruncate("Album", 24) + rightPadOrTruncate("Duration", 10)
					+ rightPadOrTruncate("Size", 8) + rightPadOrTruncate("Id", 40));
			for (String streamId : p.getStreamIds()) {
				Stream s = control.getStream(streamId);
				out.println(rightPadOrTruncate(s.getTitle(), 32) + rightPadOrTruncate(s.getAttrValue("artist"), 24)
						+ rightPadOrTruncate(s.getAttrValue("album"), 24)
						+ rightPadOrTruncate(formatDurationHMS(s.getDuration()), 10)
						+ rightPadOrTruncate(FileUtil.humanReadableSize(s.getSize()), 8)
						+ rightPadOrTruncate(s.getStreamId(), 40));
			}
		}
	}

	private void doList(RobonoboController control, String[] args, PrintWriter out) {
		final User u = args[1].equalsIgnoreCase("mine") ? control.getMyUser() : control.getUser(args[1]);
		if (u == null) {
			out.println("No user found with email " + args[1]);
			return;
		}
		if (u.getPlaylistIds().size() == 0)
			out.println("No playlists for user " + u.getEmail());
		else {
			for (Long plId : u.getPlaylistIds()) {
				Playlist p = control.getPlaylist(plId);
				if (p != null)
					out.println("Title: '" + p.getTitle() + "', Desc: " + p.getDescription());
			}
		}
	}

	private void doShare(RobonoboController control, String[] args, PrintWriter out) {
		if (args.length < 3) {
			printHelp(out);
			return;
		}
		User me = control.getMyUser();
		Playlist p = getNamedPlaylist(control, me, args[1]);
		if (p == null) {
			out.println("No such playlist: " + args[1]);
			return;
		}
		// Check if this is going to a friend - if not, check we have enough
		// invites
		String sendEmail = args[2];
		long sendUserId = -1;
		for (Long friendId : me.getFriendIds()) {
			User friend = control.getUser(friendId);
			if (friend.getEmail().equals(sendEmail)) {
				sendUserId = friendId;
				break;
			}
		}
		Set<String> emails = new HashSet<String>();
		Set<Long> friendIds = new HashSet<Long>();
		if(sendUserId > 0)
			friendIds.add(sendUserId);
		else
			emails.add(sendEmail);
		try {
			control.sharePlaylist(p, friendIds, emails);
		} catch (RobonoboException e) {
			out.println("Error sharing playlist: "+e.getMessage());
			return;
		}
		out.println("Playlist shared successfully.");
	}

	private Playlist getNamedPlaylist(RobonoboController control, User u, String plTitle) {
		for (Long playlistId : u.getPlaylistIds()) {
			Playlist p = control.getPlaylist(playlistId);
			if (p.getTitle().equals(plTitle))
				return p;
		}
		return null;
	}
}
