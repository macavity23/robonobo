package com.robonobo.console.cmds;

import java.io.File;
import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;
import com.robonobo.robotest.RoboTest;

public class robotest implements ConsoleCommand {
	@Override
	public void printHelp(PrintWriter out) {
		out.println("'robotest start <testfile> <myindex>' starts a robotest with the supplied test file and node index\n"+
				"'robotest' shows the current status of the robotest");
	}

	@Override
	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RoboTest rt = RoboTest.getInstance();
		if(args.length == 0) {
			if(rt == null)
				out.println("No robotest has been started");
			else	
				rt.printStatus(out);
			return;
		} 
		if(args.length != 3) {
			printHelp(out);
			return;
		}
		if(!args[0].equalsIgnoreCase("start")) {
			printHelp(out);
			return;
		}
		if(rt != null && !rt.finished()) {
			out.println("A robotest is already running, not starting a new one");
			return;
		}
		File testFile = new File(args[1]);
		if(!testFile.exists()) {
			out.println("File '"+testFile.getAbsolutePath()+"' does not exist");
			return;
		}
		int myIndex = Integer.parseInt(args[2]);
		rt = new RoboTest(console.getController(), testFile, myIndex);
		rt.start();
	}


}
