package com.robonobo.console.cmds;

import static com.robonobo.common.util.TextUtil.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.console.RobonoboConsole;

public class dbupdate implements ConsoleCommand {

	public void printHelp(PrintWriter out) {
		out.println("'dbupdate [meta|page] \"<query>\"' runs the supplied update query against the local db");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		if (args.length <2) {
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
			Statement st = conn.createStatement();
			int rowCount = st.executeUpdate(query);
			st.close();
			out.println(numItems(rowCount, "row")+" updated");
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
