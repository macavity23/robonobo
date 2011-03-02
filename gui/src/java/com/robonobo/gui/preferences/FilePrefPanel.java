package com.robonobo.gui.preferences;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;


@SuppressWarnings("serial")
public class FilePrefPanel extends PrefPanel {
	RTextField textField;
	String propName;
	JFileChooser fc;
	
	public FilePrefPanel(RobonoboFrame frame, String propName, String description, boolean selDirectory) {
		super(frame);
		double[][] cellSizen = { { 5, TableLayout.FILL, 5, 205, 5, 30, 5 }, { 25 } };
		setLayout(new TableLayout(cellSizen));
		this.propName = propName;
		RLabel descLbl = new RLabel12(description);
		
		add(descLbl, "1,0");
		textField = new RTextField(getProperty(propName));
		textField.setEnabled(false);
		add(textField, "3,0");
		fc = new JFileChooser(new File(getProperty(propName)));
		fc.setFileSelectionMode(selDirectory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
		RButton but = new RGlassButton("...");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int retVal = fc.showOpenDialog(FilePrefPanel.this);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					textField.setText(f.getAbsolutePath());
				}
			}
		});
		add(but, "5,0");
		setMaximumSize(new Dimension(478, 27));
	}
	
	@Override
	public void resetValue() {
		textField.setText(getProperty(propName));
		fc.setSelectedFile(new File(getProperty(propName)));
	}
	
	@Override
	public void applyChanges() {
		setProperty(propName, textField.getText());
	}

	@Override
	public boolean hasChanged() {
		return !(textField.getText().equals(getProperty(propName)));
	}
}
