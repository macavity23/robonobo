package com.robonobo.core.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.DownloadingTrack;
import com.robonobo.core.api.model.SharedTrack;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.User;
import com.robonobo.mina.external.ConnectedNode;


/**
 * RobonobBean provides a Bean API implementation of the 
 * Robonobo framework.
 * 
 * Currently only a sub-set of functionality is implemented 
 * compared to Robonobo, but you should use this if possible.
 * 
 * @author ray
 *
 */
public interface RobonoboBean {

	public void start() throws RobonoboException;

	public void stop();

	public List getDownloads();

	public void addDownload(Stream s, File pathToFile);

	public void addDownload(Stream stream);

	public void startDownload(DownloadingTrack d) throws Exception;

	/** Download must already be added 
	 * @throws Exception */
	public void startDownload(Stream s) throws Exception;

	public void pauseDownload(DownloadingTrack d);

	public void pauseDownload(Stream s);

	public void removeDownload(DownloadingTrack d);

	public void removeDownload(Stream s);

	public Collection<SharedTrack> getShares();

	public Stream addShare(File f)
			throws RobonoboException, IOException;

	public void removeShare(SharedTrack u) throws RobonoboException;

	public void removeShare(Stream s) throws RobonoboException;

	// My Channels
	public Playlist[] getMyChannels();

	public Playlist addMyChannel(String title, String desc)
			throws RobonoboException, IOException;

	public void removeMyChannel(String channelId);

	public void removeMyChannel(Playlist c);

	public void addStreamToMyChannel(String channelId, String streamId)
			throws RobonoboException, IOException;

	public void addStreamToMyChannel(Playlist channel, String streamId)
			throws RobonoboException, IOException;

	public void addStreamToMyChannel(Playlist channel, Stream stream)
			throws RobonoboException, IOException;

	public void removeStreamFromMyChannel(String channelId, String streamId)
			throws RobonoboException, IOException;

	public void removeStreamFromMyChannel(Playlist chan, String streamId)
			throws RobonoboException, IOException;

	// Subscribed Channels
	public Playlist[] getSubscriptions();
	public void addSubscription(Playlist c);
	public void addSubscription(String channelUrl);
	public void removeSubscription(Playlist c);
	public void removeSubscription(String channelId);

	public void setCurrentUser(User u) throws AuthenticationFailedException;
	public User getCurrentUser();

	// Network
	public ConnectedNode[] getConnectedNodes();

	// Util Methods (no associated events)
	public File getAutoDownloadLocation(String channelId);

	/**
	 * @param fetchDir
	 *            The directory to store the autofetched files, or null to turn
	 *            off autofetching
	 */
	public void setAutoDownloadLocation(String channelId, File fetchDir);
	public File getDefaultDownloadLocation(Stream s);
	public String getFriendlyNameForMimeType(String mimeType);

	/**
	 * TODO: move this to the top level and make maxResults a property
	 * Also, latest channels should be a default subscribed channel
	 * 
	 */
	public Playlist[] getLatestChannels();
    
	
    public String getAutoUploadLocation(String channelId);    
    public void setAutoUploadLocation(String channelId, File uploadDir);
}