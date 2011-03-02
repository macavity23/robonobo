package com.robonobo.midas.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.model.Library;
import com.robonobo.core.api.model.UserConfig;
import com.robonobo.core.api.proto.CoreApi.LibraryMsg;
import com.robonobo.midas.LocalMidasService;
import com.robonobo.midas.model.MidasLibrary;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.remote.service.MidasService;

@Controller
public class LibraryController extends BaseController {
	@RequestMapping("/library/{userIdStr}")
	public void getLibrary(@PathVariable("userIdStr") String userIdStr, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long reqUserId = Long.parseLong(userIdStr, 16);
		MidasUser authUser = getAuthUser(req);
		if (authUser == null) {
			send401(req, resp);
			return;
		}
		long authUid = authUser.getUserId();
		MidasUser reqUser = midas.getUserById(reqUserId);
		boolean allowed = (authUid == reqUserId || authUser.getFriendIds().contains(reqUserId));
		if (allowed) {
			// If the user has disabled library sharing, just send them an empty library
			UserConfig cfg = midas.getUserConfig(reqUser);
			if (cfg != null && "false".equals(cfg.getItems().get("sharelibrary"))) {
				log.info("Returning blank library for " + reqUser.getEmail() + " to " + authUser.getEmail());
				Library lib = new Library();
				lib.setUserId(reqUserId);
				writeToOutput(lib.toMsg(), resp);
			} else {
				log.info("Returning library for " + reqUser.getEmail() + " to " + authUser.getEmail());
				Date since = null;
				if (req.getParameter("since") != null)
					since = new Date(Long.parseLong(req.getParameter("since")));
				Library lib = midas.getLibrary(reqUser, since);
				if (lib == null)
					lib = new MidasLibrary();
				writeToOutput(lib.toMsg(), resp);
			}
		} else {
			send401(req, resp);
			return;
		}
	}

	/**
	 * To add to the library: /library/<user-id>/add
	 * 
	 * To delete from the library: /library/<user-id>/del
	 * 
	 * Expects a serialized LibraryMsg in the request body with the streams being added/deleted
	 * 
	 * NB we do both these via PUT rather than using PUT & DELETE as tomcat doesn't pass through a request body in the
	 * DELETE
	 */
	@RequestMapping(value="/library/{userIdStr}/{verb}", method=RequestMethod.PUT)
	public void addOrRemoveFromLibrary(@PathVariable("userIdStr") String userIdStr, @PathVariable("verb") String verb,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser authUser = getAuthUser(req);
		long userId = Long.parseLong(userIdStr, 16);
		if (authUser == null || authUser.getUserId() != userId) {
			send401(req, resp);
			return;
		}
		LibraryMsg.Builder b = LibraryMsg.newBuilder();
		readFromInput(b, req);
		LibraryMsg msg = b.build();
		Library newLib = new MidasLibrary(msg);
		Library currentLib = midas.getLibrary(authUser, null);
		if ("add".equals(verb)) {
			if (currentLib == null) {
				newLib.setUserId(authUser.getUserId());
				midas.putLibrary(newLib);
			} else {
				for (Entry<String, Date> entry : newLib.getTracks().entrySet()) {
					currentLib.getTracks().put(entry.getKey(), entry.getValue());
				}
				midas.putLibrary(currentLib);
			}
			log.info("User " + authUser.getEmail() + " added " + newLib.getTracks().size() + " tracks to their library");
		} else if ("del".equals(verb)) {
			if (currentLib != null) {
				for (String sid : newLib.getTracks().keySet()) {
					currentLib.getTracks().remove(sid);
				}
				midas.putLibrary(currentLib);
			}
			log.info("User " + authUser.getEmail() + " removed " + newLib.getTracks().size()
					+ " tracks from their library");
		} else
			send404(req, resp);

	}
}
