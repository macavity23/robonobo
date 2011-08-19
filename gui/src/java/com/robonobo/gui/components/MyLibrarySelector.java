package com.robonobo.gui.components;

import static com.robonobo.gui.GuiUtil.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

import com.robonobo.core.Platform;
import com.robonobo.gui.components.base.RMenuItem;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.LeftSidebar;

public class MyLibrarySelector extends LeftSidebarSelector implements ActionListener {
	boolean hasComments = false;
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
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(action.equals("addFromFiles"))
			frame.showAddSharesDialog();
		else if(action.equals("addFromITunes"))
			frame.importITunes();
	}

	public boolean hasComments() {
		return hasComments;
	}

	public void setHasComments(boolean hasComments) {
		this.hasComments = hasComments;
	}
}
