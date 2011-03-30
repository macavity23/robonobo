package com.robonobo.gui.sheets;

import static com.robonobo.gui.GUIUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import com.robonobo.core.Platform;
import com.robonobo.core.itunes.ITunesService;
import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class WelcomeSheet extends Sheet {
	private RCheckBox shutUpCB;
	private RButton feckOffBtn;

	public WelcomeSheet(RobonoboFrame rFrame) {
		super(rFrame);
		boolean haveITunes = Platform.getPlatform().iTunesAvailable();
		Dimension size = new Dimension(600, (haveITunes ? 500: 400));
		setPreferredSize(size);
		setSize(size);
		double[][] cellSizen = {
				{ 20, TableLayout.FILL, 20 },
				{ 20 /* sp */, 55 /* logo */, 25 /* title */, 5 /* sp */, 20 /* blurb */, 10 /* sp */,
						25 /* filechoose */, 10 /* sp */, 50 /* blurb */, 0 /* sp */, (haveITunes ? 30 : 0) /* title */, (haveITunes ? 10 : 0) /* sp */,
						(haveITunes ? 30 : 0) /* btn */, (haveITunes ? 30 : 0) /* sp */, 30 /* title */, 10 /* sp */, 30 /* btn */, 20 /* sp */, 1 /* sep */,
						10 /* sp */, 30 /* btn */, 5 /* sp */, 30 /* cb */, 10 /* sp */} };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");

		RLabel imgLbl = new RIconLabel(createImageIcon("/rbnb-logo_mid-grey-bg.png", null));
		add(imgLbl, "1,1,l,t");
		RLabel titleLbl = new RLabel24B("Welcome");
		add(titleLbl, "1,2");
		RLabel dloadBlurb = new RLabel12("<html><p>" + "robonobo will store your downloaded music in this folder:"
				+ "</p></html>");
		add(dloadBlurb, "1,4");
		FileChoosePanel filePanel = new FileChoosePanel();
		add(filePanel, "1,6");
		RLabel shareBlurb = new RLabel12(
				"<html><p>"
						+ "Before you can share your music and playlists with your friends, you must add tracks to your robonobo music library. "
						+ (haveITunes ? "You can add tracks from iTunes, or else you can add them from MP3 files on your computer."
								: "You can add tracks from MP3 files on your computer.") + "</p></html>");
		add(shareBlurb, "1,8,l,t");
		if (haveITunes) {
			RLabel iTunesTitle = new RLabel18B("Share Tracks/Playlists from iTunes");
			add(iTunesTitle, "1,10");

			RButton iTunesBtn = new RGlassButton("Share from iTunes...");
			iTunesBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					frame.importITunes();
				}
			});
			addButton(iTunesBtn, "1,12");
		}
		RLabel fileTitle = new RLabel18B("Share Tracks from Files");
		add(fileTitle, "1,14");
		RButton fileBtn = new RGlassButton("Share from files...");
		fileBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				frame.showAddSharesDialog();
			}
		});
		addButton(fileBtn, "1,16");
		add(new Sep(), "1,18");
		feckOffBtn = new RGlassButton("Don't share anything");
		feckOffBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (shutUpCB.isSelected()) {
					frame.getGuiConfig().setShowWelcomePanel(false);
					frame.getController().saveConfig();
				}
				setVisible(false);
			}
		});
		addButton(feckOffBtn, "1,20");
		shutUpCB = new RCheckBox("Don't show this screen on startup");
		shutUpCB.setSelected(!frame.getGuiConfig().getShowWelcomePanel());
		add(shutUpCB, "1,22");
	}

	@Override
	public void onShow() {
	}

	@Override
	public JButton defaultButton() {
		return feckOffBtn;
	}

	private void addButton(RButton btn, String layoutPos) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		pnl.add(btn);
		add(pnl, layoutPos);
	}

	class Sep extends JSeparator {
		public Sep() {
			super(SwingConstants.HORIZONTAL);
			setBackground(RoboColor.DARK_GRAY);
		}
	}

	class FileChoosePanel extends JPanel {
		private RTextField tf;

		public FileChoosePanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			tf = new RTextField();
			tf.setMaximumSize(new Dimension(300, 30));
			tf.setText(frame.getController().getConfig().getFinishedDownloadsDirectory());
			tf.setEnabled(false);
			add(tf);
			add(Box.createHorizontalStrut(10));
			RButton btn = new RGlassButton("...");
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser(new File(tf.getText()));
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int retVal = fc.showOpenDialog(WelcomeSheet.this);
					if (retVal == JFileChooser.APPROVE_OPTION) {
						File f = fc.getSelectedFile();
						tf.setText(f.getAbsolutePath());
						frame.getController().getConfig().setFinishedDownloadsDirectory(f.getAbsolutePath());
						frame.getController().saveConfig();
					}
				}
			});
			add(btn);
		}
	}
}
