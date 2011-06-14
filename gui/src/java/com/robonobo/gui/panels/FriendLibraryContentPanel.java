package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.Component;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.robonobo.core.api.model.Library;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.FriendLibraryTableModel;

@SuppressWarnings("serial")
public class FriendLibraryContentPanel extends ContentPanel {
	private Document searchTextDoc;
	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib) {
		this(frame, lib, new PlainDocument());
	}

	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib, Document doc) {
		super(frame, FriendLibraryTableModel.create(frame, lib, doc));
		searchTextDoc = doc;
		tabPane.insertTab("library", null, new TabPanel(), null, 0);
		tabPane.setSelectedIndex(0);
	}
	
	class TabPanel extends JPanel {
		public TabPanel() {
			double[][] cellSizen = { { 10, 300, TableLayout.FILL }, { TableLayout.FILL } };
			setLayout(new TableLayout(cellSizen));

			JPanel innerP = new JPanel();
			innerP.setLayout(new BoxLayout(innerP, BoxLayout.Y_AXIS));
			innerP.add(Box.createVerticalStrut(5));

			TrackListSearchPanel sp = new TrackListSearchPanel(frame, trackList, searchTextDoc);
			sp.setAlignmentX(Component.LEFT_ALIGNMENT);
			innerP.add(sp);
			add(innerP, "1,0");
		}
	}
}
