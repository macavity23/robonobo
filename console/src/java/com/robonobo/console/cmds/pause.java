package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;

public class pause implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'pause' pauses playback");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		console.getController().pause();
	}
}
