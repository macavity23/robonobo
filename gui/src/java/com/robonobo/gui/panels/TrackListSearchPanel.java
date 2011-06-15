package com.robonobo.gui.panels;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.text.Document;

import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
class TrackListSearchPanel extends JPanel {
	RTextField searchField;
	RobonoboFrame frame;
	TrackList trackList;

	public TrackListSearchPanel(RobonoboFrame frame, TrackList t, String lblName, Document searchDoc) {
		this.frame = frame;
		this.trackList = t;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		RLabel searchLbl = new RLabel16B("Search "+lblName);
		add(searchLbl);
		add(Box.createHorizontalStrut(10));
		searchField = new RTextField(searchDoc, "", 50);
		searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		add(searchField);
	}
	
	public RTextField getSearchField() {
		return searchField;
	}
}