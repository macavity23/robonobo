package com.robonobo.gui.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class FileChoosePanel extends JPanel {
	private RTextField tf = new RTextField();
	private RobonoboFrame frame;
	public File chosenFile;

	public FileChoosePanel(RobonoboFrame f, String initialPath, final boolean dirsOnly, final Runnable onChoose) {
		this.frame = f;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		Dimension sz = new Dimension(300, 30);
		tf.setText(initialPath);
		tf.setPreferredSize(sz);
		tf.setEnabled(false);
		add(Box.createHorizontalStrut(50));
		add(tf);
		add(Box.createHorizontalStrut(10));
		RButton btn = new RGlassButton("Change");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(new File(tf.getText()));
				if(dirsOnly)
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				else
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int retVal = fc.showOpenDialog(FileChoosePanel.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					chosenFile = fc.getSelectedFile();
					tf.setText(chosenFile.getAbsolutePath());
					frame.ctrl.getExecutor().execute(onChoose);
				}
			}
		});
		add(btn);
	}
}
