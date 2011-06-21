package com.robonobo.gui.frames;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.*;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.*;

@SuppressWarnings("serial")
public class EULAFrame extends JFrame {
	private boolean madeDecision = false;
	
	public EULAFrame(String eulaPath, final ThreadPoolExecutor executor, final Runnable onAccept, final Runnable onCancel) {
		Dimension sz = new Dimension(600, 400);
		setSize(sz);
		setPreferredSize(sz);
		double[][] cellSizen = { { 5, TableLayout.FILL, 90, 5, 90, 5 }, { 5, TableLayout.FILL, 10, 30, 5 } };
		setLayout(new TableLayout(cellSizen));
		setTitle("robonobo license agreement");
		RTextPane textPane = new RTextPane();
		textPane.setContentType("text/html");
		textPane.setText(getHtmlEula(eulaPath));
		textPane.setEditable(false);
		JScrollPane sp = new JScrollPane(textPane);
		sp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, RoboColor.MID_GRAY));
		add(sp, "1,1,4,1");
		RButton acceptBtn = new RGlassButton("ACCEPT");
		acceptBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				madeDecision = true;
				executor.execute(onAccept);
			}
		});
		add(acceptBtn, "2,3");
		RButton cancelBtn = new RRedGlassButton("CANCEL");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				madeDecision = true;
				executor.execute(onCancel);
			}
		});
		add(cancelBtn, "4,3");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(!madeDecision)
					executor.execute(onCancel);
			}
		});

	}
	
	private String getHtmlEula(String eulaPath) {
		InputStream is = getClass().getResourceAsStream(eulaPath);
		StringBuffer sb = new StringBuffer();
		byte[] buf = new byte[1024];
		int numRead;
		try {
			while((numRead = is.read(buf)) > 0) {
				sb.append(new String(buf, 0, numRead));
			}
			is.close();
		} catch (IOException e) {
			throw new Errot(e);
		}
		return sb.toString();
	}
}
