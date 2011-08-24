package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.LibraryListener;
import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.Library;
import com.robonobo.core.metadata.CommentCallback;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.FriendLibraryTableModel;

@SuppressWarnings("serial")
public class FriendLibraryContentPanel extends ContentPanel implements LibraryListener {
	private Document searchTextDoc;
	private TrackListSearchPanel searchPanel;
	private long userId;
	private CommentsPanel commentsPanel;
	boolean unreadComments = false;
	boolean haveShown = false;

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib) {
		this(frame, lib, new PlainDocument());
	}

	public FriendLibraryContentPanel(RobonoboFrame f, Library lib, Document doc) {
		super(f, FriendLibraryTableModel.create(f, lib, doc));
		this.userId = lib.getUserId();
		searchTextDoc = doc;
		tabPane.insertTab("library", null, new LibraryTabPanel(), null, 0);
		commentsPanel = new CommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabPane.getSelectedIndex() == 1) {
					if (unreadComments) {
						unreadComments = false;
						removeBangFromTab(1);
						frame.leftSidebar.markLibraryCommentsAsRead(userId);
						frame.ctrl.getExecutor().execute(new CatchingRunnable() {
							public void doRun() throws Exception {
								frame.ctrl.markLibraryCommentsAsSeen(userId);
							}
						});
					}
				}
			}
		});
		log.warn("FriendLibrary adding listener for " + userId);
		// Make sure the panel is all setup properly before doing this, otherwise getWidth() can return 0
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				if(haveShown)
					return;
				haveShown = true;
				frame.ctrl.addLibraryListener(FriendLibraryContentPanel.this);
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						boolean anyUnread = false;
						Map<Comment, Boolean> cs = frame.ctrl.getExistingCommentsForLibrary(userId);
						for (Boolean unread : cs.values()) {
							if (unread) {
								anyUnread = true;
								break;
							}
						}
						if (cs.size() > 0)
							gotLibraryComments(userId, anyUnread, cs);
					}
				});
			}
		});
	}

	@Override
	public JComponent defaultComponent() {
		return searchPanel.getSearchField();
	}

	@Override
	public void gotLibraryComments(long userId, boolean anyUnread, Map<Comment, Boolean> comments) {
		if (userId != this.userId)
			return;
		if (anyUnread && !(tabPane.getSelectedIndex() == 1)) {
			unreadComments = true;
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					addBangToTab(1);
				}
			});
		}
		List<Comment> cl = new ArrayList<Comment>(comments.keySet());
		Collections.sort(cl);
		commentsPanel.addComments(cl);
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

	class CommentsPanel extends CommentsTabPanel {
		public CommentsPanel(RobonoboFrame frame) {
			super(frame);
		}

		@Override
		protected boolean canRemoveComment(Comment c) {
			long myUid = frame.ctrl.getMyUser().getUserId();
			return (c.getUserId() == myUid);
		}

		@Override
		protected void newComment(long parentCmtId, String text, CommentCallback cb) {
			frame.ctrl.newCommentForLibrary(userId, parentCmtId, text, cb);
		}

		@Override
		public void addComments(Collection<Comment> comments) {
			super.addComments(comments);
		}
	}

	class LibraryTabPanel extends JPanel {
		public LibraryTabPanel() {
			double[][] cellSizen = { { 10, 400, TableLayout.FILL }, { TableLayout.FILL } };
			setLayout(new TableLayout(cellSizen));
			JPanel innerP = new JPanel();
			innerP.setLayout(new BoxLayout(innerP, BoxLayout.Y_AXIS));
			innerP.add(Box.createVerticalStrut(5));
			searchPanel = new TrackListSearchPanel(frame, trackList, "library", searchTextDoc);
			searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
			searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			innerP.add(searchPanel);
			add(innerP, "1,0");
		}
	}
}
