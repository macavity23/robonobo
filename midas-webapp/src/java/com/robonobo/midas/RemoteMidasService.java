package com.robonobo.midas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.remoting.*;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.transport.Connector;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.remote.RemoteCall;
import com.robonobo.core.api.proto.CoreApi.FriendRequestMsg;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;
import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;
import com.robonobo.core.api.proto.CoreApi.UserMsg;
import com.robonobo.midas.model.*;
import com.robonobo.remote.service.MidasService;

/** The server end of a remote midas service (client end is RemoteMidasFacade) TODO Use EJB3s instead...
 * 
 * @author macavity */
public class RemoteMidasService implements ServerInvocationHandler, InitializingBean, DisposableBean {
	@Autowired
	MidasService midas;
	@Autowired
	PlatformTransactionManager transactionManager;
	TransactionTemplate transTemplate;
	Connector connector;
	String secret;
	Log log = LogFactory.getLog(getClass());

	/** @param url
	 *            The jboss-remoting url on which to listen
	 * @param secret
	 *            The sekrit string that must be passed with all calls */
	public RemoteMidasService(String url, String secret) throws Exception {
		this.secret = secret;
		log.info("Settin up remote midas service on " + url);
		InvokerLocator locator = new InvokerLocator(url);
		connector = new Connector();
		connector.setInvokerLocator(locator.getLocatorURI());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		transTemplate = new TransactionTemplate(transactionManager);
		log.info("Starting remote midas service");
		connector.start();
		connector.addInvocationHandler("midas", this);
	}

	@Override
	public void destroy() throws Exception {
		log.info("Stopping remote midas service");
		connector.stop();
	}

