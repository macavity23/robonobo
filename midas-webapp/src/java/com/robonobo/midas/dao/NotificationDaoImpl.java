package com.robonobo.midas.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasNotification;

@Repository("notificationDao")
public class NotificationDaoImpl extends MidasDao implements NotificationDao {
	
	@Override
	public void saveNotification(MidasNotification n) {
		getSession().save(n);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<MidasNotification> getAllNotifications() {
		Criteria c = getSession().createCriteria(MidasNotification.class);
		return c.list();
	}
	
	@Override
	public void deleteNotifications(Collection<MidasNotification> nots) {
		Session s = getSession();
		for(MidasNotification n : nots) {
			s.delete(n);
		}
	}
	
	@Override
	public void deleteAllNotificationsTo(long notifiedUser) {
		String hql = "delete MidasNotification where notifUserId = :uid";
		getSession().createQuery(hql).setLong("uid", notifiedUser).executeUpdate();
	}
}
