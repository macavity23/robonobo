package com.robonobo.gui.components;

import static com.robonobo.gui.GUIUtils.*;

import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.LeftSidebar;

public class MyLibrarySelector extends LeftSidebarSelector {
	public MyLibrarySelector(LeftSidebar sideBar, RobonoboFrame frame) {
		super(sideBar, frame, "My Music Library", true, createImageIcon("/icon/home.png", null), "mymusiclibrary");
	}
}
