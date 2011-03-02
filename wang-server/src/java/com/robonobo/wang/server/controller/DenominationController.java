package com.robonobo.wang.server.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.robonobo.wang.beans.DenominationPublic;
import com.robonobo.wang.proto.WangProtocol.DenominationListMsg;
import com.robonobo.wang.server.dao.DAOException;
import com.robonobo.wang.server.dao.DenominationDao;

@Controller
public class DenominationController extends BaseController implements InitializingBean {
	private List<DenominationPublic> pubDenoms;
	@Autowired
	private DenominationDao denominationDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		pubDenoms = denominationDao.getDenomsPublic();
	}
	
	@RequestMapping(value="/getDenominations")
	public void getDenominations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(getAuthUser(req, resp) == null) {
			send401(req, resp);
			return;
		}
		resp.setContentType("application/data");
		DenominationListMsg.Builder bldr = DenominationListMsg.newBuilder();
		for (DenominationPublic denom : pubDenoms) {
			bldr.addDenomination(denom.toMsg());
		}
		writeToOutput(bldr.build(), resp);
	}
}
