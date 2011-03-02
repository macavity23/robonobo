package com.robonobo.console.cmds;

import static com.robonobo.common.util.TextUtil.formatDurationHMS;
import static com.robonobo.common.util.TextUtil.numItems;
import static com.robonobo.common.util.TextUtil.rightPadOrTruncate;
import static com.robonobo.common.util.TextUtil.urlEncode;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.robonobo.common.util.FileUtil;
import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.SearchListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.User;
import com.robonobo.mina.external.FoundSourceListener;

public class search implements ConsoleCommand {
	static Map<String, SortedSet<String>> results = new HashMap<String, SortedSet<String>>();
	static Map<String, Stream> streams = new HashMap<String, Stream>();
	static Listener listener;
	
	public void printHelp(PrintWriter out) {
		out.println("'search <stuff>' searches for stuff\n"+
				"'search 25 <stuff>' searches for stuff, starting at result 25\n"+
				"'search forget' forgets the current search\n"+
				"'search show' shows the results for the current search");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RobonoboController controller = console.getController();
 		if(args.length == 0) {
			printHelp(out);
			return;
		}
		User user = controller.getMyUser();
		if (user == null) {
			out.println("You must be logged in (type 'help login')");
			return;
		}
		if(args[0].equalsIgnoreCase("show")) {
			out.println(numItems(results.keySet(), "search result"));
			out.println(rightPadOrTruncate("Title", 32) 
					+ rightPadOrTruncate("Artist", 24)
					+ rightPadOrTruncate("Album", 24)
					+ rightPadOrTruncate("Duration", 10)
					+ rightPadOrTruncate("Size", 8)
					+ rightPadOrTruncate("Id", 40)
					+ rightPadOrTruncate("Sources", 8)
					);
			for (String streamId : results.keySet()) {
				Stream s = streams.get(streamId);
				int numSources = results.get(streamId).size();
				out.println(
						rightPadOrTruncate(s.getTitle(), 32)
						+ rightPadOrTruncate(s.getAttrValue("artist"), 24)
						+ rightPadOrTruncate(s.getAttrValue("album"), 24)
						+ rightPadOrTruncate(formatDurationHMS(s.getDuration()), 10)
						+ rightPadOrTruncate(FileUtil.humanReadableSize(s.getSize()), 8)
						+ rightPadOrTruncate(s.getStreamId(), 40)
						+ rightPadOrTruncate(String.valueOf(numSources), 8)
						);
			}
		} else if(args[0].equalsIgnoreCase("forget")) {
			stopSearch(controller);
		} else {
			// See if the first param is a number, if so it's our starting result
			int startResult = 0;
			String query;
			if(args.length > 1) {
				StringBuffer sb = new StringBuffer();
				try {
					startResult = Integer.parseInt(args[0]);
					for(int i=1;i<args.length;i++) {
						sb.append(args[i]);
					}
				} catch(NumberFormatException e) {
					// Oops, not a number - put everything in the search string
					for(int i=0;i<args.length;i++) {
						if(i>0)
							sb.append(" ");
						sb.append(args[i]);
					}
				}
				query = sb.toString();
			} else
				query = args[0];
			startSearch(controller, query, startResult);
			out.println("Search for '"+query+"' started");
		}
	}
	
	private void startSearch(RobonoboController controller, String query, int startResult) {
		String encQuery = urlEncode(query);
		synchronized (results) {
			if(listener != null)
				stopSearch(controller);
			listener = new Listener(controller);
			controller.search(encQuery, startResult, listener);
		}
	}
	
	private void stopSearch(RobonoboController controller) {
		synchronized (results) {
			if(listener == null)
				return;
			for (String streamId : results.keySet()) {
				controller.stopFindingSources(streamId, listener);
			}
			results.clear();
			streams.clear();
			listener = null;
		}
	}
	
	private class Listener implements SearchListener, FoundSourceListener {
		private RobonoboController controller;

		public Listener(RobonoboController controller) {
			this.controller = controller;
		}

		public void gotNumberOfResults(int numResults) {
			// Do nothing
		}
		
		public void foundResult(Stream s) {
			synchronized (results) {
				if(!results.containsKey(s)) {
					results.put(s.getStreamId(), new TreeSet<String>());
					streams.put(s.getStreamId(), s);
					controller.findSources(s.getStreamId(), this);
				}
			}
		}

		public void foundBroadcaster(String streamId, String nodeId) {
			synchronized (results) {
				if(!results.containsKey(streamId))
					return;
				results.get(streamId).add(nodeId);
			}
		}
	}
}
