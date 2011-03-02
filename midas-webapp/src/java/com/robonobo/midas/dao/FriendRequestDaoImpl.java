package com.robonobo.midas.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasFriendRequest;

@Repository("friendRequestDao")
public class FriendRequestDaoImpl extends MidasDao implements FriendRequestDao  {
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasFriendRequestDao#retrieveByUsers(long, long)
	 */
	@Override
	public MidasFriendRequest retrieveByUsers(long requestorId, long requesteeId) {
		Criteria c = getSession().createCriteria(MidasFriendRequest.class);
		c.add(Expression.eq("requestorId", requestorId));
		c.add(Expression.eq("requesteeId", requesteeId));
		List<MidasFriendRequest> list = c.list();
		MidasFriendRequest result = null;
		if(list.size() > 0)
			result = list.get(0);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasFriendRequestDao#retrieveByRequestee(long)
	 */
	@Override
	public List<MidasFriendRequest> retrieveByRequestee(long requesteeId) {
		Criteria c = getSession().createCriteria(MidasFriendRequest.class);
		c.add(Expression.eq("requesteeId", requesteeId));
		List<MidasFriendRequest> list = c.list();
		return list;
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasFriendRequestDao#retrieveByRequestCode(java.lang.String)
	 */
	@Override
	public MidasFriendRequest retrieveByRequestCode(String requestCode) {
		Criteria c = getSession().createCriteria(MidasFriendRequest.class);
		c.add(Expression.eq("requestCode", requestCode));
		List<MidasFriendRequest> list = c.list();
		MidasFriendRequest result = null;
		if(list.size() > 0)
			result = list.get(0);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasFriendRequestDao#save(com.robonobo.midas.model.MidasFriendRequest)
	 */
	@Override
	public void save(MidasFriendRequest req) {
		getSession().saveOrUpdate(req);
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasFriendRequestDao#delete(com.robonobo.midas.model.MidasFriendRequest)
	 */
	@Override
	public void delete(MidasFriendRequest req) {
		getSession().delete(req);
	}
}
