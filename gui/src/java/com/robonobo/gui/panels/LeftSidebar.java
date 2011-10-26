package com.robonobo.gui.panels;

import static com.robonobo.gui.RoboColor.*;

import java.awt.Color;
import java.awt.Dimension;
import java.util.*;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.*;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.components.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.ActiveSearchListModel;
import com.robonobo.gui.model.SearchResultTableModel;

@SuppressWarnings("serial")
public class LeftSidebar extends JPanel implements PlaylistListener, LibraryListener {
	List<LeftSidebarComponent> sideBarComps = new ArrayList<LeftSidebarComponent>();
	RobonoboFrame frame;
	private ActiveSearchList activeSearchList;
	private MyLibrarySelector myLib;
	private TaskListSelector taskList;
	private boolean showTasks = false;
	private NewPlaylistSelector newPlaylist;
	private PlaylistList myPlList;
	private FriendTree friendTree;
	private PublicPlaylistTree pubPlTree;
	private boolean showPublicPlaylists = false;
	List<SpecialPlaylistSelector> spSels = new ArrayList<SpecialPlaylistSelector>();
	Log log = LogFactory.getLog(getClass());
	private JPanel sideBarPanel;
	private SearchField searchField;

	public LeftSidebar(RobonoboFrame frame) {
		this.frame = frame;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		sideBarPanel = new JPanel();
		final JScrollPane treeListScroller = new JScrollPane(sideBarPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
		myLib = new MyLibrarySelector(this, frame);
		sideBarPanel.add(myLib);
		sideBarComps.add(myLib);
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
		frame.ctrl.addPlaylistListener(this);
		frame.ctrl.addLibraryListener(this);
		frame.ctrl.addLoginListener(new LoginAdapter() {
			public void loginSucceeded(User me) {
				for (SpecialPlaylistSelector sel : spSels) {
					sideBarComps.remove(sel);
				}
				spSels.clear();
				relayoutSidebar();
			}
		});
	}

	private void relayoutSidebar() {
		sideBarPanel.removeAll();
		sideBarPanel.add(searchField);
		sideBarPanel.add(activeSearchList);
		if (showPublicPlaylists)
			sideBarPanel.add(pubPlTree);
		sideBarPanel.add(friendTree);
		sideBarPanel.add(myLib);
		if (showTasks)
			sideBarPanel.add(taskList);
		for (SpecialPlaylistSelector spSel : spSels) {
			sideBarPanel.add(spSel);
		}
		sideBarPanel.add(newPlaylist);
		sideBarPanel.add(myPlList);
		sideBarPanel.revalidate();
	}

	public void showTaskList(boolean show) {
		if (showTasks == show)
			return;
		showTasks = show;
		relayoutSidebar();
	}

	public void showPublicPlaylists(boolean show) {
		if (showPublicPlaylists == show)
			return;
		showPublicPlaylists = show;
		relayoutSidebar();
	}

	public void selectForContentPanel(String cpName) {
		if (cpName.equals("mymusiclibrary"))
			myLib.setSelected(true);
		else if (cpName.equals("newplaylist"))
			newPlaylist.setSelected(true);
		else if (cpName.startsWith("search/"))
			activeSearchList.selectForQuery(cpName.substring("search/".length()));
		else if (cpName.startsWith("playlist/")) {
			long plId = Long.parseLong(cpName.substring("playlist/".length()));
			Playlist p = frame.ctrl.getKnownPlaylist(plId);
			selectForPlaylist(p);
		} else if (cpName.startsWith("library/")) {
			long uid = Long.parseLong(cpName.substring("library/".length()));
			friendTree.selectForLibrary(uid);
		} else
			log.error("Couldn't select content panel: " + cpName);
	}

	public void selectForPlaylist(Playlist p) {
		long plId = p.getPlaylistId();
		for (SpecialPlaylistSelector sel : spSels) {
			if (sel.p.getPlaylistId() == plId) {
				sel.setSelected(true);
				return;
			}
		}
		if (myPlList.getModel().hasPlaylist(plId))
			myPlList.selectPlaylist(p);
		else if (friendTree.getModel().hasPlaylist(plId))
			friendTree.selectForPlaylist(plId);
		else {
			// Public playlist
			if (!pubPlTree.getModel().hasPlaylist(plId)) {
				PlaylistConfig pc = frame.ctrl.getPlaylistConfig(p.getPlaylistId());
				// TODO Don't use friend panel, have separate public one
				// TODO Need to change playlistChanged() as well in case public panel gets replaced
				OtherPlaylistContentPanel cp = new OtherPlaylistContentPanel(frame, p, pc);
				frame.mainPanel.addContentPanel("playlist/" + plId, cp);
				pubPlTree.getModel().addPlaylist(p);
			}
			pubPlTree.selectForPlaylist(plId);
			showPublicPlaylists(true);
		}
	}

	public void showPlaylist(Playlist p) {
		selectForPlaylist(p);
	}

	public void searchAdded(String query) {
		ActiveSearchListModel model = (ActiveSearchListModel) activeSearchList.getModel();
		SearchResultTableModel srtm = model.addSearch(query);
		if (srtm != null) {
			SearchResultContentPanel srcm = new SearchResultContentPanel(frame, srtm);
			frame.mainPanel.addContentPanel("search/" + query, srcm);
		}
		activeSearchList.setSelectedIndex(model.indexOfQuery(query));
		clearSelectionExcept(activeSearchList);
		frame.mainPanel.selectContentPanel("search/" + query);
	}

	public void selectMyMusic() {
		myLib.setSelected(true);
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
	public void gotPlaylistComments(long plId, boolean anyUnread, Map<Comment, Boolean> comments) {
		// Do nothing
	}

	@Override
	public void gotLibraryComments(long userId, boolean anyUnread, Map<Comment, Boolean> comments) {
		if (anyUnread && userId == frame.ctrl.getMyUser().getUserId()) {
			// If mylib is selected and comments tab is showing, don't mark comments as unread
			if (myLib.selected) {
				ContentPanel cp = frame.mainPanel.getContentPanel("mymusiclibrary");
				if (cp.tabPane.getSelectedIndex() == 1)
					return;
			}
			myLib.setHasComments(true);
		}
	}

	public void markPlaylistCommentsAsRead(long plId) {
		User me = frame.ctrl.getMyUser();
		if (me.getPlaylistIds().contains(plId)) {
			myPlList.markPlaylistCommentsAsRead(plId);
			if (me.getPlaylistIds().size() > 1)
				friendTree.getModel().markPlaylistCommentsAsRead(plId);
			for (SpecialPlaylistSelector spSel : spSels) {
				spSel.markCommentsAsRead(plId);
			}
		} else
			friendTree.getModel().markPlaylistCommentsAsRead(plId);
	}

	public void markLibraryCommentsAsRead(long userId) {
		User me = frame.ctrl.getMyUser();
		if (me.getUserId() == userId)
			myLib.setHasComments(false);
		else
			friendTree.getModel().markLibraryCommentsAsRead(userId);
	}

	public void markMyLibraryCommentsAsRead() {
		myLib.setHasComments(false);
	}

	@Override
	public void playlistChanged(final Playlist p) {
		// I'm not sure if this is the best place to create the panels, but this is the lowest common ancestor of the
		// playlistlist and the friendtree, and we need to think about both
		final String panelName = "playlist/" + p.getPlaylistId();
		final PlaylistConfig pc = frame.ctrl.getPlaylistConfig(p.getPlaylistId());
		final long myUserId = frame.ctrl.getMyUser().getUserId();
		GuiUtil.runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				ContentPanel pPanel = frame.mainPanel.getContentPanel(panelName);
				// TODO If there is a public playlist with this plId, replace it with a friend/my playlist
				if (pPanel == null) {
					// Create playlist panel
					if (p.getOwnerIds().contains(myUserId)) {
						ContentPanel cp;
						String title = p.getTitle().toLowerCase();
						if (title.equals("loves")) {
							cp = new LovesContentPanel(frame, p, pc);
							Icon i = GuiUtil.createImageIcon("/icon/heart-small.png");
							SpecialPlaylistSelector sel = new SpecialPlaylistSelector(LeftSidebar.this, frame, i, "Loves", p);
							sideBarComps.add(sel);
							spSels.add(0, sel);
							relayoutSidebar();
						} else if (title.equals("radio")) {
							cp = new RadioContentPanel(frame, p, pc);
							Icon i = GuiUtil.createImageIcon("/icon/radio-small.png");
							SpecialPlaylistSelector sel = new SpecialPlaylistSelector(LeftSidebar.this, frame, i, "My Radio Station", p);
							sideBarComps.add(sel);
							spSels.add(sel);
							relayoutSidebar();
						} else
							cp = new MyPlaylistContentPanel(frame, p, pc);
						frame.mainPanel.addContentPanel(panelName, cp);
					} else {
						// Only show playlists with at least one track
						if (p.getStreamIds().size() > 0) {
							ContentPanel cp;
							if (frame.ctrl.isSpecialPlaylist(p.getTitle())) {
								long userId = p.getOwnerIds().iterator().next();
								cp = new FriendSpecialPlaylistContentPanel(frame, userId, p, pc);
							} else
								cp = new OtherPlaylistContentPanel(frame, p, pc);
							frame.mainPanel.addContentPanel(panelName, cp);
						}
					}
				} else {
					// Playlist panel already exists - check to see if I'm now an owner and wasn't (or vice versa)
					if ((pPanel instanceof MyPlaylistContentPanel) && !p.getOwnerIds().contains(myUserId)) {
						frame.mainPanel.addContentPanel(panelName, new OtherPlaylistContentPanel(frame, p, pc));
					} else if ((pPanel instanceof OtherPlaylistContentPanel) && p.getOwnerIds().contains(myUserId)) {
						frame.mainPanel.addContentPanel(panelName, new MyPlaylistContentPanel(frame, p, pc));
					}
				}
			}
		});
	}

	@Override
	public void friendLibraryReady(long userId, int numUnseen) {
	}

	@Override
	public void friendLibraryUpdated(long userId, int numUnseen, Map<String, Date> newTracks) {
	}

	@Override
	public void myLibraryUpdated() {
	}
}
