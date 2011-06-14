package com.robonobo.gui.panels;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.text.Document;

import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
class TrackListSearchPanel extends JPanel {
	static final String DEFAULT_SEARCH_TEXT = "Search library...";
	RTextField searchField;
	RobonoboFrame frame;
	TrackList trackList;

	public TrackListSearchPanel(RobonoboFrame frame, TrackList t, Document searchDoc) {
		this.frame = frame;
		this.trackList = t;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setMaximumSize(new Dimension(300, 30));

		searchField = new RTextField(searchDoc, "", 50);
		searchField.setMaximumSize(new Dimension(225, 30));
		add(searchField);
		add(Box.createHorizontalStrut(5));
		
		RButton searchBtn = new RSmallRoundButton("Search");
		searchBtn.setMaximumSize(new Dimension(70, 30));
		add(searchBtn);
	}
}