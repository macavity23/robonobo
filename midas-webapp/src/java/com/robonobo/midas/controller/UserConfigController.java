package com.robonobo.midas.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.midas.model.MidasUserConfig;

@Controller
@RequestMapping("/userconfig/{uIdStr}")
public class UserConfigController extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public void getUserConfig(@PathVariable("uIdStr") String uIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		long uId = Long.parseLong(uIdStr, 16);
		MidasUser authUser = getAuthUser(req);
		if (authUser == null || authUser.getUserId() != uId) {
			send401(req, resp);
			return;
		}
		MidasUserConfig config = midas.getUserConfig(authUser);
		if (config == null) {
			config = new MidasUserConfig();
			config.setUserId(authUser.getUserId());
		}
		writeToOutput(config.toMsg(), resp);
	}

	@RequestMapping(method = RequestMethod.PUT)
	public void putUserConfig(@PathVariable("uIdStr") String uIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		long uId = Long.parseLong(uIdStr, 16);
		MidasUser authUser = getAuthUser(req);
		if (authUser == null || authUser.getUserId() != uId) {
			send401(req, resp);
			return;
		}
		UserConfigMsg.Builder b = UserConfigMsg.newBuilder();
		readFromInput(b, req);
		MidasUserConfig newCfg = new MidasUserConfig(b.build());
		MidasUserConfig curCfg = midas.getUserConfig(authUser);
		if (curCfg == null)
			midas.putUserConfig(newCfg);
		else {
			// User has existing config - add/replace items from the serialized one
			for (String iName : newCfg.getItems().keySet()) {
				curCfg.getItems().put(iName, newCfg.getItems().get(iName));
			}
			midas.putUserConfig(curCfg);
		}
	}
}
