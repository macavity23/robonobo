package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.util.*;

import javax.swing.*;
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

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib) {
		this(frame, lib, new PlainDocument());
	}

	public FriendLibraryContentPanel(RobonoboFrame f, Library lib, Document doc) {
		super(f, FriendLibraryTableModel.create(f, lib, doc));
		searchTextDoc = doc;
		tabPane.insertTab("library", null, new LibraryTabPanel(), null, 0);
		commentsPanel = new CommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		this.userId = lib.getUserId();
		log.warn("FriendLibrary adding listener for "+userId);
		f.getController().addLibraryListener(this);
		// Call invokeLater with this to make sure the panel is all setup properly, otherwise getWidth() can return 0
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.getController().getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						Map<Comment, Boolean> cs = frame.getController().getExistingCommentsForLibrary(userId);
						if(cs.size() > 0)
							gotLibraryComments(userId, cs);
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
	public void gotLibraryComments(long userId, Map<Comment, Boolean> comments) {
		if(userId != this.userId)
			return;
		// TODO If any comments are new, hilite
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
			long myUid = frame.getController().getMyUser().getUserId();
			return (c.getUserId() == myUid);
		}
		
		@Override
		protected void newComment(long parentCmtId, String text, CommentCallback cb) {
			frame.getController().newCommentForLibrary(userId, parentCmtId, text, cb);
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
