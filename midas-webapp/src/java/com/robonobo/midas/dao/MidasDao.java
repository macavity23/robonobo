package com.robonobo.midas.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public abstract class MidasDao extends HibernateDaoSupport {
	protected Log log = LogFactory.getLog(getClass());
	
	@Autowired
	public void injectSessionFactory(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
}
