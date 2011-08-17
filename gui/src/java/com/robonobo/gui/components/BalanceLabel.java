package com.robonobo.gui.components;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.wang.WangListener;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.RLabel;
import com.robonobo.gui.components.base.RLabel22;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class BalanceLabel extends JPanel implements LeftSidebarComponent, WangListener {
	RobonoboFrame frame;
	RLabel lbl;
	NumberFormat balanceFormat;

	public BalanceLabel(RobonoboFrame frame) {
		this.frame = frame;
		double[][] cellSizen = { { TableLayout.FILL }, { TableLayout.FILL } };
		setLayout(new TableLayout(cellSizen));
		lbl = new RLabel22(createImageIcon("/wang-orange-on-trans.png", null));
		lbl.setForeground(RoboColor.ORANGE);
		add(lbl, "0,0,CENTER,CENTER");
		balanceFormat = NumberFormat.getInstance();
		balanceFormat.setMaximumFractionDigits(2);
		balanceFormat.setMinimumFractionDigits(2);
		setBackground(RoboColor.DARK_GRAY);
		setBalance(0d);
		addMouseListener(new MouseListener());
		frame.getController().addWangListener(this);
		Dimension sz = new Dimension(200, 30);
		setPreferredSize(sz);
	}

	@Override
	public void balanceChanged(double newBalance) {
		setBalance(newBalance);
	}

	@Override
	public void accountActivity(double creditValue, String narration) {
		// Do nothing
	}

	public void setSelected(boolean selected) {
		if(selected) {
			frame.getMainPanel().selectContentPanel("wang");
			setBorder(BorderFactory.createLoweredBevelBorder());
			frame.getLeftSidebar().clearSelectionExcept(this);
		} else {
			setBorder(null);
		}
		markAsDirty(this);
	}
	
	@Override
	public void relinquishSelection() {
		setSelected(false);
	}

	private void setBalance(final double newBalance) {
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				// TODO Make it red if they have no ends
				String balanceTxt = balanceFormat.format(newBalance);
				lbl.setText(balanceTxt);
			}
		});
	}

	class MouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			setSelected(true);
			e.consume();
		}
	}
}
