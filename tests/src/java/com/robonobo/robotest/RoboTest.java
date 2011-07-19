package com.robonobo.robotest;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.MinaConfig;
import com.robonobo.mina.external.NodeFilter;
import com.robonobo.mina.external.node.EonEndPoint;

public class RoboTest {
	static int SECS_BETWEEN_DOWNLOADS = 60;
	static Pattern testFilePat = Pattern.compile("^(\\S+)\\s+(.+)$");
	private static RoboTest instance;
	private RobonoboController control;
	private List<String> streamIds = new ArrayList<String>();
	private List<File> inFiles = new ArrayList<File>();
	private int myIndex;
	private boolean started = false;
	private ScheduledFuture<?> addDownloadsTask;
	private Log log = LogFactory.getLog(getClass());

	public static RoboTest getInstance() {
		return instance;
	}

	public RoboTest(RobonoboController control, File testFile, int myIndex) throws IOException {
		instance = this;
		this.myIndex = myIndex;
		this.control = control;
		// We expect a file containing a series of lines each with a stream id followed by a space followed by a file
		// path
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
		String line;
		while ((line = in.readLine()) != null) {
			Matcher m = testFilePat.matcher(line.replaceAll("\\s+$", ""));
			if (!m.matches())
				throw new Errot("Format of file must be <streamid> <filepath>");
			String sid = m.group(1);
			String filePath = m.group(2);
			File f = new File(filePath);
			if (!f.exists())
				throw new Errot("File '" + f.getAbsolutePath()
						+ "' specified in test file does not exist");
			streamIds.add(sid);
			inFiles.add(f);
		}
	}

	public void printStatus(PrintWriter out) {
		if (!started) {
			out.println("Robotest not yet started");
			return;
		}
		if (finished()) {
			out.println("Robotest completed successfully");
			return;
		}
		Set<String> shareSids = control.getShareStreamIds();
		int numShares = 0;
		for (String sid : streamIds) {
			if (shareSids.contains(sid))
				numShares++;
		}
		out.println("Robotest running, completed " + numShares + "/" + streamIds.size() + " streams");
	}

	public void start() throws IOException, RobonoboException {
		log.debug("Robotest starting");
		// Tell mina to ignore nodes on the same ip as us
		MinaConfig mc = (MinaConfig) control.getConfig("mina");
		InetAddress myIp = InetAddress.getByName(mc.getLocalAddress());
		control.addNodeFilter(new RejectSameIPFilter(myIp));
		// We initially share the stream specified by myIndex
		String sharePath = inFiles.get(myIndex).getAbsolutePath();
		log.debug("Robotest adding share for " + sharePath);
		control.addShare(sharePath);
		// Then we run a regular pFetcher that adds a download for every other stream - shuffle the order
		List<String> sidsToDownload = new ArrayList<String>(streamIds);
		sidsToDownload.remove(myIndex);
		Collections.shuffle(sidsToDownload);
		addDownloadsTask = control.getExecutor().scheduleAtFixedRate(new AddDownloadsTask(sidsToDownload), 0, SECS_BETWEEN_DOWNLOADS, TimeUnit.SECONDS);
	}

	public boolean finished() {
		Set<String> shareSids = control.getShareStreamIds();
		for (String sid : streamIds) {
			if (!shareSids.contains(sid))
				return false;
		}
		return true;
	}

	class AddDownloadsTask extends CatchingRunnable {
		private List<String> sidsToDownload;
		
		public AddDownloadsTask(List<String> sidsToDownload) {
			this.sidsToDownload = sidsToDownload;
		}

		@Override
		public void doRun() throws Exception {
			if(sidsToDownload.size() == 0) {
				addDownloadsTask.cancel(false);
				return;
			}
			String sid = sidsToDownload.remove(0);
			log.debug("RoboTest adding download for sid "+sid);
			control.addDownload(sid);
		}
	}

	class RejectSameIPFilter implements NodeFilter {
		private InetAddress myIp;

		public RejectSameIPFilter(InetAddress myIp) {
			this.myIp = myIp;
		}

		@Override
		public String getFilterName() {
			return "SameIPFilter";
		}

		@Override
		public boolean acceptNode(Node node) {
			for (EndPoint ep : node.getEndPointList()) {
				EonEndPoint eep = EonEndPoint.parse(ep.getUrl());
				if (eep.getAddress().equals(myIp))
					return false;
			}
			return true;
		}
	}
}
