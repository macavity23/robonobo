package com.robonobo.console.cmds;

import java.io.PrintWriter;
import java.sql.Connection;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.DbUtil;
import com.robonobo.console.RobonoboConsole;

public class dbquery implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'dbquery [meta|page] \"<query>\"' runs the supplied query against the local db and prints the results");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if (args.length < 2) {
			printHelp(out);
			return;
		}
		Connection conn;
		if(args[0].equalsIgnoreCase("meta"))
			conn = console.getController().getMetadataDbConnection();
		else if(args[0].equalsIgnoreCase("page"))
			conn = console.getController().getPageDbConnection();
		else {
			printHelp(out);
			return;
		}
		String query = args[1];
		try {
			DbUtil.runQuery(conn, query, out);
		} finally {
			if(args[0].equalsIgnoreCase("meta"))
				console.getController().returnMetadataDbConnection(conn);
			else if(args[0].equalsIgnoreCase("page"))
				console.getController().returnPageDbConnection(conn);
			else
				throw new SeekInnerCalmException();
		}
	}
}
