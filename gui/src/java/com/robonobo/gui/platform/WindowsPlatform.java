package com.robonobo.gui.platform;

import java.io.File;

import javax.swing.UIManager;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.itunes.ITunesService;
import com.robonobo.gui.itunes.windows.WindowsITunesService;

public class WindowsPlatform extends UnknownPlatform {
	@Override
	public File getDefaultHomeDirectory() {
		return new File(System.getenv("APPDATA"), "robonobo");
	}

	@Override
	public File getDefaultDownloadDirectory() {
		String s = File.separator;
		return new File(System.getenv("USERPROFILE")+s+"My Documents"+s+"My Music"+s+"robonobo");
	}
	
	@Override
	public boolean iTunesAvailable() {
		return true;
	}
	
	@Override
	public ITunesService getITunesService() {
		return new WindowsITunesService();
	}
}
