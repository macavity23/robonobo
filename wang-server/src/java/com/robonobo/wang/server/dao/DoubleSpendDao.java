package com.robonobo.wang.server.dao;


public interface DoubleSpendDao {

	public abstract boolean isDoubleSpend(String coinId) throws DAOException;

	public abstract void add(String coinId) throws DAOException;

}