package com.robonobo.midas.model;

import com.robonobo.core.api.model.Library;
import com.robonobo.core.api.proto.CoreApi.LibraryMsg;

public class MidasLibrary extends Library {
	public MidasLibrary() {
	}

	public MidasLibrary(LibraryMsg msg) {
		super(msg);
	}
}
