/**
 * 
 */
package com.robonobo.gui.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import com.robonobo.common.exceptions.SeekInnerCalmException;

public class StreamTransfer implements Transferable {
	public static DataFlavor DATA_FLAVOR;
	private static DataFlavor[] flavors;

	static {
		try {
			DATA_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=com.robonobo.gui.model.StreamIdList");
			flavors = new DataFlavor[1];
			flavors[0] = DATA_FLAVOR;
		} catch (ClassNotFoundException e) {
			throw new SeekInnerCalmException();
		}
	}

	private List<String> streamIds;

	public StreamTransfer(List<String> streamIds) {
		this.streamIds = streamIds;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DATA_FLAVOR)) {
			return streamIds;
		} else
			throw new UnsupportedFlavorException(flavor);
	}

	public List<String> getStreamIds() {
		return streamIds;
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(DATA_FLAVOR));
	}
}