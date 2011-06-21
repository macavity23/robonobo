package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

public class AboutSheet extends Sheet {
	private static final String CREDITS_PATH = "/credits.html";
	Dimension sz = new Dimension(500, 400);
	private RButton closeBtn;
	
	public AboutSheet(RobonoboFrame f) {
		super(f);
		double[][] cellSizen = { {10, TableLayout.FILL, 100, 10}, { 10, 25, 10, TableLayout.FILL, 10, 30, 10 } };
		setName("playback.background.panel");
		setLayout(new TableLayout(cellSizen));
		setPreferredSize(sz);
		
		RLabel title = new RLabel14B("About robonobo (version "+f.getController().getVersion()+")");
		add(title, "1,1,2,1,LEFT,CENTER");
		
		RTextPane textPane = new RTextPane();
		textPane.setContentType("text/html");
		textPane.setText(getCredits());
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		add(scrollPane, "1,3,2,3");

		closeBtn = new RRedGlassButton("CLOSE");
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(closeBtn, "2,5");
	}

	@Override
	public void onShow() {
		closeBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return closeBtn;
	}
	
	private String getCredits() {
		InputStream is = getClass().getResourceAsStream(CREDITS_PATH);
		StringBuffer sb = new StringBuffer();
		byte[] buf = new byte[1024];
		int numRead;
		try {
			while ((numRead = is.read(buf)) > 0) {
				sb.append(new String(buf, 0, numRead));
			}
			is.close();
		} catch (IOException e) {
			throw new Errot(e);
		}
		return sb.toString().replace("!VERSION!", frame.getController().getVersion());
	}
}
