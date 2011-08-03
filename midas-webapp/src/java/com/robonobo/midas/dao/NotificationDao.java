package com.robonobo.midas.dao;

import java.util.Collection;
import java.util.List;

import com.robonobo.midas.model.MidasNotification;

public interface NotificationDao {
	public void saveNotification(MidasNotification n);

	public List<MidasNotification> getAllNotifications();

	public void deleteAllNotificationsTo(long notifiedUser);

	public abstract void deleteNotifications(Collection<MidasNotification> nots);
}