package com.robonobo.gui.tasks;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.sheets.Sheet;

@SuppressWarnings("serial")
public class ChooseImportsSheet extends Sheet {
	private List<StreamWithFile> sl;
	private TracksTableModel tracksTm;
	Log log = LogFactory.getLog(getClass());
	RButton importBtn;
	ImportFilesTask task;
	private Map<String, List<File>> plm;
	private List<String> pll;
	private PlaylistsTableModel plTm;

	public ChooseImportsSheet(RobonoboFrame f, List<StreamWithFile> sli, Map<String, List<File>> plMap, ImportFilesTask t) {
		super(f);
		this.sl = sli;
		this.plm = plMap;
		this.task = t;
		Dimension sz = new Dimension(600, 600);
		setSize(sz);
		setMaximumSize(sz);
		setPreferredSize(sz);
		double[][] cells = { { 10, TableLayout.FILL, 10 }, { 10, 20, 5, 25, 5, TableLayout.FILL, 20, 20, 5, 25, 5, 150, 10, 30, 10 } };
		TableLayout tl = new TableLayout(cells);
		setLayout(tl);
		setName("playback.background.panel");
		add(new RLabel16B("Choose tracks to share"), "1,1");
		JPanel trackSelPanel = new JPanel();
		trackSelPanel.setLayout(new BoxLayout(trackSelPanel, BoxLayout.X_AXIS));
		RButton selAllBtn = new RSmallRoundButton("All");
		selAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tracksTm.selectAll();
			}
		});
		trackSelPanel.add(selAllBtn);
		trackSelPanel.add(Box.createHorizontalStrut(5));
		RButton selNoneBtn = new RSmallRoundButton("None");
		selNoneBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tracksTm.selectNone();
			}
		});
		trackSelPanel.add(selNoneBtn);
		add(trackSelPanel, "1,3");
		tracksTm = new TracksTableModel();
		final JTable trackTbl = new JTable(tracksTm);
		trackTbl.getColumnModel().getColumn(0).setPreferredWidth(54);
		trackTbl.getColumnModel().getColumn(1).setPreferredWidth(263);
		trackTbl.getColumnModel().getColumn(2).setPreferredWidth(263);
		trackTbl.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
		trackTbl.getTableHeader().setFont(RoboFont.getFont(13, false));
		BoolRenderer tracksBr = new BoolRenderer(tracksTm.bl);
		TextRenderer tr = new TextRenderer();
		trackTbl.getColumnModel().getColumn(0).setCellRenderer(tracksBr);
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
		if (plm != null) {
			add(new RLabel16B("Choose playlists to share"), "1,7");
			JPanel plSelPanel = new JPanel();
			plSelPanel.setLayout(new BoxLayout(plSelPanel, BoxLayout.X_AXIS));
			RButton plSelAllBtn = new RSmallRoundButton("All");
			plSelAllBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plTm.selectAll();
				}
			});
			plSelPanel.add(plSelAllBtn);
			plSelPanel.add(Box.createHorizontalStrut(5));
			RButton plSelNoneBtn = new RSmallRoundButton("None");
			plSelNoneBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plTm.selectNone();
				}
			});
			plSelPanel.add(plSelNoneBtn);
			add(plSelPanel, "1,9");
			pll = new ArrayList<String>(plm.keySet());
			Collections.sort(pll);
			plTm = new PlaylistsTableModel();
			final JTable plTbl = new JTable(plTm);
			plTbl.getColumnModel().getColumn(0).setPreferredWidth(54);
			plTbl.getColumnModel().getColumn(1).setPreferredWidth(526);
			plTbl.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
			plTbl.getTableHeader().setFont(RoboFont.getFont(13, false));
			BoolRenderer plBr = new BoolRenderer(plTm.bl);
			plTbl.getColumnModel().getColumn(0).setCellRenderer(plBr);
			plTbl.getColumnModel().getColumn(1).setCellRenderer(tr);
			// Add listener to [de]select the track by clicking on it anywhere
			plTbl.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Point p = e.getPoint();
					int col = trackTbl.columnAtPoint(p);
					// If col is 0, then this will happen anyway
					if (col > 0) {
						int row = plTbl.rowAtPoint(p);
						if (row >= 0) {
							boolean cur = plTm.bl.get(row);
							plTm.bl.set(row, !cur);
							plTm.fireTableRowsUpdated(row, row);
						}
					}
				}
			});
			add(new JScrollPane(plTbl), "1,11");
		} else {
			// Not importing playlists
			tl.setRow(6, 0);
			tl.setRow(7, 0);
			tl.setRow(8, 0);
			tl.setRow(9, 0);
			tl.setRow(10, 0);
			tl.setRow(11, 0);
		}
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
		importBtn = new RGlassButton("Share");
		importBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doImport();
			}
		});
		btnsPnl.add(importBtn);
		add(btnsPnl, "1,13");
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
				List<StreamWithFile> isl = new ArrayList<StreamWithFile>();
				List<String> sidsAdded = new ArrayList<String>();
				for (int i = 0; i < sl.size(); i++) {
					if (tracksTm.bl.get(i)) {
						isl.add(sl.get(i));
					}
				}
				Map<String, List<File>> ipm = new HashMap<String, List<File>>();
				for (int i = 0; i < pll.size(); i++) {
					if (plTm.bl.get(i)) {
						String plName = pll.get(i);
						ipm.put(plName, plm.get(plName));
					}
				}
				int total = isl.size() + ipm.size();
				log.debug("Adding shares from " + isl.size() + " files");
				for (int i = 0; i < isl.size(); i++) {
					if (task.cancelRequested) {
						task.cancelConfirmed();
						return;
					}
					String path = isl.get(i).file.getAbsolutePath();
					log.debug("Adding share from file " + path);
					frame.ctrl.addShare(path, isl.get(i));
					sidsAdded.add(isl.get(i).getStreamId());
					task.setStatusText("Importing " + (i + 1) + " of " + isl.size());
					task.setCompletion(((float) i + 1) / total);
					task.fireUpdated();
				}
				task.streamsAdded(sidsAdded);
				int i=0;
				for (String plName : ipm.keySet()) {
					if (task.cancelRequested) {
						task.cancelConfirmed();
						return;
					}

					List<File> plFiles = ipm.get(plName);
					Playlist p = frame.ctrl.getMyPlaylistByTitle(plName);
					if(p == null) {
						p = new Playlist();
						p.setTitle(plName);
						p.getOwnerIds().add(frame.ctrl.getMyUser().getUserId());
						for (File f : plFiles) {
							SharedTrack sh = frame.ctrl.getShareByFilePath(f);
							if(sh != null)
								p.getStreamIds().add(sh.getStream().getStreamId());
						}
						if(p.getStreamIds().size() > 0) {
							task.setStatusText("Creating playlist '"+plName+"'");
							frame.ctrl.createPlaylist(p, null);
						} else
							task.setStatusText("Not importing playlist '"+plName+"' - no tracks selected for import");
					} else {
						// Update existing playlist - add each track if it's not already there
						for (File f : plFiles) {
							SharedTrack sh = frame.ctrl.getShareByFilePath(f);
							if(sh != null && !p.getStreamIds().contains(sh.getStream().getStreamId()))
								p.getStreamIds().add(sh.getStream().getStreamId());
						}
						task.setStatusText("Updating existing playlist '"+plName+"'");
						frame.ctrl.updatePlaylist(p);
					}
					i++;
					task.setCompletion(((float)(i+isl.size())) / total);
					task.fireUpdated();
				}
				task.setStatusText("Done.");
				task.setCompletion(1f);
				task.fireUpdated();
			}
		});
	}

	class TracksTableModel extends AbstractTableModel {
		String[] cols = { "Share?", "Title", "Artist" };
		List<Boolean> bl;

		TracksTableModel() {
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
			return bl.size();
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
			bl.set(row, b);
		}
	}

	class PlaylistsTableModel extends AbstractTableModel {
		String[] cols = { "Share?", "Playlist Name", };
		List<Boolean> bl;

		PlaylistsTableModel() {
			bl = new ArrayList<Boolean>();
			for (int i = 0; i < pll.size(); i++) {
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
			return bl.size();
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
				return pll.get(row);
			}
			throw new SeekInnerCalmException();
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (column != 0)
				throw new SeekInnerCalmException();
			Boolean b = (Boolean) aValue;
			bl.set(row, b);
		}
	}

	// TODO We can't just use a standard table model with editable booleans as something non-obvious in our L&F buggers
	// it up
	class FixedCheckbox extends RCheckBox {
		@Override
		protected void paintComponent(Graphics g) {
			// Translate where we draw this - use this way rather than a margin, as otherwise the checkbox moves when
			// clicked
			g.translate(17, 0);
			super.paintComponent(g);
		}
	}

	class BoolRenderer implements TableCellRenderer {
		JCheckBox cb = new FixedCheckbox();
		private List<Boolean> bl;

		public BoolRenderer(List<Boolean> bl) {
			cb.setOpaque(true);
			this.bl = bl;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			cb.setSelected(bl.get(row));
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
