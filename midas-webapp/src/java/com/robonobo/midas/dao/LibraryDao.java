package com.robonobo.midas.dao;

import com.robonobo.core.api.model.Library;

public interface LibraryDao {

	public abstract Library getLibrary(long userId);

	public abstract void saveLibrary(Library lib);

	public abstract void deleteLibrary(Library lib);

}