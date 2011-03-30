package com.robonobo.gui.components;

import static com.robonobo.gui.RoboColor.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.ActiveSearchListModel;
import com.robonobo.gui.model.SearchResultTableModel;
import com.robonobo.gui.panels.*;

@SuppressWarnings("serial")
public class ActiveSearchList extends LeftSidebarList {
	private static final int MAX_LBL_WIDTH = 160;
	/** Clicks with an X coord between mincloseclick and maxcloseclick we take to be on the close 'X' **/
	private static final int MIN_CLOSE_CLICK = 163;
	private static final int MAX_CLOSE_CLICK = 176;
	ActiveSearchListModel aslm;

	public ActiveSearchList(LeftSidebar sideBar, final RobonoboFrame frame) {
		super(sideBar, frame, new ActiveSearchListModel(frame.getController()));
		aslm = (ActiveSearchListModel) getModel();
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseListener());
	}

	public void selectForQuery(String query) {
		int idx = aslm.indexOfQuery(query);
		setSelectedIndex(idx);
	}
	
	@Override
	protected void itemSelected(int index) {
		String query = (String) aslm.getElementAt(index);
		frame.getMainPanel().selectContentPanel("search/" + query);
	}

	public void searchAdded(String query) {
		aslm.addSearch(query);
	}

	private class MouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			int clickIdx = locationToIndex(new Point(e.getX(), e.getY()));
			if (clickIdx < 0)
				return;
			if (e.getX() >= MIN_CLOSE_CLICK && e.getX() <= MAX_CLOSE_CLICK) {
				// They clicked on the close 'X'
				String query = (String) aslm.getElementAt(clickIdx);
				aslm.removeElementAt(clickIdx);
				ContentPanel cp = frame.getMainPanel().removeContentPanel("search/" + query);
				SearchResultTableModel srtm = (SearchResultTableModel) cp.getTrackList().getModel();
				srtm.die();
				// Bring the next search into focus if there is one, else select MyMusicLibrary
				if (clickIdx < aslm.getSize())
					setSelectedIndex(clickIdx);
				else
					sideBar.selectMyMusic();
				e.consume();
			}
		}
	}

	class CellRenderer extends DefaultListCellRenderer {
		RLabel textLbl;
		RLabel closeLbl;
		JPanel pnl;
		ImageIcon closeIconUnsel;
		ImageIcon closeIconSel;

		public CellRenderer() {
			textLbl = new RLabel12();
			textLbl.setOpaque(false);
			textLbl.setIcon(new ImageIcon(ActiveSearchList.class.getResource("/icon/magnifier_small.png")));
			textLbl.setMaximumSize(new Dimension(MAX_LBL_WIDTH, 65535));
			textLbl.setPreferredSize(new Dimension(MAX_LBL_WIDTH, 65535));
			textLbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
			closeIconSel = new ImageIcon(ActiveSearchList.class.getResource("/icon/close_x_light_bg.png"));
			closeIconUnsel = new ImageIcon(ActiveSearchList.class.getResource("/icon/close_x_mid_bg.png"));
			closeLbl = new RIconLabel(closeIconUnsel);
			closeLbl.setOpaque(false);
			closeLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			pnl = new JPanel();
			pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
			pnl.add(textLbl);
			pnl.add(closeLbl);
			pnl.setOpaque(true);
			pnl.setMaximumSize(new Dimension(65535, 65535));
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			String searchStr = (String) value;
			textLbl.setText(searchStr);
			if (index == getSelectedIndex()) {
				pnl.setBackground(LIGHT_GRAY);
				textLbl.setForeground(BLUE_GRAY);
				closeLbl.setIcon(closeIconSel);
			} else {
				pnl.setBackground(MID_GRAY);
				textLbl.setForeground(DARK_GRAY);
				closeLbl.setIcon(closeIconUnsel);
			}
			return pnl;
		}
	}
}
