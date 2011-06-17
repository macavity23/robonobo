package com.robonobo.gui.frames;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.gui.GuiUtil.*;
import static javax.swing.SwingUtilities.*;
import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.Robonobo;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.FileUtil;
import com.robonobo.common.util.NetUtil;
import com.robonobo.core.Platform;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.TrackListener;
import com.robonobo.core.api.model.*;
import com.robonobo.core.metadata.UserConfigCallback;
import com.robonobo.gui.*;
import com.robonobo.gui.panels.*;
import com.robonobo.gui.preferences.PrefDialog;
import com.robonobo.gui.sheets.*;
import com.robonobo.gui.tasks.ImportFilesTask;
import com.robonobo.gui.tasks.ImportITunesTask;
import com.robonobo.mina.external.HandoverHandler;

@SuppressWarnings("serial")
public class RobonoboFrame extends SheetableFrame implements TrackListener {
	public RobonoboController control;
	private String[] cmdLineArgs;
	private JMenuBar menuBar;
	private MainPanel mainPanel;
	private LeftSidebar leftSidebar;
	private Log log = LogFactory.getLog(RobonoboFrame.class);
	private GuiConfig guiConfig;
	UriHandler uriHandler;
	private boolean tracksLoaded;
	private boolean shownLogin;

	public RobonoboFrame(RobonoboController control, String[] args) {
		this.control = control;
		this.cmdLineArgs = args;

		setTitle("robonobo");
		setIconImage(getRobonoboIconImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseListener());

		menuBar = Platform.getPlatform().getMenuBar(this);
		setJMenuBar(menuBar);

		JPanel contentPane = new JPanel();
		double[][] cellSizen = { { 5, 200, 5, TableLayout.FILL, 5 }, { 3, TableLayout.FILL, 5 } };
		contentPane.setLayout(new TableLayout(cellSizen));
		setContentPane(contentPane);
		leftSidebar = new LeftSidebar(this);
		contentPane.add(leftSidebar, "1,1");
		mainPanel = new MainPanel(this);
		contentPane.add(mainPanel, "3,1");
		setPreferredSize(new Dimension(1024, 723));
		pack();
		leftSidebar.selectMyMusic();
		guiConfig = (GuiConfig) control.getConfig("gui");
		addListeners();
		uriHandler = new UriHandler(this);
	}

