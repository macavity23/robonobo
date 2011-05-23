package com.robonobo.gui.panels;

import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
class TrackListSearchPanel extends JPanel {
	static final String DEFAULT_SEARCH_TEXT = "Search library...";
	RTextField searchField;
	RobonoboFrame frame;
	TrackList trackList;
	
	public TrackListSearchPanel(RobonoboFrame frame, TrackList t) {
		this.frame = frame;
		this.trackList = t;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setMaximumSize(new Dimension(300, 30));

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doFilter();
			}
		};
		searchField = new RTextField(DEFAULT_SEARCH_TEXT);
		searchField.setMaximumSize(new Dimension(225, 30));
		searchField.addActionListener(al);
		searchField.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				searchField.selectAll();
			}
		});
		searchField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(searchField.getText().equals(DEFAULT_SEARCH_TEXT))
					searchField.setText("");
			}
		});
		add(searchField);
		add(Box.createHorizontalStrut(5));
		
		RButton searchBtn = new RSmallRoundButton("Search");
		searchBtn.setMaximumSize(new Dimension(70, 30));
		searchBtn.addActionListener(al);
		add(searchBtn);
	}
	
	private void doFilter() {
		CatchingRunnable task = new CatchingRunnable() {
			public void doRun() throws Exception {
				String filterStr = searchField.getText();
				searchField.selectAll();
				trackList.filterTracks(filterStr);
			}
		};
		if(trackList.getModel().getRowCount() < TrackList.TRACKLIST_SIZE_THRESHOLD)
			task.run();
		else
			frame.runSlowTask("filtering track list", task);
	}
}