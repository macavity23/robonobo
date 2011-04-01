package com.robonobo.gui.frames;

import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.sheets.Sheet;

import furbelow.AbstractComponentDecorator;

@SuppressWarnings("serial")
public class SheetableFrame extends JFrame {
	private Dimmer dimmer;
	private JPanel glass;
	protected LinkedList<Sheet> sheetStack = new LinkedList<Sheet>();
	Log log = LogFactory.getLog(getClass());
	protected ReentrantLock sheetLock = new ReentrantLock(); // One at a time, fellas

	public SheetableFrame() {
		glass = (JPanel) getGlassPane();
	}

	public void showSheet(Sheet newSheet) {
		if (newSheet == null)
			throw new SeekInnerCalmException();
		sheetLock.lock();
		try {
			// If we already have a sheet showing, hide it
			if (sheetStack.size() > 0) {
				sheetStack.getFirst().hideSheet();
				repaint();
			} else
				dimmer = new Dimmer();
			sheetStack.addFirst(newSheet);
			addSheetToGlass(newSheet);
		} finally {
			sheetLock.unlock();
		}
		newSheet.onShow();
	}

	private void addSheetToGlass(Sheet s) {
		SheetContainer sc = new SheetContainer(s);
		glass.removeAll();
		double[][] cellSizen = { { TableLayout.FILL, sc.getPreferredSize().width, TableLayout.FILL },
				{ sc.getPreferredSize().height, TableLayout.FILL } };
		glass.setLayout(new TableLayout(cellSizen));
		glass.add(sc, "1,0");
		glass.setVisible(true);
		getRootPane().setDefaultButton(s.defaultButton());
		
	}
	
	public synchronized void discardTopSheet() {
		Sheet oldSheet = null;
		sheetLock.lock();
		try {
			if (sheetStack.size() == 0)
				return;
			Sheet showingSheet = sheetStack.removeFirst();
			showingSheet.hideSheet();
			if (sheetStack.size() == 0) {
				glass.setVisible(false);
				if (dimmer != null) {
					dimmer.dispose();
					dimmer = null;
				}
				getRootPane().setDefaultButton(null);
			} else {
				// There was a sheet there before this one - show it now
				repaint();
				oldSheet = sheetStack.getFirst();
				oldSheet.setVisible(true);
				addSheetToGlass(oldSheet);
			}
		} finally {
			sheetLock.unlock();
		}
		if(oldSheet != null)
			oldSheet.onShow();
	}

	public boolean isShowingSheet() {
		sheetLock.lock();
		try {
			return (sheetStack.size() > 0);
		} finally {
			sheetLock.unlock();
		}
	}

	protected Sheet getTopSheet() {
		sheetLock.lock();
		try {
			if(sheetStack.size() == 0)
				return null;
			return sheetStack.getFirst();
		} finally {
			sheetLock.unlock();
		}
	}
	
	class SheetContainer extends JPanel {
		public SheetContainer(JComponent sheet) {
			// Make a 1px grey border and a 5px white background around the sheet
			double[][] cellSizen = { { 3, sheet.getPreferredSize().width, 5 }, { 2, sheet.getPreferredSize().height } };
			setLayout(new TableLayout(cellSizen));
			add(sheet, "1,1");
			Dimension sz = new Dimension(sheet.getPreferredSize().width + 8, sheet.getPreferredSize().height + 7);
			setPreferredSize(sz);
			setOpaque(true);
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createLineBorder(RoboColor.LIGHT_GRAY));
		}
	}

	class Dimmer extends AbstractComponentDecorator implements KeyEventDispatcher {
		public Dimmer() {
			super(SheetableFrame.this.getLayeredPane());
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
			getPainter().addMouseListener(new MouseAdapter() {
			});
			getPainter().addMouseMotionListener(new MouseMotionAdapter() {
			});
		}

		@Override
		public void paint(Graphics g) {
			Color bg = getComponent().getBackground();
			Color c = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 128);
			Rectangle r = getDecorationBounds();
			g = g.create();
			g.setColor(c);
			g.fillRect(r.x, r.y, r.width, r.height);
			g.dispose();
		}

		@Override
		public void dispose() {
			super.dispose();
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			return SwingUtilities.isDescendingFrom(e.getComponent(), getComponent());
		}
	}
}
