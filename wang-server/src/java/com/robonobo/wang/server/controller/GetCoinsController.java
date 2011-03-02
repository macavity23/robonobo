package com.robonobo.wang.server.controller;

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

import com.robonobo.wang.beans.*;
import com.robonobo.wang.client.LucreFacade;
import com.robonobo.wang.proto.WangProtocol.BlindedCoinListMsg;
import com.robonobo.wang.proto.WangProtocol.BlindedCoinListMsg.Status;
import com.robonobo.wang.proto.WangProtocol.CoinRequestListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinRequestMsg;
import com.robonobo.wang.server.UserAccount;
import com.robonobo.wang.server.dao.DAOException;
import com.robonobo.wang.server.dao.DenominationDao;

@Controller
public class GetCoinsController extends BaseController implements InitializingBean {
	@Autowired
	private DenominationDao denominationDao;
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

	@Transactional(rollbackFor=Exception.class)
	@RequestMapping(value="/getCoins")
	public void getCoins(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserAccount user = getAuthUser(req, resp);
		if (user == null) {
			send401(req, resp);
			return;
		}
		resp.setContentType("application/data");
		CoinRequestListMsg.Builder crlBldr = CoinRequestListMsg.newBuilder();
		readFromInput(crlBldr, req);
		CoinRequestListMsg crl = crlBldr.build();
		double coinValue = 0;
		for (CoinRequestMsg coinReq : crl.getCoinRequestList()) {
			coinValue += getDenomValue(coinReq.getDenom());
		}
		BlindedCoinListMsg.Builder blBldr = BlindedCoinListMsg.newBuilder();
		try {
			UserAccount lockUser = uaDao.getAndLockUserAccount(user.getEmail());
			if (lockUser.getBalance() < coinValue) {
				blBldr.setStatus(Status.InsufficientWang);
			} else {
				for (CoinRequestMsg coinReq : crl.getCoinRequestList()) {
					DenominationPrivate denom = denomPrivs.get(coinReq.getDenom());
					BlindedCoin bc = lucre.signCoinRequest(denom, new CoinRequestPublic(coinReq));
					blBldr.addCoin(bc.toMsg());
				}
				blBldr.setStatus(Status.OK);
				lockUser.setBalance(lockUser.getBalance() - coinValue);
				uaDao.putUserAccount(lockUser);
			}
		} catch (DAOException e) {
			throw new IOException(e);
		}
		writeToOutput(blBldr.build(), resp);
		resp.setStatus(HttpServletResponse.SC_OK);
		log.info("User "+user.getEmail()+" withdrew "+crl.getCoinRequestCount()+" coins worth "+WANG_CHAR+coinValue);
	}

	private double getDenomValue(Integer denom) {
		return Math.pow(2, denom);
	}

}
