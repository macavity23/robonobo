package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.User;

public class account implements ConsoleCommand {
	public static final char WANG_CHAR = 0x65fa;
	
	public void printHelp(PrintWriter out) {
		out.println("'account' shows details of the logged in account");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RobonoboController control = console.getController();
		User u = control.getMyUser();
		if(u == null) {
			out.println("You must be logged in (type 'help login')");
			return;
		}
		out.println("Logged in as "+u.getFriendlyName()+" ("+u.getEmail()+")");
		double bankBal= control.getBankBalance();
		double onHandBal = control.getOnHandBalance();
		out.println("Balance: "+WANG_CHAR+bankBal+" in bank, "+WANG_CHAR+onHandBal+" in coins");
	}

}
