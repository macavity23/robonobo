package com.robonobo.gui.components;

import static com.robonobo.core.Platform.*;

import java.awt.event.*;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.NetUtil;
import com.robonobo.core.Platform;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.sheets.OpenURISheet;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private Log log;

	public MenuBar(final RobonoboFrame frame) {
		setForeground(RoboColor.DARKISH_GRAY);
		setBackground(RoboColor.OFF_WHITE);
		setFont(RoboFont.getFont(11, false));
		log = LogFactory.getLog(getClass());
		JMenu fileMenu = new JMenu("File");
		add(fileMenu);

		JMenuItem login = new JMenuItem("Login...", KeyEvent.VK_L);
		login.setAccelerator(getPlatform().getAccelKeystroke(KeyEvent.VK_L));
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showLogin(null);
			}
		});
		fileMenu.add(login);

		JMenuItem showWelcome = new JMenuItem("Show welcome page...");
		showWelcome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showWelcome(true);
			}
		});
		fileMenu.add(showWelcome);
		
		JMenuItem shareFiles = new JMenuItem("Share files...", KeyEvent.VK_O);
		shareFiles.setAccelerator(getPlatform().getAccelKeystroke(KeyEvent.VK_O));
		shareFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.showAddSharesDialog();
			}
		});
		fileMenu.add(shareFiles);

		if (getPlatform().iTunesAvailable()) {
			JMenuItem iTunesImport = new JMenuItem("Share tracks/playlists from iTunes...", KeyEvent.VK_I);
			iTunesImport.setAccelerator(getPlatform().getAccelKeystroke(KeyEvent.VK_I));
			iTunesImport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.getController().getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							frame.importITunes();
						}
					});

				}
			});
			fileMenu.add(iTunesImport);
		}

		JMenuItem openUrl = new JMenuItem("Open 'rbnb:' URI...");
		openUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showSheet(new OpenURISheet(frame));
			}
		});
		fileMenu.add(openUrl);
		
		if (getPlatform().shouldShowQuitInFileMenu()) {
			JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);
			quit.setAccelerator(getPlatform().getAccelKeystroke(KeyEvent.VK_Q));
			quit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.confirmThenShutdown();
				}
			});
			fileMenu.add(quit);
		}

		JMenu networkMenu = new JMenu("Network");
		add(networkMenu);
		JMenuItem updateUsers = new JMenuItem("Update friends & playlists");
		updateUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.getController().checkUsersUpdate();
			}
		});
		networkMenu.add(updateUsers);

		if (getPlatform().shouldShowOptionsMenu()) {
			JMenu optionsMenu = new JMenu("Options");
			add(optionsMenu);
			JMenuItem showPrefs = new JMenuItem("Preferences...");
			showPrefs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.showPreferences();
				}
			});
			optionsMenu.add(showPrefs);
		}

		JMenu debugMenu = new JMenu("Debug");
		add(debugMenu);
		JMenuItem openConsole = new JMenuItem("Open Console");
		openConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showConsole();
			}
		});
		debugMenu.add(openConsole);
		JMenuItem showLog = new JMenuItem("Show Log Window");
		showLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showLogFrame();
			}
		});
		debugMenu.add(showLog);

		JMenu helpMenu = new JMenu("Help");
		add(helpMenu);
		if (getPlatform().shouldShowAboutInHelpMenu()) {
			JMenuItem showAbout = new JMenuItem("About robonobo");
			showAbout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.showAbout();
				}
			});
			helpMenu.add(showAbout);
		}
		JMenuItem showHelpPage = new JMenuItem("Go to online help...");
		showHelpPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openUrl(frame.getController().getConfig().getHelpUrl());
			}
		});
		helpMenu.add(showHelpPage);
		JMenuItem showWiki = new JMenuItem("Go to developer wiki...");
		showWiki.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openUrl(frame.getController().getConfig().getWikiUrl());
			}
		});
		helpMenu.add(showWiki);
		JMenuItem submitBugReport = new JMenuItem("Submit bug report...");
		submitBugReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openUrl(frame.getController().getConfig().getBugReportUrl());
			}
		});
		helpMenu.add(submitBugReport);
	}

	private void openUrl(String url) {
		try {
			NetUtil.browse(url);
		} catch (Exception e) {
			log.error("Caught error opening url " + url);
		}
	}
}
