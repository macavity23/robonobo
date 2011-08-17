package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Date;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

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

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib) {
		this(frame, lib, new PlainDocument());
	}

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib, Document doc) {
		super(frame, FriendLibraryTableModel.create(frame, lib, doc));
		searchTextDoc = doc;
		tabPane.insertTab("library", null, new LibraryTabPanel(), null, 0);
		tabPane.setSelectedIndex(0);
		this.userId = lib.getUserId();
	}

	@Override
	public JComponent defaultComponent() {
		return searchPanel.getSearchField();
	}
	
	@Override
	public void gotLibraryComments(long userId, Map<Comment, Boolean> comments) {
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