	private void addListeners() {
		control.addTrackListener(this);
		// There's a chance the control might have loaded all its tracks before we add ourselves as a tracklistener, so spawn a thread to check if this is so
		control.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				checkTracksLoaded();
			}
		});
		// Grab our events...
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventHandler());
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			// Log us the hell in
			Runnable onLogin = new CatchingRunnable() {
				public void doRun() throws Exception {
					// If the tracks haven't loaded yet, show the welcome when they have
					shownLogin = true;
					if (tracksLoaded)
						showWelcome(false);
				}
			};
			final LoginSheet ls = new LoginSheet(RobonoboFrame.this, false, onLogin);
			showSheet(ls);
			if (isNonEmpty(ls.getEmailField().getText())) {
				invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						ls.tryLogin();
					}
				});
			}
		}
	}

	public LeftSidebar getLeftSidebar() {
		return leftSidebar;
	}

	public GuiConfig getGuiConfig() {
		return guiConfig;
	}

	public PlaybackPanel getPlaybackPanel() {
		return mainPanel.getPlaybackPanel();
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * Once this is called, everything is up and running
	 */
	@Override
	public void allTracksLoaded() {
		tracksLoaded = true;
		setupHandoverHandler();
		handleArgs();
		// If we haven't shown the login sheet yet, show the welcome later
		if (shownLogin)
			showWelcome(false);
	}

	private void checkTracksLoaded() {
		if (tracksLoaded)
			return;
		if (control.haveAllSharesStarted())
			allTracksLoaded();
	}

	private void handleArgs() {
		// Handle everything that isn't the -console
		for (String arg : cmdLineArgs) {
			if (!"-console".equalsIgnoreCase(arg))
				handleArg(arg);
		}
	}

	private void setupHandoverHandler() {
		control.setHandoverHandler(new HandoverHandler() {
			@Override
			public String gotHandover(String arg) {
				handleArg(arg);
				SwingUtilities.invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						// Note: this doesn't bring the app to the front on OSX, but we don't care that much as the app
						// receives URL notifications directly anyway
						// If we need it at a subsequent stage, just run an applescript:
						// tell app "robonobo"
						// activate
						// end tell
						RobonoboFrame.this.setState(Frame.NORMAL);
						RobonoboFrame.this.toFront();
					}
				});
				log.debug("Got handover msg: " + arg);
				return "0:OK";
			}

		});
	}

	private void handleArg(String arg) {
		if (isNonEmpty(arg)) {
			if (arg.startsWith("rbnb"))
				openRbnbUri(arg);
			else
				log.error("Received erroneous robonobo argument: " + arg);
		}
	}

	public void openRbnbUri(String uri) {
		uriHandler.handle(uri);
	}

	public void showWelcome(boolean forceShow) {
		// If we have no shares (or we're forcing it), show the welcome dialog
		final boolean gotShares = (control.getShares().size() > 0);
		if (forceShow || (!gotShares && guiConfig.getShowWelcomePanel())) {
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					showSheet(new WelcomeSheet(RobonoboFrame.this));
				}
			});
		}
	}

	@Override
	public void trackUpdated(String streamId, Track t) {
		// Do nothing
	}

	@Override
	public void tracksUpdated(Collection<Track> trax) {
		// Do nothing
	}

	public void importFilesOrDirectories(final List<File> files) {
		List<File> allFiles = new ArrayList<File>();
		for (File selFile : files)
			if (selFile.isDirectory())
				allFiles.addAll(FileUtil.getFilesWithinPath(selFile, "mp3"));
			else
				allFiles.add(selFile);
		importFiles(allFiles);
		return;
	}

	public void importFiles(final List<File> files) {
		ImportFilesTask t = new ImportFilesTask(control, files);
		control.runTask(t);
	}

	public void importITunes() {
		ImportITunesTask t = new ImportITunesTask(control);
		control.runTask(t);
	}

	public void showAddSharesDialog() {
		// Define this as a runnable as we might need to login first
		Runnable flarp = new CatchingRunnable() {
			@Override
			public void doRun() throws Exception {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory())
							return true;
						return "mp3".equalsIgnoreCase(FileUtil.getFileExtension(f));
					}

					public String getDescription() {
						return "MP3 files";
					}
				});
				fc.setMultiSelectionEnabled(true);
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int retVal = fc.showOpenDialog(RobonoboFrame.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					final File[] selFiles = fc.getSelectedFiles();
					control.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							importFilesOrDirectories(Arrays.asList(selFiles));
						}
					});
				}
			}
		};
		if (control.getMyUser() != null)
			flarp.run();
		else
			showLogin(flarp);
	}

	/**
	 * @param onLogin
	 *            If the login is successful, this will be executed on the Swing GUI thread (so don't do too much in it)
	 */
	public void showLogin(final Runnable onLogin) {
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				LoginSheet lp = new LoginSheet(RobonoboFrame.this, true, onLogin);
				showSheet(lp);
			}
		});
	}

	public void showAbout() {
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				AboutSheet ap = new AboutSheet(RobonoboFrame.this);
				showSheet(ap);
			}
		});

	}

	public void showPreferences() {
//		showSheet(new PreferencesSheet(this));
		PrefDialog prefDialog = new PrefDialog(this);
		prefDialog.setVisible(true);
	}

	public void showConsole() {
		ConsoleFrame consoleFrame = new ConsoleFrame(this);
		consoleFrame.setVisible(true);
	}

	public void showLogFrame() {
		Log4jMonitorFrame logFrame = new Log4jMonitorFrame(this);
		logFrame.setVisible(true);
	}

	// TODO Generalise fb/twitter into SocialNetwork or something
	/**
	 * Call only from UI thread
	 */
	public void postToFacebook(final Playlist p) {
		if(!SwingUtilities.isEventDispatchThread())
			throw new Errot();
		UserConfig uc = control.getMyUserConfig();
		if (uc == null || uc.getItem("facebookId") == null) {
			// We don't seem to be registered for facebook - fetch a fresh copy of the usercfg from midas in
			// case they've recently added themselves to fb, but GTFOTUT
			control.fetchMyUserConfig(new UserConfigCallback() {
				public void success(UserConfig freshUc) {
					if (freshUc.getItem("facebookId") == null) {
						// They haven't associated their facebook account with their rbnb one... open a browser window on the page to do so
						try {
							NetUtil.browse(control.getConfig().getWebsiteUrlBase()+"before-facebook-attach");
						} catch (IOException e) {
							throw new Errot(e);
						}
					} else {
						runOnUiThread(new CatchingRunnable() {
							public void doRun() throws Exception {
								showSheet(new PostToFacebookSheet(RobonoboFrame.this, p));
							}
						});
					}

				}
				
				public void error(long userId, Exception e) {
				}
			});
		} else
			showSheet(new PostToFacebookSheet(this, p));
	}
	
	public void postToTwitter(final Playlist p) {
		if(!SwingUtilities.isEventDispatchThread())
			throw new Errot();
		UserConfig uc = control.getMyUserConfig();
		if (uc == null || uc.getItem("twitterId") == null) {
			// We don't seem to be registered for twitter - fetch a fresh copy of the usercfg from midas in
			// case they've recently added themselves, but GTFOTUT
			control.fetchMyUserConfig(new UserConfigCallback() {
				public void success(UserConfig freshUc) {
					if (freshUc.getItem("twitterId") == null) {
						// They haven't associated their twitter account with their rbnb one...open a browser window on the page to do so
						try {
							NetUtil.browse(control.getConfig().getWebsiteUrlBase()+"before-twitter-attach");
						} catch (IOException e) {
							throw new Errot(e);
						}
					} else {
						runOnUiThread(new CatchingRunnable() {
							public void doRun() throws Exception {
								showSheet(new PostToTwitterSheet(RobonoboFrame.this, p));
							}
						});
					}

				}
				
				public void error(long userId, Exception e) {
				}
			});
		} else
			showSheet(new PostToTwitterSheet(this, p));
	}
	
	public static Image getRobonoboIconImage() {
		return GuiUtil.getImage("/rbnb-icon-128x128.png");
	}

	public void shutdown() {
		setVisible(false);
		Thread shutdownThread = new Thread(new CatchingRunnable() {
			public void doRun() throws Exception {
				control.shutdown();
				System.exit(0);
			}
		});
		shutdownThread.start();
	}

	public void restart() {
		log.fatal("robonobo restarting");
		Thread restartThread = new Thread(new CatchingRunnable() {
			public void doRun() throws Exception {
				// Show a message that we're restarting
				SwingUtilities.invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						String[] butOpts = { "Quit" };
						int result = JOptionPane.showOptionDialog(RobonoboFrame.this,
								"robonobo is restarting, please wait...", "robonobo restarting",
								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, butOpts,
								"Force Quit");
						if (result >= 0) {
							// They pressed the button... just kill everything
							log.fatal("Emergency shutdown during restart... pressing Big Red Switch");
							System.exit(1);
						}
					}
				});
				// Shut down the controller - this will block until the
				// controller exits
				control.shutdown();
				// Hide this frame - don't dispose of it yet as this might make
				// the jvm exit
				SwingUtilities.invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						RobonoboFrame.this.setVisible(false);
					}
				});
				// Startup a new frame and controller
				Robonobo.startup(null, cmdLineArgs, false);
				// Dispose of the old frame
				RobonoboFrame.this.dispose();
			}
		});
		restartThread.setName("Restart");
		restartThread.start();
	}

	public RobonoboController getController() {
		return control;
	}

	public void confirmThenShutdown() {
		invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				// If we aren't sharing anything, just close
				if (getController().getShares().size() == 0) {
					shutdown();
					return;
				}
				// Likewise, if they've asked us not to confirm
				if (!getGuiConfig().getConfirmExit()) {
					shutdown();
					return;
				}
				showSheet(new ConfirmCloseSheet(RobonoboFrame.this));
			}
		});
	}

	/**
	 * For slow things that have to happen on the gui thread - shows a helpful message to mollify the user while their ui is frozen
	 * @param pFetcher This will be run on the gui thread
	 */
	public void runSlowTask(final String whatsHappening, final Runnable task) {
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				final PleaseWaitSheet sheet = new PleaseWaitSheet(RobonoboFrame.this, whatsHappening);
				showSheet(sheet);
				invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						task.run();
						sheet.setVisible(false);
					}
				});
			}
		});
	}
	
	class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			confirmThenShutdown();
		}
	}

	class KeyEventHandler implements KeyEventDispatcher {
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			int code = e.getKeyCode();
			int modifiers = e.getModifiers();
			if (code == KeyEvent.VK_ESCAPE) {
				if (isShowingSheet()) {
					// If this is the initial login sheet, don't let them escape it
					Sheet sh = getTopSheet();
					if(sh instanceof LoginSheet) {
						LoginSheet lsh = (LoginSheet) sh;
						if(!lsh.getCancelAllowed())
							return false;
					}
					discardTopSheet();
					return true;
				}
			}
			if (code == KeyEvent.VK_Q && modifiers == Platform.getPlatform().getCommandModifierMask()) {
				confirmThenShutdown();
				return true;
			}
			return false;
		}
	}
}