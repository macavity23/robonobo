package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

public class SheetTestFrame extends JFrame {
	public SheetTestFrame(Sheet sheet) {
		// Make a 5px white background around the sheet
		double[][] cellSizen = { { 3, sheet.getPreferredSize().width, 5 }, { 2, sheet.getPreferredSize().height } };
		setLayout(new TableLayout(cellSizen));
		add(sheet, "1,1");
		Dimension sz = new Dimension(sheet.getPreferredSize().width + 8, sheet.getPreferredSize().height + 7);
		setPreferredSize(sz);
		setBackground(Color.WHITE);
	}
}
