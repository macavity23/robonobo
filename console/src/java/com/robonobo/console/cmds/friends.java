package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.User;

public class friends implements ConsoleCommand {

	public void printHelp(PrintWriter out) {
		out.println("'friends' lists all friends");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RobonoboController control = console.getController();
		if(control.getMyUser() == null) {
			out.println("You must be logged in (type 'help login')");
			return;
		}
		for (long friendId : control.getMyUser().getFriendIds()) {
			User u = control.getKnownUser(friendId);
			out.println(u.getEmail()+" ("+u.getFriendlyName()+")");
		}
	}

}
