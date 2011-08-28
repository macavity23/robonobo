package com.robonobo.midas.dao;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasPlaylist;

@Repository("playlistDao")
public class PlaylistDaoImpl extends MidasDao implements PlaylistDao {
	@Override
	public long getHighestPlaylistId() {
		String hql = "select max(playlistId) from MidasPlaylist";
		List l = getSession().createQuery(hql).list();
		if(l.size() == 0 || l.get(0) == null)
			return 0;
		return ((Long)l.get(0)).longValue();
	}
	
	@Override
	public void deletePlaylist(MidasPlaylist playlist) {
		getSession().delete(playlist);
	}

	@Override
	public MidasPlaylist getPlaylistById(long playlistId) {
		return (MidasPlaylist) getSession().get(MidasPlaylist.class, playlistId);
	}

	@Override
	public MidasPlaylist getPlaylistByUserIdAndTitle(long uid, String title) {
		String hql = "from MidasPlaylist as mp where :title = lower(mp.title) and :uid in elements(mp.ownerIds)";
		Query q = getSession().createQuery(hql);
		q.setParameter("title", title.toLowerCase());
		q.setParameter("uid", uid);
		List result = q.list();
		if(result.size() == 0)
			return null;
		return (MidasPlaylist) result.get(1);
	}
	
	@Override
	public void savePlaylist(MidasPlaylist playlist) {
		sanitizePlaylist(playlist);
		getSession().saveOrUpdate(playlist);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<MidasPlaylist> getRecentPlaylists(long maxAgeMs) {
		Date maxAge = timeInPast(maxAgeMs);
		String hql = "from MidasPlaylist where updated > :maxAge";
		Query q = getSession().createQuery(hql);
		q.setParameter("maxAge", maxAge, Hibernate.TIMESTAMP);
		return q.list();
	}
	
	private void sanitizePlaylist(MidasPlaylist p) {
		p.setTitle(truncate(p.getTitle(), 128));
		p.setDescription(truncate(p.getDescription(), 512));
	}
}
