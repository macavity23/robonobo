package com.robonobo.console.cmds;

import static com.robonobo.common.util.TextUtil.*;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.robonobo.common.exceptions.Errot;
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
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
			// Retrieve the whole set as strings so we can work out display
			// widths
			// Sql uses 1-based indexing, v annoying
			int[] maxWidths = new int[md.getColumnCount() + 1];
			List<String[]> vals = new ArrayList<String[]>();
			while (rs.next()) {
				String[] rowVals = new String[md.getColumnCount() + 1];
				for (int i = 1; i < rowVals.length; i++) {
					Object val = rs.getObject(i);
					String valStr = String.valueOf(val);
					if (valStr.length() > maxWidths[i])
						maxWidths[i] = valStr.length();
					rowVals[i] = valStr;
				}
				vals.add(rowVals);
			}

			int totalWidth = 0;
			for (int i = 1; i <= md.getColumnCount(); i++) {
				String colName = md.getColumnName(i);
				if(maxWidths[i] < colName.length())
					maxWidths[i] = colName.length();
				int colWidth = maxWidths[i] + 1;
				out.print(rightPad(colName, colWidth));
				totalWidth += colWidth;
			}
			out.println();
			out.println(repeat("=", totalWidth));
			int numRows = 0;

			for(String[] rowVals : vals) {
				for (int i = 1; i < rowVals.length; i++) {
					out.print(rightPad(rowVals[i], maxWidths[i] + 1));
				}
				out.println();
				numRows++;
			}
			out.println(numItems(numRows, "row"));
			st.close();
		} catch(SQLException e) {
			out.println("SQL error: "+e.getMessage());
		} finally {
			if(args[0].equalsIgnoreCase("meta"))
				console.getController().returnMetadataDbConnection(conn);
			else if(args[0].equalsIgnoreCase("page"))
				console.getController().returnPageDbConnection(conn);
			else
				throw new Errot();
		}
	}
}
