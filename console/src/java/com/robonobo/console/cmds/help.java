package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;

public class help implements ConsoleCommand {
	private static String[] cmdNames = {"help", "network", "friends", "playlist", "search", "download", "login", "account", "share", "watch", "play", "pause", "quit", "bsh"};
	public void printHelp(PrintWriter out) {
		out.print("Available commands: ");
		for(int i = 0; i < cmdNames.length; i++) {
			out.print(cmdNames[i]);
			out.print(" ");
		}
		out.println("\nType 'help <command>' to get help on that command");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if(args.length == 0) {
			printHelp(out);
		} else {
			String className = "com.robonobo.console.cmds."+args[0];
			try {
				ConsoleCommand cmd = (ConsoleCommand) Class.forName(className).newInstance();
				cmd.printHelp(out);
			} catch(ClassNotFoundException e) {
				out.println("Unknown command '"+args[0]+"'");
				return;
			}
		}
	}
}
