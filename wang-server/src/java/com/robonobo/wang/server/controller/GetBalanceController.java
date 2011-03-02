package com.robonobo.wang.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.robonobo.wang.proto.WangProtocol.BalanceMsg;
import com.robonobo.wang.server.UserAccount;

@Controller
public class GetBalanceController extends BaseController {
	@RequestMapping("/getBalance")
	@Transactional(rollbackFor=Exception.class)
	public void getBalance(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserAccount ua = getAuthUser(req, resp);
		if(ua == null) {
			send401(req, resp);
			return;
		}
		resp.setContentType("application/data");
		writeToOutput(BalanceMsg.newBuilder().setAmount(ua.getBalance()).build(), resp);
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
