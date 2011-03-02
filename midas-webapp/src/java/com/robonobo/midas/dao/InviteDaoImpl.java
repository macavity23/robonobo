package com.robonobo.midas.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasInvite;

@Repository("inviteDao")
public class InviteDaoImpl extends MidasDao implements InviteDao {
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasInviteDao#retrieveByEmail(java.lang.String)
	 */
	@Override
	public MidasInvite retrieveByEmail(String email) {
		Criteria c = getSession().createCriteria(MidasInvite.class);
		c.add(Expression.eq("email", email));
		List<MidasInvite> list = c.list();
		MidasInvite result = null;
		if(list.size() > 0)
			result = list.get(0);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasInviteDao#retrieveByInviteCode(java.lang.String)
	 */
	@Override
	public MidasInvite retrieveByInviteCode(String inviteCode) {
		Criteria c = getSession().createCriteria(MidasInvite.class);
		c.add(Expression.eq("inviteCode", inviteCode));
		List<MidasInvite> list = c.list();
		MidasInvite result = null;
		if(list.size() > 0)
			result = list.get(0);
		return result;	
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasInviteDao#save(com.robonobo.midas.model.MidasInvite)
	 */
	@Override
	public void save(MidasInvite invite) {
		getSession().saveOrUpdate(invite);
	}
	
	/* (non-Javadoc)
	 * @see com.robonobo.midas.dao.MidasInviteDao#delete(com.robonobo.midas.model.MidasInvite)
	 */
	@Override
	public void delete(MidasInvite invite) {
		getSession().delete(invite);
	}
}
