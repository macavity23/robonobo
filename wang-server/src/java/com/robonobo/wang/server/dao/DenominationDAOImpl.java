package com.robonobo.wang.server.dao;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.robonobo.wang.beans.DenominationPrivate;
import com.robonobo.wang.beans.DenominationPublic;

@Repository("denominationDao")
public class DenominationDAOImpl implements DenominationDao {
	private static final String GET_DENOMS_SQL = "SELECT * FROM denomination";
	static final String DELETE_DENOMS_SQL = "DELETE FROM denomination";
	static final String INSERT_DENOM_SQL = "INSERT INTO denomination (id, denom, generator, prime, public_key, private_key) VALUES (?, ?, ?, ?, ?, ?)";
	private JdbcTemplate db;
	private DenomPublicMapper denomPubMapper = new DenomPublicMapper();
	private DenomPrivateMapper denomPrivMapper = new DenomPrivateMapper();
	private Log log = LogFactory.getLog(getClass());

	@Autowired
	public void setDataSource(DataSource dataSource) {
		db = new JdbcTemplate(dataSource);
	}

	@Override
	public List<DenominationPublic> getDenomsPublic() throws DAOException {
		try {
			return db.query(GET_DENOMS_SQL, denomPubMapper);
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}

	@Override
	public List<DenominationPrivate> getDenomsPrivate() throws DAOException {
		try {
			return db.query(GET_DENOMS_SQL, denomPrivMapper);
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}

	@Override
	public void deleteAllDenoms() throws DAOException {
		try {
			db.update(DELETE_DENOMS_SQL);
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}

	}

	@Override
	public void putDenom(DenominationPrivate denom) throws DAOException {
		try {
			db.update(INSERT_DENOM_SQL, 
					denom.getPrivateKey().hashCode(), 
					denom.getDenom(), 
					denom.getGenerator().toString(), 
					denom.getPrime().toString(), 
					denom.getPublicKey().toString(), 
					denom.getPrivateKey().toString());
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}

	private class DenomPublicMapper implements RowMapper<DenominationPublic> {
		@Override
		public DenominationPublic mapRow(ResultSet rs, int rowNum) throws SQLException {
			BigInteger gen = new BigInteger(rs.getString("generator"));
			BigInteger prime = new BigInteger(rs.getString("prime"));
			BigInteger pubKey = new BigInteger(rs.getString("public_key"));
			DenominationPublic result = new DenominationPublic(gen, prime, pubKey);
			result.setDenom(rs.getInt("denom"));
			return result;
		}
	}

	private class DenomPrivateMapper implements RowMapper<DenominationPrivate> {
		@Override
		public DenominationPrivate mapRow(ResultSet rs, int rowNum) throws SQLException {
			BigInteger gen = new BigInteger(rs.getString("generator"));
			BigInteger prime = new BigInteger(rs.getString("prime"));
			BigInteger pubKey = new BigInteger(rs.getString("public_key"));
			BigInteger priKey = new BigInteger(rs.getString("private_key"));
			DenominationPrivate result = new DenominationPrivate(gen, prime, pubKey, priKey);
			result.setDenom(rs.getInt("denom"));
			return result;
		}
	}
}
