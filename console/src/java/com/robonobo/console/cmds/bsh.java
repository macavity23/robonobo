package com.robonobo.console.cmds;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.logging.LogFactory;

import bsh.Interpreter;

import com.robonobo.console.RobonoboConsole;

public class bsh implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'bsh <scriptfile> [<args>]' runs the beanshell script file <scriptfile>.  The script will have the following variables set: 'control' (RobonoboController), 'out' (PrintWriter), 'args' (String[]), 'log' (Log).");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if (args.length == 0) {
			printHelp(out);
			return;
		}
		File scriptFile = new File(args[0]);
		if (!scriptFile.exists() || !scriptFile.canRead()) {
			out.println("Script file '" + scriptFile + "' does not exist or is not readable");
			return;
		}
		String[] scriptArgs = new String[args.length - 1];
		for (int i = 0; i < scriptArgs.length; i++) {
			scriptArgs[i] = args[i + 1];
		}
		Interpreter i = new Interpreter();
		i.set("control", console.getController());
		i.set("out", out);
		i.set("args", scriptArgs);
		i.set("log", LogFactory.getLog(getClass()));
		i.source(scriptFile.getAbsolutePath());
	}
}
