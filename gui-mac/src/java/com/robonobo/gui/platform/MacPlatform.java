package com.robonobo.gui.platform;

import java.awt.Event;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import com.apple.eawt.Application;
import com.apple.eawt.OpenURIHandler;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.itunes.ITunesService;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.itunes.mac.MacITunesService;

public class MacPlatform extends UnknownPlatform {

	@Override
	public void init() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "robonobo");
	}

	@Override
	public void setLookAndFeel() {
		// For the menu bar, we keep the L&F from the platform default so that it appears in the expected place for the
		// mac
		// Mad props to Marian Bouček, re http://www.ptakopysk.cz/algi/index.html
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			throw new SeekInnerCalmException();
		}
		String mbUI = UIManager.getString("MenuBarUI");
		super.setLookAndFeel();
		UIManager.put("MenuBarUI", mbUI);
	}

	@Override
	public void initMainWindow(JFrame jFrame) throws Exception {
		RobonoboFrame frame = (RobonoboFrame) jFrame;
		Application app = Application.getApplication();
		// Use all this stuff even though it's deprecated as we want to keep working with old versions of the apple java
		// sdk
		app.addApplicationListener(new MacAppListener(frame));
		app.addAboutMenuItem();
		app.setEnabledAboutMenu(true);
		app.addPreferencesMenuItem();
		app.setEnabledPreferencesMenu(true);
		registerUriHandler(app, frame);
	}

	private void registerUriHandler(Application app, RobonoboFrame frame) {
		// We do this buggering about with reflection as the OpenURIHandler only exists in later versions of the OSX
		// Java API, and we still want to work on previous versions (with a warning message)
		try {
			// This next line will throw ClassNotFoundException if we cannot handle URIs
			Class.forName("com.apple.eawt.OpenURIHandler");
			Class<?> handlerClass = Class.forName("com.robonobo.gui.platform.URIHandler");
			Constructor<?> ctor = handlerClass.getConstructor(frame.getClass());
			app.setOpenURIHandler((OpenURIHandler) ctor.newInstance(frame));
		} catch (ClassNotFoundException e) {
			// Old version of apple java
			// TODO Tell them to update
		} catch (Exception e) {
			throw new SeekInnerCalmException("Exception registering URI handler", e);
		}
	}

	@Override
	public boolean shouldSetMenuBarOnDialogs() {
		return true;
	}

	@Override
	public boolean shouldShowPrefsInFileMenu() {
		return false;
	}

	@Override
	public boolean shouldShowQuitInFileMenu() {
		return false;
	}

	@Override
	public boolean shouldShowOptionsMenu() {
		return false;
	}

	@Override
	public boolean shouldShowAboutInHelpMenu() {
		return false;
	}

	@Override
	public KeyStroke getAccelKeystroke(int key) {
		return KeyStroke.getKeyStroke(key, Event.META_MASK);
	}

	@Override
	public int getCommandModifierMask() {
		return Event.META_MASK;
	}

	@Override
	public boolean iTunesAvailable() {
		return true;
	}

	@Override
	public ITunesService getITunesService() {
		return new MacITunesService();
	}

	@Override
	public void customizeMainbarButtons(List<? extends JButton> btns) {
		for (int i = 0; i < btns.size(); i++) {
			JButton btn = btns.get(i);
			btn.putClientProperty("JButton.buttonType", "bevel");
		}
	}

	@Override
	public void customizeSearchTextField(JTextField field) {
		field.putClientProperty("JTextField.variant", "search");
	}

	@Override
	public File getDefaultHomeDirectory() {
		File libDir = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "Library");
		File appSupDir = new File(libDir, "Application Support");
		return new File(appSupDir, "robonobo");
	}
}
