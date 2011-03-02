package com.robonobo.midas.dao;

import org.springframework.stereotype.Repository;

import com.robonobo.core.api.model.Library;
import com.robonobo.midas.model.MidasLibrary;

@Repository("libraryDao")
public class LibraryDaoImpl extends MidasDao implements LibraryDao {
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasLibraryDao#getLibrary(long)
	 */
	@Override
	public Library getLibrary(long userId) {
		return (Library) getSession().get(MidasLibrary.class, userId);
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasLibraryDao#saveLibrary(com.robonobo.core.api.model.Library)
	 */
	@Override
	public void saveLibrary(Library lib) {
		log.debug("Saving library with "+lib.getTracks().size()+" tracks");
		getSession().saveOrUpdate(lib);
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasLibraryDao#deleteLibrary(com.robonobo.core.api.model.Library)
	 */
	@Override
	public void deleteLibrary(Library lib) {
		getSession().delete(lib);
	}
}
