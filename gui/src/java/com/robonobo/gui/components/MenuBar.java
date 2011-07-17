package com.robonobo.gui.components;

import static com.robonobo.core.Platform.*;

import java.awt.event.*;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.NetUtil;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.RMenuItem;
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

		RMenuItem login = new RMenuItem("Login...", KeyEvent.VK_L);
		login.setAccelerator(getPlatform().getAccelKeystroke(KeyEvent.VK_L));
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showLogin(null);
			}
		});
		fileMenu.add(login);

		RMenuItem showWelcome = new RMenuItem("Show welcome page...");
		showWelcome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showWelcome(true);
			}
		});
		fileMenu.add(showWelcome);
		
		RMenuItem shareFiles = new RMenuItem("Share files...", KeyEvent.VK_O);
		shareFiles.setAccelerator(getPlatform().getAccelKeystroke(KeyEvent.VK_O));
		shareFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.showAddSharesDialog();
			}
		});
		fileMenu.add(shareFiles);

		if (getPlatform().iTunesAvailable()) {
			RMenuItem iTunesImport = new RMenuItem("Share tracks/playlists from iTunes...", KeyEvent.VK_I);
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

		RMenuItem openUrl = new RMenuItem("Open 'rbnb:' URI...");
		openUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showSheet(new OpenURISheet(frame));
			}
		});
		fileMenu.add(openUrl);
		
		if (getPlatform().shouldShowQuitInFileMenu()) {
			RMenuItem quit = new RMenuItem("Quit", KeyEvent.VK_Q);
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
		RMenuItem updateUsers = new RMenuItem("Update friends & playlists");
		updateUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.getController().checkUsersUpdate();
			}
		});
		networkMenu.add(updateUsers);

		if (getPlatform().shouldShowOptionsMenu()) {
			JMenu optionsMenu = new JMenu("Options");
			add(optionsMenu);
			RMenuItem showPrefs = new RMenuItem("Preferences...");
			showPrefs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.showPreferences();
				}
			});
			optionsMenu.add(showPrefs);
		}

		JMenu debugMenu = new JMenu("Debug");
		add(debugMenu);
		RMenuItem openConsole = new RMenuItem("Open Console");
		openConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showConsole();
			}
		});
		debugMenu.add(openConsole);
		RMenuItem showLog = new RMenuItem("Show Log Window");
		showLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.showLogFrame();
			}
		});
		debugMenu.add(showLog);

		JMenu helpMenu = new JMenu("Help");
		add(helpMenu);
		if (getPlatform().shouldShowAboutInHelpMenu()) {
			RMenuItem showAbout = new RMenuItem("About robonobo");
			showAbout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.showAbout();
				}
			});
			helpMenu.add(showAbout);
		}
		RMenuItem showHelpPage = new RMenuItem("Online help/feedback...");
		showHelpPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openUrl(frame.getController().getConfig().getHelpUrl());
			}
		});
		helpMenu.add(showHelpPage);
		RMenuItem showWiki = new RMenuItem("Go to developer webpage...");
		showWiki.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openUrl(frame.getController().getConfig().getDeveloperUrl());
			}
		});
		helpMenu.add(showWiki);
		RMenuItem submitBugReport = new RMenuItem("Submit bug report...");
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
