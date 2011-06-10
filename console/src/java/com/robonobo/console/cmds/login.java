package com.robonobo.console.cmds;

import java.io.PrintWriter;

import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.LoginListener;
import com.robonobo.core.api.model.User;

public class login implements ConsoleCommand, LoginListener {
	PrintWriter out;

	public void printHelp(PrintWriter out) {
		out.println("'login <email> <password>' logs into robonobo - visit http://robonobo.com for an account");
	}

	@Override
	public void loginSucceeded(User me) {
		out.println("Login successful");
	}

	@Override
	public void loginFailed(String reason) {
		out.println("Login failed");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if (args.length < 2) {
			printHelp(out);
			return;
		}
		this.out = out;
		RobonoboController control = console.getController();
		control.addLoginListener(this);
		String email = args[0];
		String pwd = args[1];
		out.println("Attempting login as " + email + ", please hold...");
		control.login(email, pwd);
	}
}
