package com.robonobo.gui.components;

import static com.robonobo.gui.GUIUtil.*;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.robonobo.core.Platform;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.StreamTransfer;
import com.robonobo.gui.panels.ContentPanel;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class NewPlaylistSelector extends LeftSidebarSelector {
	public NewPlaylistSelector(LeftSidebar sideBar, RobonoboFrame frame) {
		super(sideBar, frame, "New Playlist", false, createImageIcon("/icon/new_playlist.png", null), "newplaylist");
		setTransferHandler(new DnDHandler());
	}
	
	class DnDHandler extends TransferHandler {
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			boolean result = false;
			// Is this a drop from somewhere else in robonobo?
			for (DataFlavor dataFlavor : transferFlavors) {
				if (dataFlavor.equals(StreamTransfer.DATA_FLAVOR)) {
					result = true;
					break;
				}
			}
			// Is this a file drop from outside?
			if(!result)
				result = Platform.getPlatform().canDnDImport(transferFlavors);
			// If we can import, set us as selected
			if(result)
				setSelected(true);
			return result;
		}
		
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			ContentPanel cp = frame.getMainPanel().getContentPanel("newplaylist");
			return cp.importData(comp, t);
		}
	}
}
