package com.robonobo.midas.dao;

import com.robonobo.midas.model.MidasEvent;

public interface EventDao {
	public void saveEvent(MidasEvent event);
}