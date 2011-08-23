package com.robonobo.gui.panels;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.Platform;
import com.robonobo.core.api.*;
import com.robonobo.core.api.model.*;
import com.robonobo.core.metadata.CommentCallback;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.MyLibraryTableModel;

@SuppressWarnings("serial")
public class MyLibraryContentPanel extends ContentPanel implements UserListener, LibraryListener {
	private RCheckBox shareLibCheckBox;
	private RLabel addLbl;
	private Document searchDoc;
	private TrackListSearchPanel searchPanel;
	CommentsPanel commentsPanel;
	boolean unreadComments = false;

	public MyLibraryContentPanel(RobonoboFrame f) {
		this(f, new PlainDocument());
	}

	private MyLibraryContentPanel(RobonoboFrame f, Document searchDoc) {
		super(f, MyLibraryTableModel.create(f, searchDoc));
		this.searchDoc = searchDoc;
		tabPane.insertTab("library", null, new LibraryTabPanel(), null, 0);
		commentsPanel = new CommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabPane.getSelectedIndex() == 1) {
					if (unreadComments) {
						unreadComments = false;
						removeBangFromTab(1);
						final long myUid = frame.ctrl.getMyUser().getUserId();
						frame.leftSidebar.markMyLibraryCommentsAsRead();
						frame.ctrl.getExecutor().execute(new CatchingRunnable() {
							public void doRun() throws Exception {
								frame.ctrl.markLibraryCommentsAsSeen(myUid);
							}
						});
					}
				}
			}
		});
		frame.ctrl.addUserListener(this);
		// Wait til we're shown before getting comments as they need to know our width
		User me = frame.ctrl.getMyUser();
		if (me != null) {
			// Unlikely
			addComponentListener(new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					log.debug("MyLibraryContentPanel shown!");
					fetchComments();
				}
			});
		} else {
			frame.ctrl.addLoginListener(new LoginAdapter() {
				public void loginSucceeded(User me) {
					fetchComments();
				}
			});
		}
		// Fetch our update msg (if any)
		frame.ctrl.getExecutor().schedule(new CatchingRunnable() {
			public void doRun() throws Exception {
				final UpdateInfo updateInfo = frame.ctrl.getUpdateInfo();
				if (isNonEmpty(updateInfo.getUpdateHtml())) {
					runOnUiThread(new CatchingRunnable() {
						public void doRun() throws Exception {
							showMessage(updateInfo.getUpdateTitle(), updateInfo.getUpdateHtml());
						}
					});
				}
			}
		}, 10, TimeUnit.SECONDS);
	}

	@Override
	public JComponent defaultComponent() {
		return searchPanel.getSearchField();
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
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.importFilesOrDirectories(fl);
			}
		});
		return true;
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

	@Override
	public void myLibraryUpdated() {
		final int libSz = frame.ctrl.getNumSharesAndDownloads();
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				addLbl.setText("Add to library (" + libSz + " tracks)");
			}
		});
	}

	@Override
	public void gotLibraryComments(long userId, boolean anyUnread, Map<Comment, Boolean> comments) {
		long myUid = frame.ctrl.getMyUser().getUserId();
		if (userId != myUid)
			return;
		if (anyUnread && !(tabPane.getSelectedIndex() == 1)) {
			unreadComments = true;
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					addBangToTab(1);
				}
			});
		}
		List<Comment> cl = new ArrayList<Comment>(comments.keySet());
		Collections.sort(cl);
		commentsPanel.addComments(cl);
	}

	@Override
	public void friendLibraryReady(long userId, int numUnseen) {
		// Do nothing
	}

	@Override
	public void friendLibraryUpdated(long userId, int numUnseen, Map<String, Date> newTracks) {
		// Do nothing
	}

	private void fetchComments() {
		final long myUid = frame.ctrl.getMyUser().getUserId();
		frame.ctrl.addLibraryListener(this);
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				log.debug("Fetching existing comments for my library; my uid = " + myUid);
				Map<Comment, Boolean> cs = frame.ctrl.getExistingCommentsForLibrary(myUid);
				boolean anyUnread = false;
				for (Boolean unread : cs.values()) {
					if (unread) {
						anyUnread = true;
						break;
					}
				}
				if (cs.size() > 0)
					gotLibraryComments(myUid, anyUnread, cs);
			}
		});
	}

	class CommentsPanel extends CommentsTabPanel {
		public CommentsPanel(RobonoboFrame frame) {
			super(frame);
		}

		@Override
		protected boolean canRemoveComment(Comment c) {
			return true;
		}

		@Override
		protected void newComment(long parentCmtId, String text, CommentCallback cb) {
			long myUid = frame.ctrl.getMyUser().getUserId();
			frame.ctrl.newCommentForLibrary(myUid, parentCmtId, text, cb);
		}
	}

	class LibraryTabPanel extends JPanel {
		public LibraryTabPanel() {
			double[][] cellSizen = { { 10, 400, TableLayout.FILL, 240, 30 }, { TableLayout.FILL } };
			setLayout(new TableLayout(cellSizen));
			JPanel lPanel = new JPanel();
			lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.Y_AXIS));
			lPanel.add(Box.createVerticalStrut(5));
			searchPanel = new TrackListSearchPanel(frame, trackList, "library", searchDoc);
			searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
			searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			lPanel.add(searchPanel);
			lPanel.add(Box.createVerticalStrut(15));
			RLabel optsLbl = new RLabel16B("Library options");
			optsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
			lPanel.add(optsLbl);
			lPanel.add(Box.createVerticalStrut(5));
			shareLibCheckBox = new RCheckBox("Allow friends to see library");
			shareLibCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final boolean sel = shareLibCheckBox.isSelected();
					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							frame.ctrl.saveUserConfigItem("sharelibrary", sel ? "true" : "false");
						}
					});
				}
			});
			shareLibCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			shareLibCheckBox.setSelected(false);
			// We disable it first, it gets re-enabled when we get our user config
			shareLibCheckBox.setEnabled(false);
			lPanel.add(shareLibCheckBox);
			add(lPanel, "1,0");
			JPanel rPanel = new JPanel();
			rPanel.setLayout(new BoxLayout(rPanel, BoxLayout.Y_AXIS));
			rPanel.add(Box.createVerticalStrut(10));
			addLbl = new RLabel16B("Add to library (0 tracks)");
			rPanel.add(addLbl);
			rPanel.add(Box.createVerticalStrut(10));
			RButton shareFilesBtn = new RGlassButton("Add from files...");
			shareFilesBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.showAddSharesDialog();
				}
			});
			shareFilesBtn.setMaximumSize(new Dimension(200, 30));
			rPanel.add(shareFilesBtn);
			rPanel.add(Box.createVerticalStrut(10));
			if (Platform.getPlatform().iTunesAvailable()) {
				RButton shareITunesBtn = new RGlassButton("Add from iTunes...");
				shareITunesBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.importITunes();
					}
				});
				shareITunesBtn.setMaximumSize(new Dimension(200, 30));
				rPanel.add(shareITunesBtn);
			}
			add(rPanel, "3,0");
			onStartup();
		}

		private void onStartup() {
			// Deal with concurrency issues arising from controller and ui starting independently
			frame.ctrl.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					if (frame.ctrl.haveAllSharesStarted())
						myLibraryUpdated();
				}
			});
		}
	}
}
