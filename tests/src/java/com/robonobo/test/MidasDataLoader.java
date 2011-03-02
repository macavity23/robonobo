package com.robonobo.test;

import java.io.*;
import java.sql.*;
import java.util.*;

public class MidasDataLoader {
	static final int NUM_USERS = 100;
	static final int PLAYLISTS_PER_USER = 10;
	static final int STREAMS_PER_PLAYLIST = 10;

	static final String USER_SQL = "insert into users (userid, email, friendlyname, password, verified, updated, invitesleft, imgurl) values(?, ?, ?, ?, ?, ?, ?, ?)";
	static final String PLAYLIST_SQL = "insert into playlists (playlistid, title, description, updated, visibility) values (?, ?, ?, ?, ?)";
	static final String UP_SQL = "insert into playlistidsinuser (userid, playlistid) values (?, ?)";
	static final String PU_SQL = "insert into owneridsinplaylist (playlistid, ownerid) values (?, ?)";
	static final String STREAM_SQL = "insert into streamidsinplaylist (playlistid, streamid, listindex) values (?, ?, ?)";

	String dbUrl;
	String dbUser;
	String dbPwd;
	List<String> sids;
	Random rand = new Random();

	private static void printUsage() {
		System.err.println("Usage: MidasDataLoader <db driver class> <db url> <db user> <db pwd> <sid file>");
	}

	public static void main(String[] args) throws Exception {
		// First, share a bunch of tracks from a robonobo node to have midas read them into a db. Save the stream ids
		// from these tracks into a file, one per line
		if (args.length < 5) {
			printUsage();
			return;
		}
		String dbDriver = args[0];
		String dbUrl = args[1];
		String dbUser = args[2];
		String dbPwd = args[3];
		File sidFile = new File(args[4]);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(sidFile)));
		List<String> sids = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			sids.add(line);
		}
		in.close();
		MidasDataLoader mdl = new MidasDataLoader(dbDriver, dbUrl, dbUser, dbPwd, sids);
		mdl.run();
	}

	public MidasDataLoader(String dbDriver, String dbUrl, String dbUser, String dbPwd, List<String> sids)
			throws Exception {
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
		this.sids = sids;
		Class.forName(dbDriver);
	}

	public void run() throws SQLException {
		Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
		for (int userNum = 0; userNum < NUM_USERS; userNum++) {
			PreparedStatement userSt = conn.prepareStatement(USER_SQL);
			int userId = 1000000 + userNum;
			userSt.setLong(1, userId);
			userSt.setString(2, "testuser-" + userNum + "@robonobo.com");
			userSt.setString(3, "test user " + userNum);
			userSt.setString(4, "password");
			userSt.setBoolean(5, true);
			userSt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			userSt.setInt(7, 100);
			userSt.setString(8, "http://robonobo.com");
			userSt.executeUpdate();
			userSt.close();
			for (int plNum = 0; plNum < PLAYLISTS_PER_USER; plNum++) {
				PreparedStatement plSt = conn.prepareStatement(PLAYLIST_SQL);
				long plId = 1000000 + (userId * 1000) + plNum;
				plSt.setLong(1, plId);
				plSt.setString(2, "Test Playlist " + plNum);
				plSt.setString(3, "flarp");
				plSt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				plSt.setString(5, "all");
				plSt.executeUpdate();
				plSt.close();
				PreparedStatement upSt = conn.prepareStatement(UP_SQL);
				upSt.setLong(1, userId);
				upSt.setLong(2, plId);
				upSt.executeUpdate();
				upSt.close();
				PreparedStatement puSt = conn.prepareStatement(PU_SQL);
				puSt.setLong(1, plId);
				puSt.setLong(2, userId);
				puSt.executeUpdate();
				puSt.close();
				Set<String> playlistSids = new HashSet<String>();
				while (playlistSids.size() < STREAMS_PER_PLAYLIST) {
					playlistSids.add(sids.get(rand.nextInt(sids.size())));
				}
				int trackNum = 0;
				for (String sid : playlistSids) {
					PreparedStatement trSt = conn.prepareStatement(STREAM_SQL);
					trSt.setLong(1, plId);
					trSt.setString(2, sid);
					trSt.setInt(3, trackNum++);
					trSt.executeUpdate();
					trSt.close();
				}
			}
		}
		conn.close();
		System.out.println("Done.");
	}
}
