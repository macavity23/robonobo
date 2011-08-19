package com.robonobo.gui.panels;

import static com.robonobo.common.util.TextUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.ComponentOrientation;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.FileUtil;
import com.robonobo.core.Platform;
import com.robonobo.core.api.PlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.*;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.PlaylistTableModel;
import com.robonobo.gui.model.StreamTransfer;
import com.robonobo.gui.sheets.DeletePlaylistSheet;
import com.robonobo.gui.sheets.SharePlaylistSheet;
import com.robonobo.gui.tasks.ImportFilesTask;

@SuppressWarnings("serial")
public class MyPlaylistContentPanel extends PlaylistContentPanel implements PlaylistListener {
	protected RTextField titleField;
	protected RTextArea descField;
	protected RButton saveBtn;
	protected RButton shareBtn;
	protected RButton delBtn;
	protected RCheckBox iTunesCB;
	protected RRadioButton visMeBtn;
	protected RRadioButton visFriendsBtn;
	protected RRadioButton visAllBtn;
	protected PlaylistCommentsPanel commentsPanel;
	private ActionListener saveActionListener;
	protected Map<String, RCheckBox> options = new HashMap<String, RCheckBox>();

	public MyPlaylistContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, true);
		tabPane.insertTab("playlist", null, new PlaylistDetailsPanel(), null, 0);
		commentsPanel = new PlaylistCommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(tabPane.getSelectedIndex() == 1)
					tabPane.setForeground(RoboColor.DARK_GRAY);
			}
		});
		// Call invokeLater with this to make sure the panel is all setup properly as comments need to know width
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				if (getWidth() == 0)
					throw new Errot();
				if (addAsListener()) {
					frame.getController().addPlaylistListener(MyPlaylistContentPanel.this);
					frame.getController().getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							Map<Comment, Boolean> cs = frame.getController().getExistingCommentsForPlaylist(p.getPlaylistId());
							boolean hasUnseen = false;
							for (boolean unseen : cs.values()) {
								if (unseen) {
									hasUnseen = true;
									break;
								}
							}
							if (cs.size() > 0)
								gotPlaylistComments(p.getPlaylistId(), hasUnseen, cs);
						}
					});
				}
			}
		});
	}

	protected MyPlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, PlaylistTableModel model) {
		super(frame, p, pc, model);
		tabPane.insertTab("playlist", null, new PlaylistDetailsPanel(), null, 0);
		tabPane.setSelectedIndex(0);
		// Don't add a comment panel, let the subclass do that if they like
		if (addAsListener())
			frame.getController().addPlaylistListener(this);
	}

	protected boolean addAsListener() {
		return true;
	}

	protected boolean allowShare() {
		return true;
	}

	protected boolean allowDel() {
		return true;
	}

	protected boolean showITunes() {
		return true;
	}

	protected boolean detailsChanged() {
		return isNonEmpty(titleField.getText());
	}

	protected void savePlaylist() {
		frame.getController().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				Playlist p = getModel().getPlaylist();
				p.setTitle(titleField.getText());
				p.setDescription(descField.getText());
				frame.getController().updatePlaylist(p);
				frame.getController().putPlaylistConfig(pc);
			}
		});
	}

	@Override
	public void playlistChanged(Playlist p) {
		if (p.equals(getModel().getPlaylist())) {
			titleField.setText(p.getTitle());
			descField.setText(p.getDescription());
			String vis = p.getVisibility();
			if (vis.equals(Playlist.VIS_ALL))
				visAllBtn.setSelected(true);
			else if (vis.equals(Playlist.VIS_FRIENDS))
				visFriendsBtn.setSelected(true);
			else if (vis.equals(Playlist.VIS_ME))
				visMeBtn.setSelected(true);
			else
				throw new Errot("invalid visibility " + vis);
			getModel().update(p);
			toolsPanel.checkPlaylistVisibility();
		}
	}

	@Override
	public void gotPlaylistComments(long plId, boolean anyUnread, Map<Comment, Boolean> comments) {
		if (commentsPanel == null)
			return;
		if (plId != p.getPlaylistId())
			return;
		if (anyUnread && !(tabPane.getSelectedIndex() == 1)) {
			GuiUtil.runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					tabPane.setForegroundAt(1, RoboColor.RED);
				}
			});
		}
		List<Comment> cl = new ArrayList<Comment>(comments.keySet());
		Collections.sort(cl);
		commentsPanel.addComments(cl);
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for (DataFlavor dataFlavor : transferFlavors) {
			if (dataFlavor.equals(StreamTransfer.DATA_FLAVOR))
				return true;
		}
		return Platform.getPlatform().canDnDImport(transferFlavors);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		JTable table = trackList.getJTable();
		final PlaylistTableModel tm = (PlaylistTableModel) trackList.getModel();
		// If we have a mouse location, drop things there, otherwise
		// at the end
		int mouseRow = (table.getMousePosition() == null) ? -1 : table.rowAtPoint(table.getMousePosition());
		final int insertRow = (mouseRow >= 0) ? mouseRow : tm.getRowCount();
		boolean transferFromRobo = false;
		for (DataFlavor flavor : t.getTransferDataFlavors()) {
			if (flavor.equals(StreamTransfer.DATA_FLAVOR)) {
				transferFromRobo = true;
				break;
			}
		}
		if (transferFromRobo) {
			// DnD streams from inside robonobo
			List<String> streamIds;
			try {
				streamIds = (List<String>) t.getTransferData(StreamTransfer.DATA_FLAVOR);
			} catch (Exception e) {
				throw new Errot();
			}
			tm.addStreams(streamIds, insertRow);
			return true;
		} else {
			// DnD files from somewhere else
			List<File> files = null;
			try {
				files = Platform.getPlatform().getDnDImportFiles(t);
			} catch (IOException e) {
				log.error("Caught exception dropping files", e);
				return false;
			}
			List<File> allFiles = new ArrayList<File>();
			for (File selFile : files)
				if (selFile.isDirectory())
					allFiles.addAll(FileUtil.getFilesWithinPath(selFile, "mp3"));
				else
					allFiles.add(selFile);
			frame.getController().runTask(new PlaylistImportTask(allFiles, insertRow));
			return true;
		}
	}

	public void addTracks(List<String> streamIds) {
		PlaylistTableModel tm = (PlaylistTableModel) trackList.getModel();
		tm.addStreams(streamIds, tm.getRowCount());
	}

	class PlaylistImportTask extends ImportFilesTask {
		int insertRow;

		public PlaylistImportTask(List<File> files, int insertRow) {
			super(frame.getController(), files);
			this.insertRow = insertRow;
		}

		@Override
		protected void streamsAdded(List<String> streamIds) {
			PlaylistTableModel tm = (PlaylistTableModel) trackList.getModel();
			tm.addStreams(streamIds, insertRow);
		}
	}

	class PlaylistDetailsPanel extends JPanel {
		public PlaylistDetailsPanel() {
			double[][] cellSizen = { { 5, 35, 5, 380, 10, 150, 5, TableLayout.FILL, 5 }, { 5, 25, 5, 25, 25, 0, TableLayout.FILL, 5, 30, 5 } };
			setLayout(new TableLayout(cellSizen));
			KeyListener kl = new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					saveBtn.setEnabled(detailsChanged());
				}
			};
			saveActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!detailsChanged())
						return;
					Playlist p = getModel().getPlaylist();
					if (visAllBtn.isSelected())
						p.setVisibility(Playlist.VIS_ALL);
					else if (visFriendsBtn.isSelected())
						p.setVisibility(Playlist.VIS_FRIENDS);
					else if (visMeBtn.isSelected())
						p.setVisibility(Playlist.VIS_ME);
					pc.getItems().clear();
					for (String opt : options.keySet()) {
						JCheckBox cb = options.get(opt);
						if (cb.isSelected())
							pc.setItem(opt, "true");
					}
					savePlaylist();
					saveBtn.setEnabled(false);
				}
			};
			final Playlist p = getModel().getPlaylist();
			JLabel titleLbl = new JLabel("Title:");
			titleLbl.setFont(RoboFont.getFont(13, false));
			add(titleLbl, "1,1");
			titleField = new RTextField(p.getTitle());
			titleField.addKeyListener(kl);
			titleField.addActionListener(saveActionListener);
			add(titleField, "3,1");
			toolsPanel = new PlaylistToolsPanel();
			add(toolsPanel, "1,3,3,3");
			RLabel descLbl = new RLabel13("Description:");
			add(descLbl, "1,4,3,4");
			descField = new RTextArea(p.getDescription());
			descField.setBGColor(RoboColor.MID_GRAY);
			descField.addKeyListener(kl);
			add(new JScrollPane(descField), "1,6,3,8");
			add(new VisPanel(), "5,1,5,6");
			add(new OptsPanel(), "7,1,7,6");
			add(new ButtonsPanel(), "5,8,7,8");
		}
	}

	class VisPanel extends JPanel {
		public VisPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveBtn.setEnabled(detailsChanged());
				}
			};
			RLabel visLbl = new RLabel13B("Show playlist to:");
			add(visLbl);
			add(Box.createVerticalStrut(5));
			ButtonGroup bg = new ButtonGroup();
			// TODO multiple owners?
			Playlist p = getModel().getPlaylist();
			String vis = p.getVisibility();
			visMeBtn = new RRadioButton("Just me");
			visMeBtn.addActionListener(al);
			if (vis.equals(Playlist.VIS_ME))
				visMeBtn.setSelected(true);
			bg.add(visMeBtn);
			add(visMeBtn);
			visFriendsBtn = new RRadioButton("Friends");
			visFriendsBtn.addActionListener(al);
			if (vis.equals(Playlist.VIS_FRIENDS))
				visFriendsBtn.setSelected(true);
			bg.add(visFriendsBtn);
			add(visFriendsBtn);
			visAllBtn = new RRadioButton("Everyone");
			visAllBtn.addActionListener(al);
			if (vis.equals(Playlist.VIS_ALL))
				visAllBtn.setSelected(true);
			bg.add(visAllBtn);
			add(visAllBtn);
		}
	}

	class OptsPanel extends JPanel {
		public OptsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(Box.createVerticalStrut(20));
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveBtn.setEnabled(detailsChanged());
				}
			};
			if (showITunes() && Platform.getPlatform().iTunesAvailable()) {
				iTunesCB = new RCheckBox("Export playlist to iTunes");
				iTunesCB.setSelected("true".equalsIgnoreCase(pc.getItem("iTunesExport")));
				options.put("iTunesExport", iTunesCB);
				iTunesCB.addActionListener(al);
				add(iTunesCB);
			}
		}
	}

	class ButtonsPanel extends JPanel {
		public ButtonsPanel() {
			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			// Laying out right-to-left
			if (allowDel()) {
				delBtn = new RRedGlassButton("DELETE");
				delBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DeletePlaylistSheet dPanel = new DeletePlaylistSheet(frame, getModel().getPlaylist());
						frame.showSheet(dPanel);
					}
				});
				add(delBtn);
				add(Box.createHorizontalStrut(5));
			}
			if (allowShare()) {
				shareBtn = new RGlassButton("SHARE");
				shareBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SharePlaylistSheet shPanel = new SharePlaylistSheet(frame, getModel().getPlaylist());
						frame.showSheet(shPanel);
					}
				});
				add(shareBtn);
				add(Box.createHorizontalStrut(5));
			}
			saveBtn = new RGlassButton("SAVE");
			saveBtn.addActionListener(saveActionListener);
			saveBtn.setEnabled(false);
			add(saveBtn);
		}
	}
}
