package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;

public class quit implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'quit' quits the console");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		console.getController().shutdown();
		System.exit(0);
	}
}
