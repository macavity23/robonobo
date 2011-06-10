package com.robonobo.gui.platform;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.TextUtil;


public class LinuxPlatform extends UnknownPlatform {
	private static DataFlavor importFlavor;
	static {
		try {
			importFlavor = new DataFlavor("text/uri-list; class=java.lang.String");
		} catch (ClassNotFoundException e) {
			throw new Errot();
		}
	}
	
	@Override
	public int getNumberOfShakesForShakeyWindow() {
		return 25;
	}

	@Override
	public boolean canDnDImport(DataFlavor[] transferFlavors) {
		for (DataFlavor dataFlavor : transferFlavors) {
			if(importFlavor.equals(dataFlavor))
				return true;
		}
		return false;
	}

	@Override
	public List<File> getDnDImportFiles(Transferable t) throws IOException {
		Pattern fileUrlPat = Pattern.compile("^\\s*file://(.*)\\s*$");
		List<File> results = new ArrayList<File>();
		try {
			String allFilesStr = (String) t.getTransferData(importFlavor);
			String[] fileUrls = allFilesStr.split("\n");
			for (String url : fileUrls) {
				Matcher m = fileUrlPat.matcher(url);
				if(m.matches()) {
					results.add(new File(TextUtil.urlDecode(m.group(1))));
				}
			}
			return results;
		} catch (UnsupportedFlavorException e) {
			throw new Errot(e);
		}
	}
}
