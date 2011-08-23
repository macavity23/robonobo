package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.ComponentOrientation;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.Platform;
import com.robonobo.core.api.PlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class OtherPlaylistContentPanel extends PlaylistContentPanel implements PlaylistListener {
	RLabel titleField;
	RTextPane descField;
	RButton saveBtn;
	RCheckBox autoDownloadCB;
	RCheckBox iTunesCB;
	protected Map<String, JCheckBox> options = new HashMap<String, JCheckBox>();

	public OtherPlaylistContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, false);
		tabPane.insertTab("playlist", null, new PlaylistDetailsPanel(), null, 0);
		commentsPanel = new PlaylistCommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabPane.getSelectedIndex() == 1) {
					if (unreadComments) {
						unreadComments = false;
						tabPane.setForeground(RoboColor.DARK_GRAY);
						frame.leftSidebar.markPlaylistCommentsAsRead(p.getPlaylistId());
						frame.ctrl.getExecutor().execute(new CatchingRunnable() {
							public void doRun() throws Exception {
								frame.ctrl.markPlaylistCommentsAsSeen(p.getPlaylistId());
							}
						});
					}
				}
			}
		});
		// Make sure the panel is all setup properly before we do this as comments need to know the width
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				log.debug("Adding listener for playlist cp "+p.getPlaylistId());
				frame.ctrl.addPlaylistListener(OtherPlaylistContentPanel.this);
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						log.debug("Getting existing comments for playlist cp "+p.getPlaylistId());
						Map<Comment, Boolean> cs = frame.ctrl.getExistingCommentsForPlaylist(p.getPlaylistId());
						boolean hasUnseen = false;
						for (boolean unseen : cs.values()) {
							if(unseen) {
								hasUnseen = true;
								break;
							}
						}
						if(cs.size() > 0)
							gotPlaylistComments(p.getPlaylistId(), hasUnseen, cs);
					}
				});
			}
		});
	}

	@Override
	public void playlistChanged(Playlist p) {
		if (p.equals(this.p)) {
			if (p.getOwnerIds().contains(frame.ctrl.getMyUser().getUserId())) {
				log.debug("DEBUG: not updating playlist content panel for playlist '" + p.getTitle()
						+ "' - I am an owner!");
				return;
			}
			this.p = p;
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					updateFields();
				}
			});
			getModel().update(p);
			toolsPanel.checkPlaylistVisibility();
		}
	}

	void updateFields() {
		titleField.setText(p.getTitle());
		String desc = p.getDescription();
		descField.setText(desc);
	}

	class PlaylistDetailsPanel extends JPanel {
		public PlaylistDetailsPanel() {
			double[][] cellSizen = { { 5, 420, 15, TableLayout.FILL, 5 },
					{ 20, 5, 25, 5, TableLayout.FILL, 5, 30, 5 } };
			setLayout(new TableLayout(cellSizen));
			titleField = new RLabel18B();
			add(titleField, "1,0");
			toolsPanel = new PlaylistToolsPanel();
			add(toolsPanel, "1,2");
			descField = new RTextPane();
			descField.setBGColor(RoboColor.MID_GRAY);
			descField.setEditable(false);
			JScrollPane sp = new JScrollPane(descField);
			add(sp, "1,4,1,6");
			updateFields();
			add(new OptsPanel(), "3,0,3,4");
			add(new ButtonsPanel(), "3,6");
		}
	}

	class OptsPanel extends JPanel {
		public OptsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			add(Box.createVerticalStrut(3));
			RLabel optsLbl = new RLabel14B("Playlist options");
			add(optsLbl);
			add(Box.createVerticalStrut(2));
			
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveBtn.setEnabled(true);
				}
			};

			autoDownloadCB = new RCheckBox("Download tracks automatically");
			autoDownloadCB.setSelected("true".equals(pc.getItem("autoDownload")));
			options.put("autoDownload", autoDownloadCB);
			autoDownloadCB.addActionListener(al);
			add(autoDownloadCB);

			if (Platform.getPlatform().iTunesAvailable()) {
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

			saveBtn = new RGlassButton("SAVE");
			saveBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pc.getItems().clear();
					for (String opt : options.keySet()) {
						JCheckBox cb = options.get(opt);
						if (cb.isSelected())
							pc.setItem(opt, "true");
					}
					saveBtn.setEnabled(false);
					pc.setPlaylistId(p.getPlaylistId());
					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							// This will kick of auto-downloads & itunes integration if necessary
							frame.ctrl.putPlaylistConfig(pc);
//							try {
								// Checking playlist update will kick off autodownloads, if necessary
//								frame.getController().checkPlaylistUpdate(p.getPlaylistId());
//							} catch (RobonoboException e) {
//								log.info("Error checking playlist update", e);
//							}
						}
					});
				}
			});
			saveBtn.setEnabled(false);
			add(saveBtn);
		}
	}

}
