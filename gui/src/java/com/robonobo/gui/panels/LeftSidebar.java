package com.robonobo.gui.panels;

import static com.robonobo.gui.RoboColor.*;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.UserPlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.components.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.ActiveSearchListModel;
import com.robonobo.gui.model.SearchResultTableModel;

@SuppressWarnings("serial")
public class LeftSidebar extends JPanel implements UserPlaylistListener {
	List<LeftSidebarComponent> sideBarComps = new ArrayList<LeftSidebarComponent>();
	RobonoboFrame frame;
	private ActiveSearchList activeSearchList;
	private MyLibrarySelector myMusic;
	private TaskListSelector taskList;
	private boolean showTasks = false;
	private NewPlaylistSelector newPlaylist;
	private PlaylistList myPlList;
	private FriendTree friendTree;
	private PublicPlaylistTree pubPlTree;
	private boolean showPublicPlaylists = false;
	Log log = LogFactory.getLog(getClass());
	private JPanel sideBarPanel;
	private SearchField searchField;

	public LeftSidebar(RobonoboFrame frame) {
		this.frame = frame;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

		sideBarPanel = new JPanel();
		final JScrollPane treeListScroller = new JScrollPane(sideBarPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(treeListScroller);
		treeListScroller.getViewport().getView().setBackground(Color.WHITE);
		sideBarPanel.setLayout(new BoxLayout(sideBarPanel, BoxLayout.Y_AXIS));
		sideBarPanel.setBackground(MID_GRAY);

		searchField = new SearchField(this);
		sideBarPanel.add(searchField);

		activeSearchList = new ActiveSearchList(this, frame);
		activeSearchList.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		sideBarPanel.add(activeSearchList);
		sideBarComps.add(activeSearchList);

		pubPlTree = new PublicPlaylistTree(this, frame);
		pubPlTree.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
		sideBarComps.add(pubPlTree);
		
		friendTree = new FriendTree(this, frame);
		friendTree.setBorder(BorderFactory.createEmptyBorder(5, 10, 3, 10));
		sideBarPanel.add(friendTree);
		sideBarComps.add(friendTree);

		myMusic = new MyLibrarySelector(this, frame);
		sideBarPanel.add(myMusic);
		sideBarComps.add(myMusic);

		taskList = new TaskListSelector(this, frame);
		sideBarComps.add(taskList);
		
		newPlaylist = new NewPlaylistSelector(this, frame);
		sideBarPanel.add(newPlaylist);
		sideBarComps.add(newPlaylist);

		myPlList = new PlaylistList(this, frame);
		sideBarPanel.add(myPlList);
		sideBarComps.add(myPlList);

		JPanel spacerPanel = new JPanel();
		spacerPanel.setLayout(new BoxLayout(spacerPanel, BoxLayout.X_AXIS));
		spacerPanel.setPreferredSize(new Dimension(200, 5));
		spacerPanel.setOpaque(false);
		add(spacerPanel);
		StatusPanel statusPnl = new StatusPanel(frame);
		sideBarComps.add(statusPnl.getBalanceLbl());
		add(statusPnl);
		
		frame.getController().addUserPlaylistListener(this);
	}

	private void relayoutSidebar() {
		sideBarPanel.removeAll();
		sideBarPanel.add(searchField);
		sideBarPanel.add(activeSearchList);
		if(showPublicPlaylists)
			sideBarPanel.add(pubPlTree);
		sideBarPanel.add(friendTree);
		sideBarPanel.add(myMusic);
		if(showTasks)
			sideBarPanel.add(taskList);
		sideBarPanel.add(newPlaylist);
		sideBarPanel.add(myPlList);
		sideBarPanel.revalidate();
	}
	
	public void showTaskList(boolean show) {
		if(showTasks == show)
			return;
		showTasks = show;
		relayoutSidebar();
	}
	
	public void showPublicPlaylists(boolean show) {
		if(showPublicPlaylists == show)
			return;
		showPublicPlaylists = show;
		relayoutSidebar();
	}
	
	public void selectForContentPanel(String cpName) {
		if(cpName.equals("mymusiclibrary"))
			myMusic.setSelected(true);
		else if(cpName.equals("newplaylist"))
			newPlaylist.setSelected(true);
		else if(cpName.startsWith("search/"))
			activeSearchList.selectForQuery(cpName.substring("search/".length()));
		else if(cpName.startsWith("playlist/")) {
			long plId = Long.parseLong(cpName.substring("playlist/".length()));
			Playlist p = frame.getController().getPlaylist(plId);
			selectForPlaylist(p);
		} else if(cpName.startsWith("library/")) {
			long uid = Long.parseLong(cpName.substring("library/".length()));
			friendTree.selectForLibrary(uid);
		} else
			log.error("Couldn't select content panel: "+cpName);
	}
	
	public void selectForPlaylist(Playlist p) {
		long plId = p.getPlaylistId();
		if(myPlList.getModel().hasPlaylist(plId))
			myPlList.selectPlaylist(p);
		else if(friendTree.getModel().hasPlaylist(plId))
			friendTree.selectForPlaylist(plId);
		else {
			// Public playlist
			if(!pubPlTree.getModel().hasPlaylist(plId)) {
				PlaylistConfig pc = frame.getController().getPlaylistConfig(p.getPlaylistId());
				// TODO Don't use friend panel, have separate public one
				// TODO Need to change playlistChanged() as well in case public panel gets replaced
				OtherPlaylistContentPanel cp = new OtherPlaylistContentPanel(frame, p, pc);
				frame.getMainPanel().addContentPanel("playlist/"+plId, cp);
				pubPlTree.getModel().addPlaylist(p);
			}
			pubPlTree.selectForPlaylist(plId);
			showPublicPlaylists(true);
		}
	}
	
	public void showPlaylist(final long playlistId) {
		// Make sure we've got the playlist before we go to the UI thread, or it might hang while we contact midas
		final Playlist p = frame.getController().getPlaylist(playlistId); 
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				selectForPlaylist(p);
			}
		});
	}
	
	public void searchAdded(String query) {
		ActiveSearchListModel model = (ActiveSearchListModel) activeSearchList.getModel();
		SearchResultTableModel srtm = model.addSearch(query);
		if (srtm != null) {
			SearchResultContentPanel srcm = new SearchResultContentPanel(frame, srtm);
			frame.getMainPanel().addContentPanel("search/" + query, srcm);
		}
		activeSearchList.setSelectedIndex(model.indexOfQuery(query));
		clearSelectionExcept(activeSearchList);
		frame.getMainPanel().selectContentPanel("search/" + query);
	}

	public void selectMyMusic() {
		myMusic.setSelected(true);
	}

	public void selectMyPlaylist(Playlist p) {
		myPlList.selectPlaylist(p);
	}

	public void clearSelectionExcept(LeftSidebarComponent selCmp) {
		for (LeftSidebarComponent cmp : sideBarComps) {
			if (cmp != selCmp)
				cmp.relinquishSelection();
		}
	}
	
	@Override
	public void loggedIn() {
		// Do nothing
	}

	@Override
	public void allUsersAndPlaylistsUpdated() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void userChanged(User u) {
		// Do nothing
		// TODO When we have a panel for friends, create it here
	}

	@Override
	public void playlistChanged(Playlist p) {
		// I'm not sure if this is the best place to create the panels, but this is the lowest common ancestor of the
		// playlistlist and the friendtree, and we need to think about both
		String panelName = "playlist/" + p.getPlaylistId();
		PlaylistConfig pc = frame.getController().getPlaylistConfig(p.getPlaylistId());
		ContentPanel pPanel = frame.getMainPanel().getContentPanel(panelName);
		long myUserId = frame.getController().getMyUser().getUserId();
		// TODO If there is a public playlist with this plId, replace it with a friend/my playlist
		if (pPanel == null) {
			// Create playlist panel
			if (p.getOwnerIds().contains(myUserId))
				frame.getMainPanel().addContentPanel(panelName, new MyPlaylistContentPanel(frame, p, pc));
			else
				frame.getMainPanel().addContentPanel(panelName, new OtherPlaylistContentPanel(frame, p, pc));
		} else {
			// Playlist panel already exists - check to see if I'm now an owner and wasn't (or vice versa)
			if((pPanel instanceof MyPlaylistContentPanel) && !p.getOwnerIds().contains(myUserId)) {
				frame.getMainPanel().addContentPanel(panelName, new OtherPlaylistContentPanel(frame, p, pc));
			} else if((pPanel instanceof OtherPlaylistContentPanel) && p.getOwnerIds().contains(myUserId)) {
				frame.getMainPanel().addContentPanel(panelName, new MyPlaylistContentPanel(frame, p, pc));
			}
		}
	}
	
	@Override
	public void libraryChanged(Library lib) {
		String panelName = "library/"+lib.getUserId();
		ContentPanel lPanel = frame.getMainPanel().getContentPanel(panelName);
		if(lPanel == null)
			frame.getMainPanel().addContentPanel(panelName, new FriendLibraryContentPanel(frame, lib));
	}
	
	@Override
	public void userConfigChanged(UserConfig cfg) {
		// Do nothing
	}
}
