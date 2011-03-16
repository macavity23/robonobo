package com.robonobo.gui.components;

import static com.robonobo.gui.RoboColor.*;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.LeftSidebar;

/**
 * A simple label in the left sidebar that can be selected
 * @author macavity
 *
 */
public abstract class LeftSidebarSelector extends JPanel implements LeftSidebarComponent {
	protected LeftSidebar sideBar;
	protected RobonoboFrame frame;
	protected String contentPanelName;
	private RLabel lbl;

	public LeftSidebarSelector(LeftSidebar sideBar, RobonoboFrame frame, String label, boolean lblBold, Icon icon, String contentPanelName) {
		this.sideBar = sideBar;
		this.frame = frame;
		this.contentPanelName = contentPanelName;
		setOpaque(true);
		setAlignmentX(0f);
		setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(185, 19));
		setMinimumSize(new Dimension(185, 19));
		setMaximumSize(new Dimension(185, 19));
		lbl = (lblBold) ? new RLabel13B(label, icon, JLabel.LEFT) : new RLabel13(label, icon, JLabel.LEFT);
		lbl.setOpaque(false);
		add(lbl);
		addMouseListener(new MouseListener());
	}

	public void relinquishSelection() {
		setSelected(false);
	}
	
	public void setSelected(boolean isSelected) {
		if (isSelected) {
			frame.getMainPanel().selectContentPanel(contentPanelName);
			setBackground(LIGHT_GRAY);
			setForeground(BLUE_GRAY);
			sideBar.clearSelectionExcept(this);
		} else {
			setBackground(MID_GRAY);
			setForeground(DARK_GRAY);
		}
		RepaintManager.currentManager(this).markCompletelyDirty(this);
	}

	public void setIcon(Icon icon) {
		lbl.setIcon(icon);
	}
	
	public void setBold(boolean bold) {
		lbl.setFont(RoboFont.getFont(11, bold));
	}
	
	public void setText(String text) {
		lbl.setText(text);
	}
	
	class MouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			setSelected(true);
			e.consume();
		}
	}
}
