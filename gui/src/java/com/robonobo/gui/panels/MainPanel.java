package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import javax.swing.JPanel;

import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {
	private RobonoboFrame frame;
	private PlaybackPanel playbackPanel;
	private ContentPanelHolder cpHolder;

	public MainPanel(RobonoboFrame f) {
		frame = f;
		double[][] cellSizen = { { TableLayout.FILL }, { 100, 5, TableLayout.FILL } };
		setLayout(new TableLayout(cellSizen));
		playbackPanel = new PlaybackPanel(frame);
		add(playbackPanel, "0,0");
		cpHolder = new ContentPanelHolder();
		add(cpHolder, "0,2");
		addDefaultContentPanels();
		selectContentPanel("mymusiclibrary");
	}
	
	private void addDefaultContentPanels() {
		addContentPanel("mymusiclibrary", new MyLibraryContentPanel(frame));
		addContentPanel("newplaylist", new NewPlaylistContentPanel(frame));
		addContentPanel("tasklist", new TaskListContentPanel(frame));
		addContentPanel("wang", new WangContentPanel(frame));
	}
	
	public void addContentPanel(String name, ContentPanel panel) {
		cpHolder.addContentPanel(name, panel);
	}

	public ContentPanel currentContentPanel() {
		return cpHolder.currentContentPanel();
	}
	
	public String currentContentPanelName() {
		return cpHolder.currentPanelName();
	}
	
	public ContentPanel getContentPanel(String name) {
		return cpHolder.getContentPanel(name);
	}
	
	public void selectContentPanel(String name) {
		cpHolder.selectContentPanel(name);
		playbackPanel.trackListPanelChanged();
	}

	public ContentPanel removeContentPanel(String panelName) {
		return cpHolder.removeContentPanel(panelName);
	}
	
	public PlaybackPanel getPlaybackPanel() {
		return playbackPanel;
	}
}
