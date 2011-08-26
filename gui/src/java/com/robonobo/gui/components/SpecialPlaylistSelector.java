package com.robonobo.gui.components;

import static com.robonobo.common.util.TextUtil.*;

import java.awt.Color;
import java.util.Map;

import javax.swing.Icon;

import com.robonobo.core.api.PlaylistAdapter;
import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.ContentPanel;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class SpecialPlaylistSelector extends LeftSidebarSelector {
	protected String plName;
	private long plId = -1;
	private boolean hasComments = false;

	public SpecialPlaylistSelector(LeftSidebar sideBar, RobonoboFrame f, Icon icon, String playlistName) {
		super(sideBar, f, capitalizeFirst(playlistName), false, icon, "specialplaylist/me/"+playlistName);
		this.plName = playlistName;
		frame.ctrl.addPlaylistListener(new PlaylistAdapter() {
			@Override
			public void playlistChanged(Playlist p) {
				if (plId < 0) {
					if (p.getTitle().equalsIgnoreCase("loves")) {
						long myUid = frame.ctrl.getMyUser().getUserId();
						if (p.getOwnerIds().size() == 1 && p.getOwnerIds().contains(myUid))
							plId = p.getPlaylistId();
					}
				}
				super.playlistChanged(p);
			}

			@Override
			public void gotPlaylistComments(long updatePlId, boolean anyUnread, Map<Comment, Boolean> comments) {
				if (anyUnread && plId == updatePlId) {
					// Don't show unread if we're selected and comments tab is showing
					if(selected && frame.mainPanel.getContentPanel(cpName).tabPane.getSelectedIndex() == 1)
						return;
					setHasComments(true);
				}
			}
		});
	}

	@Override
	public void setSelected(boolean isSelected) {
		if(isSelected && hasComments) {
			// If playlist tab is selected, mark comments as read
			ContentPanel cp = frame.mainPanel.getContentPanel(cpName);
			if(cp.tabPane.getSelectedIndex() == 1)
				setHasComments(false);
		}
		super.setSelected(isSelected);
	}

	@Override
	protected Color fgColor(boolean isSel) {
		if (hasComments)
			return RoboColor.GREEN;
		return super.fgColor(isSel);
	}

	public void setHasComments(boolean hasComments) {
		this.hasComments = hasComments;
		updateColors();
	}
}
