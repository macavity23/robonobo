package com.robonobo.midas.model;

import java.util.Date;

public class MidasNotification {
	long notifId;
	long updateUserId;
	long notifUserId;
	String item;
	Date date;

	public MidasNotification() {
	}

	
	public MidasNotification(long updateUserId, long notifUserId, String item) {
		this.updateUserId = updateUserId;
		this.notifUserId = notifUserId;
		this.item = item;
		date = new Date();
	}


	public long getNotifId() {
		return notifId;
	}

	public void setNotifId(long notifId) {
		this.notifId = notifId;
	}

	public long getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(long updateUserId) {
		this.updateUserId = updateUserId;
	}

	public long getNotifUserId() {
		return notifUserId;
	}

	public void setNotifUserId(long notifUserId) {
		this.notifUserId = notifUserId;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
