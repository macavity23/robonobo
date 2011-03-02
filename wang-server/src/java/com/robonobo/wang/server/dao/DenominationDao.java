package com.robonobo.wang.server.dao;

import java.util.List;

import com.robonobo.wang.beans.DenominationPrivate;
import com.robonobo.wang.beans.DenominationPublic;

public interface DenominationDao {

	public abstract List<DenominationPublic> getDenomsPublic() throws DAOException;

	public abstract List<DenominationPrivate> getDenomsPrivate() throws DAOException;

	public abstract void deleteAllDenoms() throws DAOException;

	public abstract void putDenom(DenominationPrivate denom) throws DAOException;

}