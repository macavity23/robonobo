package com.robonobo.midas.controller;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.robonobo.core.api.model.User;
import com.robonobo.core.api.proto.CoreApi.SearchResponse;
import com.robonobo.midas.search.SearchService;
import com.robonobo.midas.search.SearchServiceImpl;

@Controller
public class SearchController extends BaseController {
	@Autowired
	SearchService searchService;

	@RequestMapping("/search")
	public void runSearch(@RequestParam("type") String searchType, @RequestParam("q") String query,
			@RequestParam(value = "first", required = false) Integer firstResult, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		User u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		int fr = (firstResult == null) ? 0 : firstResult;
		SearchResponse response = searchService.search(searchType, query, fr);
		writeToOutput(response, resp);
		log.info("Returning " + response.getObjectIdCount() + " results to " + u.getEmail() + " for search '"
				+ query + "'");
	}
}
