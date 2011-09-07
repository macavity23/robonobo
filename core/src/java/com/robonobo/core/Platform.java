package com.robonobo.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.*;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.Robonobo;
import com.robonobo.core.itunes.ITunesService;

public abstract class Platform {
	private static Platform platform;

	static {
		platform = detectPlatform();
	}

	private static Platform detectPlatform() {
		String os = System.getProperty("os.name").toLowerCase();

		String platformClassName = "com.robonobo.gui.platform.UnknownPlatform";
		// These classes are instantiated by reflection as some of them (hello,
		// Apple!) use platform-specific library classes which means things
		// won't compile on a different platform. So, we build them in a
		// separate project and add them to the classpath at runtime
		if (os.contains("windows"))
			platformClassName = "com.robonobo.gui.platform.WindowsPlatform";
		else if (os.contains("os x"))
			platformClassName = "com.robonobo.gui.platform.MacPlatform";
		else if (os.contains("linux"))
			platformClassName = "com.robonobo.gui.platform.LinuxPlatform";
		try {
			platform = (Platform) Class.forName(platformClassName).newInstance();
		} catch (Exception e) {
			throw new SeekInnerCalmException(e);
		}
		return platform;
	}

	public static Platform getPlatform() {
		return platform;
	}

	/** Called by main(), before anything is set up */
	public abstract void init();

	/** Called by the robonobo instance after creation, but before services are started */
	public abstract void initRobonobo(Robonobo ro);
	
	public abstract void setLookAndFeel();

	public abstract void initMainWindow(JFrame frame) throws Exception;

	public abstract JMenuBar getMenuBar(JFrame frame);

	public abstract boolean shouldSetMenuBarOnDialogs();

	public abstract File getDefaultHomeDirectory();

	public abstract File getDefaultDownloadDirectory();

	public abstract boolean shouldShowPrefsInFileMenu();

	public abstract boolean shouldShowQuitInFileMenu();

	public abstract boolean shouldShowAboutInHelpMenu();

	public abstract boolean shouldShowOptionsMenu();
	
	public abstract int getNumberOfShakesForShakeyWindow();

	public abstract boolean canDnDImport(DataFlavor[] transferFlavors);

	public abstract List<File> getDnDImportFiles(Transferable t) throws IOException;

	public abstract KeyStroke getAccelKeystroke(int key);
	
	public abstract int getCommandModifierMask();

	public abstract boolean iTunesAvailable();
	
	public abstract ITunesService getITunesService();

	public abstract void customizeMainbarButtons(List<? extends JButton> btns);

	public abstract void customizeSearchTextField(JTextField field);
	
	/**
	 * Return null to disable showing files in file manager
	 */
	public abstract String fileManagerName();
	
	public abstract void showFileInFileManager(File file);
}
