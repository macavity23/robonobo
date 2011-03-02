package com.robonobo.console.cmds;

import static com.robonobo.common.util.TextUtil.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.FileUtil;
import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.SharedTrack;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;
import com.robonobo.core.api.model.User;
import com.robonobo.core.api.model.SharedTrack.ShareStatus;

public class share implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'share add <pathToFile>' adds share\n" + "'share del <streamid>' removes share\n"
				+ "'share all' gives all shares\n"
				+ "'share <something>' gives current shares that match <something>\n");
	}

	public void run(RobonoboConsole console, String[] args, final PrintWriter out) throws Exception {
		RobonoboController controller = console.getController();
		if (args.length < 1) {
			printHelp(out);
			return;
		}
		if (args[0].equalsIgnoreCase("all")) {
			Set<String> shareStreamIds = controller.getShares();
			List<SharedTrack> shares = new ArrayList<SharedTrack>();
			for (String streamId : shareStreamIds) {
				Track t = controller.getTrack(streamId);
				if(t instanceof SharedTrack)
					shares.add((SharedTrack) t);
			}
			printShares(out, shares);
		} else if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 1) {
				printHelp(out);
				return;
			}
			User user = controller.getMyUser();
			if (user == null) {
				out.println("You must be logged in (type 'help login')");
				return;
			}
			File file = new File(args[1]);
			if (!file.exists()) {
				out.println(file.getAbsolutePath() + " does not exist");
				return;
			}
			if (!file.canRead()) {
				out.println(file.getAbsolutePath() + " is not readable");
				return;
			}
			if (file.isDirectory()) {
				out.println("Adding all mp3 files within " + file);
				List<File> files = FileUtil.getFilesWithinPath(file, "mp3");
				for (File f : files) {
					out.println("Adding " + f.getAbsolutePath());
					try {
						Stream s = controller.addShare(f.getAbsolutePath());
						out.println("Share '" + s.getTitle() + "' (" + f.getAbsolutePath() + ") added");
					} catch (Exception e) {
						out.println("Error adding share " + f.getAbsolutePath() + " - see log for details");
					}
					out.flush();
				}
			} else {
				Stream s = controller.addShare(file.getAbsolutePath());
				out.println("Share '" + s.getTitle() + "' (" + file.getAbsolutePath() + ") added");
			}
			return;
		} else if (args[0].equalsIgnoreCase("del")) {
			if (args.length < 2) {
				printHelp(out);
				return;
			}
			User user = controller.getMyUser();
			if (user == null) {
				out.println("You must be logged in (type 'help login')");
				return;
			}
			String streamId = args[1];
			controller.deleteShare(streamId);
		} else {
			String searchPattern = args[0];
			printShares(out, controller.getSharesByPattern(searchPattern));
		}
	}

	private void printShares(final PrintWriter out, List<SharedTrack> shares) {
		if (shares.size() == 0) {
			out.println("No shares");
			return;
		}
		int iWidth = numDigits(shares.size()) + 1;
		out.println(rightPad("#", iWidth) + rightPad(" Title", 32) + rightPad(" File", 26)
				+ rightPad(" Status", 12) + rightPad(" Up", 12) + rightPad("Id", 40));
		int i = 1;
		for (SharedTrack share : shares) {
			String statusStr;
			if (share.getShareStatus() == ShareStatus.Sharing)
				statusStr = "Running";
			else if (share.getShareStatus() == ShareStatus.Paused) {
				statusStr = "Paused";
			} else
				throw new SeekInnerCalmException();
			out.println(rightPad(String.valueOf(i++), iWidth)
					+ rightPadOrTruncate(share.getStream().getTitle(), 31) + " "
					+ rightPadOrTruncate(share.getFile().getAbsolutePath(), 25) + " "
					+ rightPadOrTruncate(statusStr, 11) + " "
					+ rightPadOrTruncate(FileUtil.humanReadableSize(share.getUploadRate()) + "/s", 11)
					+ rightPadOrTruncate(share.getStream().getStreamId(), 40));
		}
	}
}
