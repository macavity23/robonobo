package com.robonobo.console.cmds;

import static com.robonobo.common.util.FileUtil.*;
import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.TextUtil;
import com.robonobo.common.util.TimeUtil;
import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.RobonoboController;
import com.robonobo.mina.external.ConnectedNode;

public class network implements ConsoleCommand {
	static final char GAMMA = 0x03b3;

	public void printHelp(PrintWriter out) {
		out.println("'network' gives current network status\n" + "'network watch' outputs status every second");
	}

	public void run(RobonoboConsole console, String[] args, final PrintWriter out) throws Exception {
		final RobonoboController controller = console.getController();
		if (controller.isNetworkRunning()) {
			out.println("I am " + controller.getMyNodeId());
			List<String> urls = controller.getMyEndPointUrls();
			out.println("My listening endpoints:");
			for (String url : urls) {
				out.println(url);
			}
			List<ConnectedNode> nodes = controller.getConnectedNodes();
			out.println("\n"+numItems(nodes, "connection")+":");
			if (nodes.size() > 0)
				out.println(rightPad("Id", 36) 
						+ rightPad(" Url", 37) 
						+ rightPad(" Super", 6) 
						+ rightPad(" Their Bid", 10) 
						+ rightPad(" Their " + GAMMA, 8)
						+ rightPad(" Up", 12)
						+ rightPad(" My Bid", 7) 
						+ rightPad(" My " + GAMMA, 6)
						+ rightPad(" Down", 12) 
						);
			out.flush();
			if (args.length == 0) {
				for (ConnectedNode node : nodes) {
					printNodeDetails(out, node);
				}
			} else if (args.length == 1 && args[0].equalsIgnoreCase("watch")) {
				final DateFormat df = new SimpleDateFormat("HH:mm:ss");
				ScheduledFuture<?> task = controller.getExecutor().scheduleAtFixedRate(new CatchingRunnable() {
					public void doRun() throws Exception {
						out.println("["+df.format(now())+"]");
						List<ConnectedNode> nodes = controller.getConnectedNodes();
						for (ConnectedNode node : nodes) {
							printNodeDetails(out, node);
						}
						out.flush();
					}
				}, 0, 1, TimeUnit.SECONDS);
				console.readLine();
				task.cancel(false);
			}
		} else
			out.println("[Network stopped]");
	}

	private void printNodeDetails(PrintWriter out, ConnectedNode node) {
		out.println(rightPadOrTruncate(node.getNodeId(), 36) + " " + rightPadOrTruncate(node.getEndPointUrl(), 36)
				+ " " + rightPadOrTruncate((node.isSupernode()) ? "Yes" : "No", 5) + " "
				+ rightPad(padToMinWidth(node.getTheirBid(), 4), 9) + " "
				+ rightPad(padToMinWidth(node.getTheirGamma(), 4), 8)
				+ rightPadOrTruncate(humanReadableSize(node.getUploadRate()) + "/s", 11) + " "
				+ rightPad(padToMinWidth(node.getMyBid(), 4), 6) + " "
				+ rightPad(padToMinWidth(node.getMyGamma(), 4), 5) + " "
				+ rightPadOrTruncate(humanReadableSize(node.getDownloadRate()) + "/s", 11) + " "
				);
	}
}
