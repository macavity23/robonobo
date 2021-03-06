package com.robonobo.midas.controller;

import static com.robonobo.common.util.TextUtil.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.robonobo.core.api.proto.CoreApi.UserMsg;
import com.robonobo.midas.EventService;
import com.robonobo.midas.MessageService;
import com.robonobo.midas.model.MidasUser;

@Controller
public class UserController extends BaseController {
	@Autowired
	MessageService message;
	@Autowired
	EventService event;
	
	@RequestMapping(value="/users/byid/{uIdStr}", method=RequestMethod.GET)
	public void getUserById(@PathVariable("uIdStr") String uIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser authUser = getAuthUser(req);
		if(authUser == null) {
			send401(req, resp);
			return;
		}
		long uId = Long.parseLong(uIdStr, 16);
		MidasUser targetUser = midas.getUserById(uId);
		if(targetUser == null) {
			send404(req, resp);
			return;
		}
		getUser(targetUser, authUser, req, resp);
		if(targetUser.getUserId() == authUser.getUserId())
			event.userRemainsOnline(targetUser);
	}
	
	@RequestMapping(value="/users/byemail/{email}.{ext}", method=RequestMethod.GET) 
	public void getUserByEmail(@PathVariable("email") String emailStr, @PathVariable("ext") String ext, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser authUser = getAuthUser(req);
		if(authUser == null) {
			send401(req, resp);
			return;
		}
		// Spring's habit of chopping off the file extension is rather annoying
		String email = urlDecode(emailStr)+"."+ext;
		MidasUser targetUser = midas.getUserByEmail(email);
		if(targetUser == null) {
			send404(req, resp);
			return;
		}
		getUser(targetUser, authUser, req, resp);
		if(targetUser.getUserId() == authUser.getUserId())
			event.userLoggedIn(targetUser);
	}
	
	@RequestMapping(value="/users/testing-topup")
	public void requestTopUp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser user = getAuthUser(req);
		if(user == null) {
			send401(req, resp);
			return;
		}
		message.sendTopUpRequest(user);
		resp.setContentType("text/plain");
		resp.getWriter().println("TopUp request received OK.");
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
	protected void getUser(MidasUser targetUser, MidasUser authUser, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser returnUser = midas.getUserAsVisibleBy(targetUser, authUser);
		UserMsg uMsg = returnUser.toMsg(false);
		writeToOutput(uMsg, resp);
		log.debug("User "+authUser.getEmail()+" retrieving user: "+uMsg);
	}
}
