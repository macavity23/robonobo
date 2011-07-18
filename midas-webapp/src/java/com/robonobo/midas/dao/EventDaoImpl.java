package com.robonobo.midas.dao;

import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasEvent;

@Repository("eventDao")
public class EventDaoImpl extends MidasDao implements EventDao {
	@Override
	public void saveEvent(MidasEvent event) {
		getSession().saveOrUpdate(event);
	}
}
