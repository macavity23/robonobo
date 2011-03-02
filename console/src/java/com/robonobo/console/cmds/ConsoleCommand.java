package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;

public interface ConsoleCommand {
	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception;
	public void printHelp(PrintWriter out);
}
