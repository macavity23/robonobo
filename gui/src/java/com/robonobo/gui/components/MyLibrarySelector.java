package com.robonobo.gui.components;

import static com.robonobo.gui.GuiUtil.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.Platform;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.RMenuItem;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.ContentPanel;
import com.robonobo.gui.panels.LeftSidebar;

public class MyLibrarySelector extends LeftSidebarSelector implements ActionListener {
	private boolean hasComments = false;
	Log log = LogFactory.getLog(getClass());
	public MyLibrarySelector(LeftSidebar sideBar, RobonoboFrame frame) {
		super(sideBar, frame, "My Music Library", true, createImageIcon("/icon/home.png", null), "mymusiclibrary");
	}
	
	@Override
	protected JPopupMenu getPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		RMenuItem fromFiles = new RMenuItem("Add to library from files");
		fromFiles.setActionCommand("addFromFiles");
		fromFiles.addActionListener(this);
		menu.add(fromFiles);
		if(Platform.getPlatform().iTunesAvailable()) {
			RMenuItem fromITunes = new RMenuItem("Add to library from iTunes");
			fromITunes.setActionCommand("addFromITunes");
			fromITunes.addActionListener(this);
			menu.add(fromITunes);
		}
		return menu;
	}
	
	@Override
	public void setSelected(boolean isSelected) {
		if(isSelected && hasComments) {
			// If playlist tab is selected, mark comments as read
			ContentPanel cp = frame.mainPanel.getContentPanel(contentPanelName);
			if(cp.tabPane.getSelectedIndex() == 1)
				setHasComments(false);
		}
		super.setSelected(isSelected);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(action.equals("addFromFiles"))
			frame.showAddSharesDialog();
		else if(action.equals("addFromITunes"))
			frame.importITunes();
	}

	@Override
	protected Color fgColor(boolean isSel) {
		if(hasComments)
			return RoboColor.GREEN;
		return super.fgColor(isSel);
	}
	
	public void setHasComments(boolean hasComments) {
		this.hasComments = hasComments;
		updateColors();
	}
}
