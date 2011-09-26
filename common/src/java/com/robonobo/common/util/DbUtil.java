package com.robonobo.common.util;

import static com.robonobo.common.util.TextUtil.*;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbUtil {
	/** Runs the sql query against the db connection, and prints the results */
	public static void runQuery(Connection conn, String query, PrintWriter out) {
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
			// Retrieve the whole set as strings so we can work out display widths
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
				if (maxWidths[i] < colName.length())
					maxWidths[i] = colName.length();
				int colWidth = maxWidths[i] + 1;
				out.print(rightPad(colName, colWidth));
				totalWidth += colWidth;
			}
			out.println();
			out.println(repeat("=", totalWidth));
			int numRows = 0;
			for (String[] rowVals : vals) {
				for (int i = 1; i < rowVals.length; i++) {
					out.print(rightPad(rowVals[i], maxWidths[i] + 1));
				}
				out.println();
				numRows++;
			}
			out.println(numItems(numRows, "row"));
			st.close();
		} catch (SQLException e) {
			out.println("SQL error: " + e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter out = new PrintWriter(System.out);
		if (args.length < 4) {
			System.err.println("Usage: DbUtil <driverClass> <url> <username> <password>");
			System.exit(1);
		}
		String className = args[0];
		String dbUrl = args[1];
		String username = args[2];
		String pwd = args[3];
		Class.forName(className);
		Connection conn = DriverManager.getConnection(dbUrl, username, pwd);
		while (true) {
			out.print("db > ");
			out.flush();
			String cmdLine = in.readLine();
			String[] toks = TextUtil.getQuotedArgs(cmdLine);
			if (toks.length == 0)
				continue;
			if (toks[0].equalsIgnoreCase("quit"))
				System.exit(0);
			runQuery(conn, cmdLine, out);
		}
	}
}
