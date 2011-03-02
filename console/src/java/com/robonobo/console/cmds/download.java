package com.robonobo.console.cmds;

import static com.robonobo.common.util.FileUtil.*;
import static com.robonobo.common.util.TextUtil.*;

import java.io.PrintWriter;
import java.util.List;

import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.DownloadingTrack;
import com.robonobo.core.api.model.Track;
import com.robonobo.core.api.model.User;
import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;

public class download implements ConsoleCommand {
	public void printHelp(PrintWriter out) {
		out.println("'download' gives current downloads\n" + "'download add <streamId>' adds download\n"
				+ "'download del <stream id>' removes an download\n" + "'download start <stream id>' starts download\n"
				+ "'download pause <stream id>' pauses download\n");
	}

	public void run(RobonoboConsole console, String[] args, PrintWriter out) throws Exception {
		RobonoboController controller = console.getController();
		if (args.length == 0) {
			if (!controller.isNetworkRunning()) {
				out.println("[Network stopped]");
				return;
			}
			List<String> downloadStreamIds = controller.getDownloads();
			if (downloadStreamIds.size() == 0) {
				out.println("No downloads");
				return;
			}
			int iWidth = numDigits(downloadStreamIds.size()) + 1;
			out.println(rightPad("#", iWidth) + rightPad(" Title", 33) + rightPad(" File", 26)
					+ rightPad(" Status", 12) + rightPad(" %", 8) + rightPad(" Up", 12) + rightPad(" Down", 12)
					+ rightPad("Id", 40));
			int i = 1;
			for (String streamId : downloadStreamIds) {
				Track t = controller.getTrack(streamId);
				if (t instanceof DownloadingTrack) {
					DownloadingTrack dl = (DownloadingTrack) t;
					double pcnt = 100.0 * (dl.getBytesDownloaded()) / (dl.getStream().getSize());
					String completeStr = padToMinWidth(pcnt, 3) + "%";
					String statusStr;
					if (dl.getDownloadStatus() == DownloadStatus.Downloading)
						statusStr = "Downloading";
					else if (dl.getDownloadStatus() == DownloadStatus.Finished) {
						statusStr = "Finished";
					} else
						statusStr = "Paused";
					String filePath = (dl.getFile() == null) ? "<default>" : dl.getFile().getAbsolutePath();
					out.println(rightPad(String.valueOf(i++), iWidth) + " "
							+ rightPadOrTruncate(dl.getStream().getTitle(), 32) + " "
							+ rightPadOrTruncate(filePath, 25) + " " + rightPadOrTruncate(statusStr, 11) + " "
							+ rightPadOrTruncate(completeStr, 7) + " "
							+ rightPadOrTruncate(humanReadableSize(dl.getUploadRate()) + "/s", 11) + " "
							+ rightPadOrTruncate(humanReadableSize(dl.getDownloadRate()) + "/s", 11)
							+ rightPad(dl.getStream().getStreamId(), 40));
				}
			}
		} else {
			if (args[0].equalsIgnoreCase("add")) {
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
				controller.addDownload(streamId);
				out.println("Download added for "+streamId);
			} else if (args[0].equalsIgnoreCase("del")) {
				if (args.length < 2) {
					printHelp(out);
					return;
				}
				String streamId = args[1];
				controller.deleteDownload(streamId);
			} else if (args[0].equalsIgnoreCase("start")) {
				if (args.length < 2) {
					printHelp(out);
					return;
				}
				String streamId = args[1];
				controller.startDownload(streamId);
			} else if (args[0].equalsIgnoreCase("pause")) {
				if (args.length < 2) {
					printHelp(out);
					return;
				}
				String streamId = args[1];
				controller.pauseDownload(streamId);
			} else {
				out.print("Error: ");
				printHelp(out);
			}
		}
	}
}
