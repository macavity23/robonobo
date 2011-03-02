package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.common.serialization.UnauthorizedException;
import com.robonobo.console.RobonoboConsole;

public class login implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'login <email> <password>' logs into robonobo - visit http://robonobo.com for an account");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if (args.length < 2) {
			printHelp(out);
			return;
		}
		try {
			console.getController().login(args[0], args[1]);
		} catch (UnauthorizedException e) {
			out.println("Login as '" + args[0] + "' FAILED");
		} 
	}
}
