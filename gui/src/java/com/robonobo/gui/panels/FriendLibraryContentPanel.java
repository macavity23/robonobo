package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.robonobo.core.api.model.Library;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.FriendLibraryTableModel;

@SuppressWarnings("serial")
public class FriendLibraryContentPanel extends ContentPanel {
	private Document searchTextDoc;
	private TrackListSearchPanel searchPanel;

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib) {
		this(frame, lib, new PlainDocument());
	}

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib, Document doc) {
		super(frame, FriendLibraryTableModel.create(frame, lib, doc));
		searchTextDoc = doc;
		tabPane.insertTab("library", null, new TabPanel(), null, 0);
		tabPane.setSelectedIndex(0);
	}

	@Override
	public JComponent defaultComponent() {
		return searchPanel.getSearchField();
	}
	
	class TabPanel extends JPanel {
		public TabPanel() {
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
