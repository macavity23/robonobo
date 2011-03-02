package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;

public class hai implements ConsoleCommand {

	public void printHelp(PrintWriter out) {
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		out.println("O HAI!");
	}
}
