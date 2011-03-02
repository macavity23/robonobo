package com.robonobo.midas.dao;

import static com.robonobo.common.util.TextUtil.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasPlaylist;

@Repository("playlistDao")
public class PlaylistDaoImpl extends MidasDao implements PlaylistDao {
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasPlaylistDao#getHighestPlaylistId()
	 */
	@Override
	public long getHighestPlaylistId() {
		String hql = "select max(playlistId) from MidasPlaylist";
		List l = getSession().createQuery(hql).list();
		if(l.size() == 0 || l.get(0) == null)
			return 0;
		return ((Long)l.get(0)).longValue();
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasPlaylistDao#deletePlaylist(com.robonobo.midas.model.MidasPlaylist)
	 */
	@Override
	public void deletePlaylist(MidasPlaylist playlist) {
		getSession().delete(playlist);
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasPlaylistDao#loadPlaylist(long)
	 */
	@Override
	public MidasPlaylist loadPlaylist(long playlistId) {
		return (MidasPlaylist) getSession().get(MidasPlaylist.class, playlistId);
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasPlaylistDao#savePlaylist(com.robonobo.midas.model.MidasPlaylist)
	 */
	@Override
	public void savePlaylist(MidasPlaylist playlist) {
		sanitizePlaylist(playlist);
		getSession().saveOrUpdate(playlist);
	}
	
	private void sanitizePlaylist(MidasPlaylist p) {
		p.setTitle(truncate(p.getTitle(), 128));
		p.setDescription(truncate(p.getDescription(), 512));
	}
}
