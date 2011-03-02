package com.robonobo.console.cmds;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import com.robonobo.common.util.TextUtil;
import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.User;

public class watch implements ConsoleCommand {

	public void printHelp(PrintWriter out) {
		out.println("'watch' shows current watched dirs\n'"
				+ "watch add <path>' adds the supplied dir to the list to watch for new files\n'"
				+ "watch del <path>' removes dir from the watch list\n");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RobonoboController controller = console.getController();
		if(args.length == 0) {
			List<File> watchDirs = controller.getWatchDirs();
			out.println(TextUtil.numItems(watchDirs, "dir"));
			for (File dir : watchDirs) {
				out.println(dir.getAbsolutePath());
			}
		} else if(args.length == 1)
			printHelp(out);
		else {
			String subCmd = args[0];
			File dir = new File(args[1]);
			if(subCmd.equals("add")) {
				User user = controller.getMyUser();
				if (user == null) {
					out.println("You must be logged in (type 'help login')");
					return;
				}
				if(!dir.exists()) {
					out.println("Directory '"+dir.getAbsolutePath()+"' does not exist");
					return;
				}
				if(!dir.isDirectory()) {
					out.println(dir.getAbsolutePath()+" is not a directory");
					return;
				}
				controller.addWatchDir(dir);
				out.println("Directory '"+dir.getAbsolutePath()+"' added to watch list");
			} else if(subCmd.equals("del")) {
				controller.deleteWatchDir(dir);
				out.println("Entry '"+dir.getAbsolutePath()+"' deleted");
			} else
				printHelp(out);
		}
	}

}