	public Object invoke(InvocationRequest req) throws Throwable {
		Object obj = req.getParameter();
		if (!(obj instanceof RemoteCall)) {
			log.error("Remote invocation with parameter " + obj.getClass().getName());
			throw new IllegalArgumentException("Invalid param");
		}
		final RemoteCall params = (RemoteCall) obj;
		if (!secret.equals(params.getSecret())) {
			log.error("Remote invocation with invalid secret '" + params.getSecret() + "'");
			throw new IllegalArgumentException("Invalid secret");
		}
		final String method = params.getMethodName();
		// Make sure everything happens inside a transaction
		return transTemplate.execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus ts) {
				try {
					if (method.equals("getUserByEmail")) {
						return getUserByEmail(params);
					} else if (method.equals("getUserById")) {
						return getUserById(params);
					} else if (method.equals("saveUser")) {
						saveUser(params);
						return null;
					} else if (method.equals("getUserAsVisibleBy")) {
						return getUserAsVisibleBy(params);
					} else if (method.equals("getPlaylistById")) {
						return getPlaylistById(params);
					} else if (method.equals("savePlaylist")) {
						savePlaylist(params);
						return null;
					} else if (method.equals("deletePlaylist")) {
						deletePlaylist(params);
						return null;
					} else if (method.equals("getStreamById")) {
						return getStreamById(params);
					} else if (method.equals("saveStream")) {
						saveStream(params);
						return null;
					} else if (method.equals("deleteStream")) {
						deleteStream(params);
						return null;
					} else if (method.equals("countUsers")) {
						return countUsers();
					} else if (method.equals("createUser")) {
						return createUser(params);
					} else if (method.equals("getAllUsers")) {
						return getAllUsers();
					} else if (method.equals("deleteUser")) {
						deleteUser(params);
						return null;
					} else if (method.equals("createOrUpdateFriendRequest")) {
						return createOrUpdateFriendRequest(params);
					} else if (method.equals("getFriendRequest")) {
						return getFriendRequest(params);
					} else if (method.equals("getPendingFriendRequests")) {
						return getPendingFriendRequests(params);
					} else if (method.equals("ignoreFriendRequest")) {
						ignoreFriendRequest(params);
						return null;
					} else if (method.equals("acceptFriendRequest")) {
						return acceptFriendRequest(params);
					} else if (method.equals("createOrUpdateInvite")) {
						return createOrUpdateInvite(params);
					} else if (method.equals("inviteAccepted")) {
						inviteAccepted(params);
						return null;
					} else if (method.equals("getInvite")) {
						return getInvite(params);
					} else if(method.equals("getInviteByEmail")) {
						return getInviteByEmail(params);
					} else if (method.equals("getUserConfig")) {
						return getUserConfig(params);
					} else if (method.equals("putUserConfig")) {
						putUserConfig(params);
						return null;
					} else if (method.equals("addFriends")) {
						addFriends(params);
						return null;
					} else if (method.equals("requestTopUp")) {
						return requestTopUp(params);
					} else
						throw new IllegalArgumentException("Invalid method");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private String acceptFriendRequest(RemoteCall params) throws IOException {
		FriendRequestMsg msg = FriendRequestMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		return midas.acceptFriendRequest(new MidasFriendRequest(msg));
	}

	private byte[] getFriendRequest(RemoteCall params) {
		MidasFriendRequest fr = midas.getFriendRequest((String) params.getArg());
		return fr.toMsg().toByteArray();
	}

	private byte[][] getPendingFriendRequests(RemoteCall params) {
		List<MidasFriendRequest> frList = midas.getPendingFriendRequests((Long) params.getArg());
		byte[][] result = new byte[frList.size()][];
		for (int i = 0; i < frList.size(); i++) {
			result[i] = frList.get(i).toMsg().toByteArray();
		}
		return result;
	}

	private void ignoreFriendRequest(RemoteCall params) throws IOException {
		FriendRequestMsg msg = FriendRequestMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		midas.ignoreFriendRequest(new MidasFriendRequest(msg));
	}

	private void inviteAccepted(RemoteCall params) {
		Long acceptedUserId = (Long) params.getArg();
		String inviteCode = (String) params.getExtraArgs().get(0);
		midas.inviteAccepted(acceptedUserId, inviteCode);
	}

	private Object getInvite(RemoteCall params) {
		MidasInvite invite = midas.getInvite((String) params.getArg());
		if (invite == null)
			return null;
		return invite.toMsg().toByteArray();
	}

	private Object getInviteByEmail(RemoteCall params) {
		MidasInvite invite = midas.getInviteByEmail((String) params.getArg());
		if (invite == null)
			return null;
		return invite.toMsg().toByteArray();
	}

	private void addFriends(RemoteCall params) {
		Long userId = (Long) params.getArg();
		if (params.getExtraArgs().size() < 2)
			throw new Errot();
		// Java won't cast Object[] into Long[] automatically, rubbish
		Object[] fidArr = (Object[]) params.getExtraArgs().get(0);
		List<Long> fidList = new ArrayList<Long>();
		for (Object o : fidArr) {
			fidList.add((Long) o);
		}
		Object[] strArr = (Object[]) params.getExtraArgs().get(1);
		List<String> strList = new ArrayList<String>();
		for (Object o : strArr) {
			strList.add((String) o);
		}
		midas.addFriends(userId, fidList, strList);
	}

	private byte[] requestTopUp(RemoteCall params) {
		Long userId = (Long) params.getArg();
		return midas.requestAccountTopUp(userId).getBytes();
	}

	private Object getUserConfig(RemoteCall params) {
		MidasUser u = midas.getUserById((Long) params.getArg());
		MidasUserConfig uc = midas.getUserConfig(u);
		return uc.toMsg().toByteArray();
	}

	private void putUserConfig(RemoteCall params) throws IOException {
		UserConfigMsg ucm = UserConfigMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasUserConfig newMuc = new MidasUserConfig(ucm);
		MidasUser u = midas.getUserById(newMuc.getUserId());
		MidasUserConfig muc = midas.getUserConfig(u);
		if (muc == null)
			muc = newMuc;
		else
			muc.mergeFrom(newMuc);
		midas.putUserConfig(muc);
	}

	private void deleteStream(RemoteCall params) throws IOException {
		StreamMsg msg = StreamMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasStream s = new MidasStream(msg);
		midas.deleteStream(s);
	}

	private void saveStream(RemoteCall params) throws IOException {
		StreamMsg msg = StreamMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasStream s = new MidasStream(msg);
		midas.saveStream(s);
	}

	private Object getStreamById(RemoteCall params) {
		String sId = (String) params.getArg();
		return midas.getStreamById(sId).toMsg().toByteArray();
	}

	private void deletePlaylist(RemoteCall params) throws IOException {
		PlaylistMsg msg = PlaylistMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasPlaylist pl = new MidasPlaylist(msg);
		midas.deletePlaylist(pl);
	}

	private void savePlaylist(RemoteCall params) throws IOException {
		PlaylistMsg msg = PlaylistMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasPlaylist pl = new MidasPlaylist(msg);
		midas.savePlaylist(pl);
	}

	private Object getPlaylistById(RemoteCall params) {
		Long plId = (Long) params.getArg();
		return midas.getPlaylistById(plId).toMsg().toByteArray();
	}

	private Object getUserAsVisibleBy(RemoteCall params) throws IOException {
		UserMsg targetMsg = UserMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		UserMsg reqMsg = UserMsg.newBuilder().mergeFrom((byte[]) params.getExtraArgs().get(0)).build();
		MidasUser target = new MidasUser(targetMsg);
		MidasUser requestor = new MidasUser(reqMsg);
		return midas.getUserAsVisibleBy(target, requestor).toMsg(true).toByteArray();
	}

	private Object createOrUpdateFriendRequest(RemoteCall params) throws IOException {
		UserMsg requestorMsg = UserMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		UserMsg requesteeMsg = UserMsg.newBuilder().mergeFrom((byte[]) params.getExtraArgs().get(0)).build();
		PlaylistMsg plMsg = PlaylistMsg.newBuilder().mergeFrom((byte[]) params.getExtraArgs().get(1)).build();
		return midas.createOrUpdateFriendRequest(new MidasUser(requestorMsg), new MidasUser(requesteeMsg), new MidasPlaylist(plMsg)).toMsg().toByteArray();
	}

	private Object createOrUpdateInvite(RemoteCall params) throws IOException {
		String email = (String) params.getArg();
		UserMsg friendMsg = UserMsg.newBuilder().mergeFrom((byte[]) params.getExtraArgs().get(0)).build();
		PlaylistMsg plMsg = PlaylistMsg.newBuilder().mergeFrom((byte[]) params.getExtraArgs().get(1)).build();
		return midas.createOrUpdateInvite(email, new MidasUser(friendMsg), new MidasPlaylist(plMsg)).toMsg().toByteArray();
	}

	private void saveUser(RemoteCall params) throws IOException {
		UserMsg msg = UserMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasUser user = new MidasUser(msg);
		midas.saveUser(user);
	}

	private Object createUser(RemoteCall params) throws IOException {
		UserMsg msg = UserMsg.newBuilder().mergeFrom((byte[]) params.getArg()).build();
		MidasUser user = new MidasUser(msg);
		return midas.createUser(user).toMsg(true).toByteArray();
	}

	private Object getUserById(RemoteCall params) {
		Long userId = (Long) params.getArg();
		MidasUser user = midas.getUserById(userId);
		if (user == null)
			return null;
		return user.toMsg(true).toByteArray();
	}

	private Object getUserByEmail(RemoteCall params) {
		String email = (String) params.getArg();
		MidasUser user = midas.getUserByEmail(email);
		if (user == null)
			return null;
		return user.toMsg(true).toByteArray();
	}

	private Long countUsers() {
		return midas.countUsers();
	}

	private Object getAllUsers() {
		List<MidasUser> allUsers = midas.getAllUsers();
		byte[][] arrOfArrs = new byte[allUsers.size()][];
		for (int i = 0; i < arrOfArrs.length; i++) {
			arrOfArrs[i] = allUsers.get(i).toMsg(true).toByteArray();
		}
		return arrOfArrs;
	}

	private void deleteUser(RemoteCall params) {
		midas.deleteUser((Long) params.getArg());
	}

	public void addListener(InvokerCallbackHandler arg0) {
		// Do nothing
	}

	public void removeListener(InvokerCallbackHandler arg0) {
		// Do nothing
	}

	public void setInvoker(ServerInvoker arg0) {
		// Do nothing
	}

	public void setMBeanServer(MBeanServer arg0) {
		// Do nothing
	}
}
