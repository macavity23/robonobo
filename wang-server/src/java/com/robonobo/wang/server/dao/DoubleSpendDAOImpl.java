package com.robonobo.wang.server.dao;

import static com.robonobo.common.util.TextUtil.*;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("doubleSpendDao")
public class DoubleSpendDAOImpl implements DoubleSpendDao {
	private static final String CHECK_SQL = "SELECT count(*) FROM doublespend WHERE coin_hash = ?";
	private static final String ADD_SQL = "INSERT INTO doublespend (coin_hash) VALUES (?)";

	private JdbcTemplate db;
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		db = new JdbcTemplate(dataSource);
	}

	@Override
	public boolean isDoubleSpend(String coinId) throws DAOException {
		long coinIdHash = longHash(coinId);
		try {
			return db.queryForInt(CHECK_SQL, coinIdHash) > 0;
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}

	@Override
	public void add(String coinId) throws DAOException {
		long coinIdHash = longHash(coinId);
		try {
			db.update(ADD_SQL, coinIdHash);
		} catch (DataAccessException e) {
			throw new DAOException(e);
		}
	}
}
