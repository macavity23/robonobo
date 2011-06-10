package com.robonobo.midas.client;

import com.robonobo.core.api.model.Library;
import com.robonobo.core.metadata.LibraryHandler;

/**
 * Just extends AddToLibReq as midas handles both adds & dels via http PUT as DELETE doesn't get the body passed through
 * @author macavity
 *
 */
public class DeleteFromLibraryRequest extends AddToLibraryRequest {
	public DeleteFromLibraryRequest(MidasClientConfig cfg, long userId, Library lib, LibraryHandler handler) {
		super(cfg, userId, lib, handler);
	}

	@Override
	protected String getUrl() {
		return cfg.getLibraryDelUrl(userId);
	}
}
