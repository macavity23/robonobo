package com.robonobo.test;

import static com.robonobo.common.util.TextUtil.*;

import java.io.*;
import java.util.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.robonobo.common.serialization.*;
import com.robonobo.common.util.TextUtil;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;
import com.robonobo.core.api.proto.CoreApi.UserMsg;

public class MidasSoakTester {
	static final int MAX_HTTP_CONNECTIONS_PER_CLIENT = 4;
	/** percentage */
	static final int PLAYLIST_CHANGE_CHANCE = 10;

	String midasUrl;
	List<Long> userIds;
	int numThreads;
	Random rand = new Random();
	List<String> sids;

	static void printUsage() {
		System.err.println("Usage: MidasSoakTester <sid file> <midas url> <minUserId> <maxUserId> <num threads>");
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 5) {
			printUsage();
			return;
		}
		File sidFile = new File(args[0]);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(sidFile)));
		List<String> sids = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			sids.add(line);
		}
		in.close();
		String midasUrl = args[1];
		long minUserId = Long.parseLong(args[2]);
		long maxUserId = Long.parseLong(args[3]);
		int numThreads = Integer.parseInt(args[4]);
		List<Long> userIds = new ArrayList<Long>();
		for (long i = minUserId; i <= maxUserId; i++) {
			userIds.add(i);
		}
		MidasSoakTester mst = new MidasSoakTester(midasUrl, userIds, sids, numThreads);
		mst.run();
	}

	public MidasSoakTester(String midasUrl, List<Long> userIds, List<String> sids, int numThreads) {
		this.midasUrl = midasUrl;
		this.userIds = userIds;
		this.numThreads = numThreads;
		this.sids = sids;
	}

	public void run() throws Exception {
		for(int i=0;i<numThreads;i++) {
			Thread t = new SoakTestThread(i);
			t.start();
		}
	}

	class SoakTestThread extends Thread {
		int num;
		HttpClient client;

		public SoakTestThread(int num) {
			this.num = num;
			HttpConnectionManagerParams httpParams = new HttpConnectionManagerParams();
			httpParams.setDefaultMaxConnectionsPerHost(MAX_HTTP_CONNECTIONS_PER_CLIENT);
			HttpConnectionManager connMgr = new MultiThreadedHttpConnectionManager();
			connMgr.setParams(httpParams);
			client = new HttpClient(connMgr);
		}
		
		@Override
		public void run() {
			while (true) {
				long userId = userIds.get(rand.nextInt(userIds.size()));
				String userEmail = "testuser-"+userId+"@robonobo.com";
				String pwd = "password";
				UserMsg.Builder ub = UserMsg.newBuilder();
				String userUrl = midasUrl + "users/byemail/"+urlEncode(userEmail);
				try {
					System.out.println(num+": fetching user "+userId);
					getObjectFromUrl(ub, userUrl, userEmail, pwd);
					UserMsg um = ub.build();
					for (long plId : um.getPlaylistIdList()) {
						String playlistUrl = midasUrl + "playlists/" + Long.toHexString(plId);
						PlaylistMsg.Builder pb = PlaylistMsg.newBuilder();
						System.out.println(num+": fetching playlist "+plId);
						getObjectFromUrl(pb, playlistUrl, userEmail, pwd);
						PlaylistMsg pm = pb.build();
						for (String sid : pm.getStreamIdList()) {
							String streamUrl = midasUrl + "streams/"+sid;
							StreamMsg.Builder sb = StreamMsg.newBuilder();
							System.out.println(num+": fetching stream "+sid);
							getObjectFromUrl(sb, streamUrl, userEmail, pwd);
							StreamMsg sm = sb.build();
						}
						// Change this playlist, maybe
						int babyNeedsANewPairOfShoes = rand.nextInt(100);
						if(babyNeedsANewPairOfShoes < PLAYLIST_CHANGE_CHANCE) {
							pb = PlaylistMsg.newBuilder(pm);
							pb.clearStreamId();
							Set<String> newSids = new HashSet<String>();
							while(newSids.size() < pm.getStreamIdCount()) {
								newSids.add(sids.get(rand.nextInt(sids.size())));
							}
							pb.addAllStreamId(newSids);
							System.out.println(num+": updating playlist "+plId);
							putObjectToUrl(pb.build(), playlistUrl, null, userEmail, pwd);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		public void getObjectFromUrl(AbstractMessage.Builder bldr, String url, String username, String password) throws IOException, SerializationException {
			GetMethod get = new GetMethod(url);
			try {
				if (username != null) {
					client.getState().setAuthenticationPreemptive(true);
					client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
				}
				int statusCode = client.executeMethod(get);
				switch (statusCode) {
				case 200:
					bldr.mergeFrom(get.getResponseBodyAsStream());
					break;
				case 404:
					throw new ResourceNotFoundException("Server could not find resource for " + url);
				case 401:
					throw new UnauthorizedException("Server did not allow us to access url " + url
							+ " with supplied credentials");
				case 500:
					throw new IOException("Unable to get object from url '" + url + "', server said: "
							+ get.getResponseBodyAsString());
				default:
					throw new IOException("Url '" + url + "' replied with status " + statusCode);
				}
			} finally {
				get.releaseConnection();
			}
		}
		
		public void putObjectToUrl(GeneratedMessage msg, String url, AbstractMessage.Builder bldr, String username, String password) throws IOException {
			PutMethod put = new PutMethod(url);
			try {
				put.setRequestEntity(new ByteArrayRequestEntity(msg.toByteArray()));
				// set auth
				client.getState().setAuthenticationPreemptive(true);
				client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
				int statusCode = client.executeMethod(put);
				switch (statusCode) {
				case 200:
					if(bldr != null) {
						byte[] bodBytes = put.getResponseBody();
						bldr.mergeFrom(bodBytes);
					}
					return;
				default:
					throw new IOException("Server replied with status " + statusCode + ": " + put.getResponseBodyAsString());
				}
			} finally {
				put.releaseConnection();
			}
		}
	}
}
