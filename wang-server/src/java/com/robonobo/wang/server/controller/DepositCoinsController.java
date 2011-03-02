package com.robonobo.wang.server.controller;

import static com.robonobo.common.util.TextUtil.*;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.robonobo.common.util.TextUtil;
import com.robonobo.wang.beans.Coin;
import com.robonobo.wang.beans.DenominationPrivate;
import com.robonobo.wang.client.LucreFacade;
import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinMsg;
import com.robonobo.wang.proto.WangProtocol.DepositStatusMsg;
import com.robonobo.wang.proto.WangProtocol.DepositStatusMsg.Status;
import com.robonobo.wang.server.UserAccount;
import com.robonobo.wang.server.dao.*;

@Controller
public class DepositCoinsController extends BaseController implements InitializingBean {
	@Autowired
	private DenominationDao denominationDao;
	@Autowired
	private DoubleSpendDao doubleSpendDao;
	private LucreFacade lucre;
	private Map<Integer, DenominationPrivate> denomPrivs;

	@Override
	public void afterPropertiesSet() throws Exception {
		lucre = new LucreFacade();
		try {
			List<DenominationPrivate> denoms = denominationDao.getDenomsPrivate();
			denomPrivs = new HashMap<Integer, DenominationPrivate>();
			for (DenominationPrivate denom : denoms) {
				denomPrivs.put(denom.getDenom(), denom);
			}
		} catch (DAOException e) {
			throw new ServletException(e);
		}
	}

	@RequestMapping(value = "/depositCoins")
	@Transactional(rollbackFor=Exception.class)
	public void depositCoins(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserAccount user = getAuthUser(req, resp);
		if (user == null) {
			send401(req, resp);
			return;
		}
		resp.setContentType("application/data");
		CoinListMsg.Builder clBldr = CoinListMsg.newBuilder();
		readFromInput(clBldr, req);
		CoinListMsg cl = clBldr.build();
		double coinValue = 0;
		DepositStatusMsg.Builder dBldr = DepositStatusMsg.newBuilder();
		dBldr.setStatus(Status.OK);
		try {
			List<String> deposCoins = new ArrayList<String>();
			for (CoinMsg coinMsg : cl.getCoinList()) {
				DenominationPrivate denom = denomPrivs.get(coinMsg.getDenom());
				Coin coin = new Coin(coinMsg);
				if (denom == null)
					throw new IOException("Malformed coin, no denomination");
				boolean lucreSaysOk = lucre.verifyCoin(denom, coin);
				boolean isDoubleSpend = doubleSpendDao.isDoubleSpend(coinMsg.getCoinId());
				if (!lucreSaysOk || isDoubleSpend) {
					dBldr.setStatus(Status.Error);
					dBldr.addBadCoinId(coinMsg.getCoinId());
					continue;
				}
				coinValue += getDenomValue(coin.getDenom());
				deposCoins.add(coinMsg.getCoinId());
			}
			if (dBldr.getStatus() == Status.OK) {
				for (String coinId : deposCoins) {
					doubleSpendDao.add(coinId);
				}
				log.info("User " + user.getEmail() + " deposited " + deposCoins.size() + " coins worth " + WANG_CHAR
						+ coinValue);
				try {
					UserAccount lockUser = uaDao.getAndLockUserAccount(user.getEmail());
					lockUser.setBalance(lockUser.getBalance() + coinValue);
					uaDao.putUserAccount(lockUser);
				} catch (DAOException e) {
					throw new IOException(e);
				}
			} else
				log.warn("User " + user.getEmail() + " got bad coin error while attempting to deposit coins");
		} catch (DAOException e) {
			throw new IOException(e);
		}
		writeToOutput(dBldr.build(), resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	private double getDenomValue(Integer denom) {
		return Math.pow(2, denom);
	}
}
