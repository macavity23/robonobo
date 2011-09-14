package com.robonobo.gui.tasks;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.model.Stream;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.sheets.Sheet;

@SuppressWarnings("serial")
public class ChooseImportsSheet extends Sheet {
	private List<Stream> sl;
	private ChooseTracksTableModel tracksTm;
	private List<File> fl;
	Log log = LogFactory.getLog(getClass());
	RButton importBtn;
	ImportFilesTask task;

	public ChooseImportsSheet(RobonoboFrame f, List<File> fli, List<Stream> sli, ImportFilesTask t) {
		super(f);
		this.fl = fli;
		this.sl = sli;
		this.task = t;
		Dimension sz = new Dimension(400, 600);
		setSize(sz);
		setMaximumSize(sz);
		boolean pl = false;
		double[][] cells = { { 10, TableLayout.FILL, 10 }, { 10, 20, 5, 25, 5, TableLayout.FILL, 20, 20, 10, 50, 10, 30, 10 } };
		TableLayout tl = new TableLayout(cells);
		if (!pl) {
			tl.setRow(6, 0);
			tl.setRow(7, 0);
			tl.setRow(8, 0);
			tl.setRow(9, 0);
		}
		setLayout(tl);
		setName("playback.background.panel");
		add(new RLabel16B("Choose tracks to share"), "1,1");
		JPanel selPanel = new JPanel();
		selPanel.setLayout(new BoxLayout(selPanel, BoxLayout.X_AXIS));
		RButton selAllBtn = new RSmallRoundButton("All");
		selAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tracksTm.selectAll();
			}
		});
		selPanel.add(selAllBtn);
		selPanel.add(Box.createHorizontalStrut(5));
		RButton selNoneBtn = new RSmallRoundButton("None");
		selNoneBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tracksTm.selectNone();
			}
		});
		selPanel.add(selNoneBtn);
		add(selPanel, "1,3");
		tracksTm = new ChooseTracksTableModel();
		final JTable trackTbl = new JTable(tracksTm);
		trackTbl.getColumnModel().getColumn(0).setPreferredWidth(30);
		trackTbl.getColumnModel().getColumn(1).setPreferredWidth(175);
		trackTbl.getColumnModel().getColumn(2).setPreferredWidth(175);
		trackTbl.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
		trackTbl.getTableHeader().setFont(RoboFont.getFont(13, false));
		BoolRenderer br = new BoolRenderer();
		TextRenderer tr = new TextRenderer();
		trackTbl.getColumnModel().getColumn(0).setCellRenderer(br);
		trackTbl.getColumnModel().getColumn(1).setCellRenderer(tr);
		trackTbl.getColumnModel().getColumn(2).setCellRenderer(tr);
		// Add listener to [de]select the track by clicking on it anywhere
		trackTbl.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				int col = trackTbl.columnAtPoint(p);
				// If col is 0, then this will happen anyway
				if (col > 0) {
					int row = trackTbl.rowAtPoint(p);
					if (row >= 0) {
						boolean cur = tracksTm.bl.get(row);
						tracksTm.bl.set(row, !cur);
						tracksTm.fireTableRowsUpdated(row, row);
					}
				}
			}
		});
		add(new JScrollPane(trackTbl), "1,5");
		JPanel btnsPnl = new JPanel();
		btnsPnl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		btnsPnl.setLayout(new BoxLayout(btnsPnl, BoxLayout.LINE_AXIS));
		RButton cancelBtn = new RRedGlassButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				task.cancel();
				task.cancelConfirmed();
			}
		});
		btnsPnl.add(cancelBtn);
		btnsPnl.add(Box.createHorizontalStrut(5));
		importBtn = new RGlassButton("Import");
		importBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doImport();
			}
		});
		btnsPnl.add(importBtn);
		add(btnsPnl, "1,11");
	}

	@Override
	public void onShow() {
		importBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return importBtn;
	}

	protected void doImport() {
		setVisible(false);
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				List<Stream> isl = new ArrayList<Stream>();
				List<File> ifl = new ArrayList<File>();
				List<String> sidsAdded = new ArrayList<String>();
				for (int i = 0; i < sl.size(); i++) {
					if (tracksTm.bl.get(i)) {
						isl.add(sl.get(i));
						ifl.add(fl.get(i));
					}
				}
				log.debug("Adding shares from " + isl.size() + " files");
				for (int i = 0; i < isl.size(); i++) {
					if (task.cancelRequested) {
						task.cancelConfirmed();
						return;
					}
					String path = ifl.get(i).getAbsolutePath();
					log.debug("Adding share from file " + path);
					frame.ctrl.addShare(path, isl.get(i));
					sidsAdded.add(isl.get(i).getStreamId());
					task.setStatusText("Importing " + (i + 1) + " of " + isl.size());
					task.setCompletion(((float) i + 1) / isl.size());
					task.fireUpdated();
				}
				task.streamsAdded(sidsAdded);
				task.setStatusText("Done.");
				task.setCompletion(1f);
				task.fireUpdated();
			}
		});
	}

	class ChooseTracksTableModel extends AbstractTableModel {
		String[] cols = { "Share?", "Title", "Artist" };
		List<Boolean> bl;

		ChooseTracksTableModel() {
			bl = new ArrayList<Boolean>();
			for (int i = 0; i < sl.size(); i++) {
				bl.add(true);
			}
		}

		void selectAll() {
			for (int i = 0; i < bl.size(); i++) {
				bl.set(i, Boolean.TRUE);
			}
			fireTableDataChanged();
		}

		void selectNone() {
			for (int i = 0; i < bl.size(); i++) {
				bl.set(i, Boolean.FALSE);
			}
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return sl.size();
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
				Boolean isSel = bl.get(row);
				log.debug("ImportsSheet: row " + row + " returning " + isSel);
				return isSel;
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
			log.debug("ImportsSheet: row " + row + " setting " + b);
			bl.set(row, b);
		}
	}

	// TODO We can't just use a standard table model with editable booleans as something non-obvious in our L&F buggers
	// it up
	class FixedCheckbox extends RCheckBox {
		@Override
		protected void paintComponent(Graphics g) {
			// Translate where we draw this - use this way rather than a margin, as otherwise the checkbox 'jumps' when
			// clicked
			g.translate(17, 0);
			super.paintComponent(g);
		}
	}

	class BoolRenderer implements TableCellRenderer {
		JCheckBox cb = new FixedCheckbox();

		public BoolRenderer() {
			cb.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			cb.setSelected(tracksTm.bl.get(row));
			// if(isSelected)
			// cb.setBackground(RoboColor.PALE_BLUE);
			// else
			// cb.setBackground(Color.WHITE);
			return cb;
		}
	}

	class TextRenderer extends DefaultTableCellRenderer {
		Font plainFont = RoboFont.getFont(13, false);
		Font boldFont = RoboFont.getFont(13, true);
		Border selBorder = BorderFactory.createEmptyBorder(0, 1, 0, 0);

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JComponent result = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (column == 1)
				result.setFont(boldFont);
			else
				result.setFont(plainFont);
			if (isSelected)
				result.setBorder(selBorder);
			result.setBackground(Color.WHITE);
			return result;
		}

		@Override
		protected void paintComponent(Graphics g) {
			makeTextLookLessRubbish(g);
			super.paintComponent(g);
		}
	}
}
