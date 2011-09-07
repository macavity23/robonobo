package com.robonobo.gui.tasks;

import info.clearthought.layout.TableLayout;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.model.Stream;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.sheets.Sheet;

public class ChooseImportsSheet extends Sheet {
	private List<Stream> sl;
	private ChooseStreamsTableModel tm;
	private List<File> fl;
	Log log = LogFactory.getLog(getClass());

	public ChooseImportsSheet(RobonoboFrame f, List<File> fli, List<Stream> sli, final Task task) {
		super(f);
		this.fl = fli;
		this.sl = sli;
		setSize(new Dimension(400, 600));
		boolean pl = false;
		double[][] cells = { { 20, TableLayout.FILL, 20 }, { 10, 20, 10, TableLayout.FILL, 20, 20, 10, 50, 10, 30, 10 } };
		TableLayout tl = new TableLayout(cells);
		if (!pl) {
			tl.setRow(4, 0);
			tl.setRow(5, 0);
			tl.setRow(6, 0);
			tl.setRow(7, 0);
		}
		add(new RLabel16B("Choose tracks to share"), "1,1");
		tm = new ChooseStreamsTableModel();
		JTable trackTbl = new JTable(tm);
		add(trackTbl, "1,3");
		JPanel btnsPnl = new JPanel();
		btnsPnl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		btnsPnl.setLayout(new BoxLayout(btnsPnl, BoxLayout.LINE_AXIS));
		RButton cancelBtn = new RRedGlassButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		btnsPnl.add(cancelBtn);
		RButton importBtn = new RGlassButton("Import");
		importBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						log.debug("Running import on " + sl.size() + " possible files");
						for (int i = 0; i < sl.size(); i++) {
							if (task.cancelRequested) {
								task.cancelConfirmed();
								return;
							}
							if (tm.bl.get(i)) {
								String path = fl.get(i).getAbsolutePath();
								log.debug("Adding share from file "+path);
								frame.ctrl.addShare(path, sl.get(i));
								task.setStatusText("Importing " + (i + 1) + " of " + sl.size());
								task.setCompletion(((float) i + 1) / sl.size());
								task.fireUpdated();
							}
						}
						task.setStatusText("Done.");
						task.setCompletion(1f);
						task.fireUpdated();
					}
				});
			}
		});
	}

	@Override
	public void onShow() {
	}

	@Override
	public JButton defaultButton() {
		return null;
	}

	class ChooseStreamsTableModel extends DefaultTableModel {
		String[] cols = { "Share?", "Title", "Artist" };
		List<Boolean> bl;

		ChooseStreamsTableModel() {
			bl = new ArrayList<Boolean>();
			for (int i = 0; i < sl.size(); i++) {
				bl.add(true);
			}
		}

		@Override
		public int getColumnCount() {
			return cols.length;
		}

		@Override
		public String getColumnName(int column) {
			return cols[column];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			default:
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 0)
				return true;
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return bl.get(row);
			case 1:
				return sl.get(row).getTitle();
			case 2:
				return sl.get(row).getArtist();
			}
			throw new SeekInnerCalmException();
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (column != 0)
				throw new SeekInnerCalmException();
			Boolean b = (Boolean) aValue;
			bl.set(column, b);
		}
	}
}
