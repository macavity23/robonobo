package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;

public class play implements ConsoleCommand {

	public void printHelp(PrintWriter out) {
		out.println("'play <streamId>' plays download/share\n'play' plays previously paused playback");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if (args.length > 0) {
			String streamId = args[0];
			console.getController().play(streamId);
		} else
			console.getController().play(null);
	}

}
