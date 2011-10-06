package com.robonobo.midas.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasStream;

@Repository("streamDao")
public class StreamDaoImpl extends MidasDao implements StreamDao {
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasStreamDao#deleteStream(com.robonobo.midas.model.MidasStream)
	 */
	@Override
	public void deleteStream(MidasStream stream) {
		getSession().delete(stream);
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasStreamDao#findLatest(int)
	 */
	@Override
	public List<MidasStream> findLatest(int limit) {
		Criteria crit = getSession().createCriteria(MidasStream.class);
		crit.addOrder(Order.desc("modified"));
		crit.setMaxResults(limit);
		return crit.list();
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasStreamDao#loadStream(java.lang.String)
	 */
	@Override
	public MidasStream getStream(String streamId) {
		MidasStream stream = (MidasStream) getSession().get(MidasStream.class, streamId);
		return stream;
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasStreamDao#saveStream(com.robonobo.midas.model.MidasStream)
	 */
	@Override
	public void putStream(MidasStream stream) {
		getSession().saveOrUpdate(stream);
	}
}
