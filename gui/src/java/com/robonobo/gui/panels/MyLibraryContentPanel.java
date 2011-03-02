package com.robonobo.gui.panels;

import static com.robonobo.common.util.TextUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.Platform;
import com.robonobo.core.api.UserPlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.MyLibraryTableModel;

@SuppressWarnings("serial")
public class MyLibraryContentPanel extends ContentPanel implements UserPlaylistListener {
	private RCheckBox shareLibCheckBox;

	public MyLibraryContentPanel(RobonoboFrame f) {
		super(f, new MyLibraryTableModel(f.getController()));
		tabPane.insertTab("library", null, new MyLibraryTabPanel(), null, 0);
		tabPane.setSelectedIndex(0);
		frame.getController().addUserPlaylistListener(this);

		frame.getController().getExecutor().schedule(new CatchingRunnable() {
			public void doRun() throws Exception {
				final String updateMsg = frame.getController().getUpdateMessage();
				if (isNonEmpty(updateMsg)) {
					final String title = "A new version is available";
					SwingUtilities.invokeLater(new CatchingRunnable() {
						public void doRun() throws Exception {
							showMessage(title, updateMsg);
						}
					});
				}
			}
		}, 10, TimeUnit.SECONDS);
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return Platform.getPlatform().canDnDImport(transferFlavors);
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		List<File> l = null;
		try {
			l = Platform.getPlatform().getDnDImportFiles(t);
		} catch (IOException e) {
			log.error("Caught exception dropping files", e);
			return false;
		}
		final List<File> fl = l;
		frame.getController().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.importFilesOrDirectories(fl);
			}
		});
		return true;
	}

	@Override
	public void libraryChanged(Library lib) {
		// Do nothing
	}

	@Override
	public void loggedIn() {
		// Do nothing
	}

	@Override
	public void playlistChanged(Playlist p) {
		// Do nothing
	}

	@Override
	public void allUsersAndPlaylistsUpdated() {
		// TODO Auto-generated method stub

	}

	@Override
	public void userChanged(User u) {
		// Do nothing
	}

	@Override
	public void userConfigChanged(UserConfig cfg) {
		boolean libShared = true;
		if (cfg.getItems().containsKey("sharelibrary"))
			libShared = ("true".equalsIgnoreCase(cfg.getItems().get("sharelibrary")));
		final boolean flarp = libShared;
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				shareLibCheckBox.setEnabled(true);
				shareLibCheckBox.setSelected(flarp);
			}
		});
	}

	class MyLibraryTabPanel extends JPanel {
		public MyLibraryTabPanel() {
			double[][] cellSizen = { { 10, 200, 200, TableLayout.FILL, 10 }, { 0, 25, 5, 30, 10, 30, TableLayout.FILL } };
			setLayout(new TableLayout(cellSizen));

			RLabel addLbl = new RLabel16B("Add to library");
			add(addLbl, "1,1");

			RButton shareFilesBtn = new RGlassButton("Add from files...");
			shareFilesBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.showAddSharesDialog();
				}
			});
			add(shareFilesBtn, "1,3");
			if (Platform.getPlatform().iTunesAvailable()) {
				RButton shareITunesBtn = new RGlassButton("Add from iTunes...");
				shareITunesBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.importITunes();
					}
				});
				add(shareITunesBtn, "1,5");
			}

			RLabel optsLbl = new RLabel16B("Library options");
			add(optsLbl, "3,1");

			shareLibCheckBox = new RCheckBox("Share library with friends");
			shareLibCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					frame.getController().getExecutor().execute(new CatchingRunnable() {
						@Override
						public void doRun() throws Exception {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								frame.getController().saveUserConfigItem("sharelibrary", "true");
							} else if (e.getStateChange() == ItemEvent.DESELECTED) {
								frame.getController().saveUserConfigItem("sharelibrary", "false");
							}
						}
					});
				}
			});
			shareLibCheckBox.setSelected(false);
			// We disable it first, it gets re-enabled when we get our user config
			shareLibCheckBox.setEnabled(false);
			add(shareLibCheckBox, "3,3,LEFT,TOP");
		}
	}
}
