package com.robonobo.plugin.mplayer;

import static com.robonobo.common.util.ByteUtil.*;
import static java.lang.Math.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.io.LinedInputStreamHandler;
import com.robonobo.common.pageio.buffer.PageBufferInputStream;
import com.robonobo.core.api.AudioPlayer;
import com.robonobo.core.api.AudioPlayerListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.buffer.PageBuffer;

/**
 * Plays audio through mplayer by creating an external mplayer process in slave mode For slave mode reference, see
 * ftp://ftp2.mplayerhq.hu/MPlayer/DOCS/tech/slave.txt
 * 
 * @author macavity
 * 
 */
public class MplayerAudioPlayer implements AudioPlayer {
	private static final String MPLAYER_ARGS = "-slave -quiet -identify -hr-mp3-seek -cache-min 10";

	ScheduledThreadPoolExecutor executor;
	Stream s;
	PageBuffer pb;
	File mplayerExe;
	MplayerServer server;
	MplayerHandler handler;
	Status status;
	List<AudioPlayerListener> listeners = new ArrayList<AudioPlayerListener>();
	Log log = LogFactory.getLog(getClass());
	int serverListenPort;
	/**
	 * mplayer guesses mp3 lengths badly, and we have to compensate for this for seeking. If this is <1, mplayer thinks
	 * the file is shorter than it really is
	 */
	float mplayerStupidityRatio = 1f;

	public MplayerAudioPlayer(ScheduledThreadPoolExecutor executor, Stream s, PageBuffer pb, File mplayerExe)
			throws IOException {
		this.executor = executor;
		this.s = s;
		this.pb = pb;
		this.mplayerExe = mplayerExe;
		if (!mplayerExe.canExecute())
			throw new IOException("mplayer exe " + mplayerExe.getAbsolutePath()
					+ " does not exist or is not executable");
	}

	@Override
	public void play() throws IOException {
		if (handler == null) {
			server = new MplayerServer();
			handler = new MplayerHandler();
		} else {
			if (status != Status.Paused)
				throw new SeekInnerCalmException();
			handler.resume();
		}
		status = Status.Playing;
	}

	@Override
	public void pause() throws IOException {
		if (status == Status.Paused)
			return;
		if (handler != null)
			handler.pause();
		status = Status.Paused;
	}

	@Override
	public void stop() {
		if (handler != null) {
			handler.die();
			server.die();
			handler = null;
		}
		status = Status.Stopped;
	}

	@Override
	public void seek(long ms) throws IOException {
		if (handler != null)
			handler.seek(ms);
	}

	public void addListener(AudioPlayerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(AudioPlayerListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	private String getMplayerUrl() {
		return "http://localhost:" + serverListenPort + "/" + s.getStreamId();
	}

	/**
	 * Listens on a localhost port and does pretend http for mplayer to talk to
	 */
	class MplayerServer extends CatchingRunnable {
		Thread t;
		PageBufferInputStream pbis;
		private ServerSocket serverSock;

		public MplayerServer() throws IOException {
			serverSock = new ServerSocket();
			serverSock.bind(null);
			serverListenPort = serverSock.getLocalPort();
			pbis = new PageBufferInputStream(pb);
			t = new Thread(this);
			t.start();
			log.debug("MplayerServer listening on local port " + serverListenPort);
		}

		@Override
		public void doRun() throws Exception {
			Socket sock = serverSock.accept();
			log.debug("Mplayer server received connection, sending data");
			try {
				OutputStream out = sock.getOutputStream();
				out.write("HTTP/1.1 200 OK\n".getBytes());
				out.write("Content-Length: ".getBytes());
				out.write(String.valueOf(s.getSize()).getBytes());
				out.write("\nContent-Type: audio/mpeg\n\n".getBytes());
				out.flush();
				streamDump(pbis, out);
				sock.close();
				log.debug("Mplayer server sent all data");
			} catch (IOException e) {
				log.info("Mplayer server exiting after catching IOException: " + e.getMessage());
			}
		}

		void die() {
			t.interrupt();
			try {
				serverSock.close();
			} catch (IOException ignore) {
			}
		}
	}

	class MplayerHandler {
		Thread stdoutRdr, stderrRdr;
		PrintWriter stdinWriter;
		Process mplayerProc;
		boolean waitingForPlayback = true;
		Future<?> getPosTask;

		public MplayerHandler() throws IOException {
			mplayerProc = Runtime.getRuntime().exec(
					mplayerExe.getAbsolutePath() + " " + MPLAYER_ARGS + " " + getMplayerUrl());
			stdoutRdr = new Thread(new StdOutHandler(mplayerProc.getInputStream()));
			stdoutRdr.start();
			stderrRdr = new Thread(new StdErrHandler(mplayerProc.getErrorStream()));
			stderrRdr.start();
			stdinWriter = new PrintWriter(mplayerProc.getOutputStream());
			getPosTask = executor.scheduleAtFixedRate(new CatchingRunnable() {
				public void doRun() throws Exception {
					if (status == Status.Playing) {
						stdinWriter.write("pausing_keep get_property time_pos\n");
						stdinWriter.flush();
					}
				}
			}, 500, 500, TimeUnit.MILLISECONDS);
		}

		private void pause() {
			stdinWriter.write("pause\n");
			stdinWriter.flush();
		}

		/**
		 * This assumes we're keeping track somewhere else of whether we're paused/playing
		 */
		private void resume() {
			stdinWriter.write("pause\n");
			stdinWriter.flush();
		}

		private void seek(long ms) {
//			int secs = round(ms / 1000 * mplayerStupidityRatio);
//			int secs = round(ms / 1000f);
//			log.debug("Seeking to: " + secs);
//			stdinWriter.write("seek " + secs + " 2\n");

						int pcnt = (int) (100 * ms / s.getDuration());
			log.debug("Seeking to: "+pcnt+"%");
			stdinWriter.write("seek " + pcnt + " 1\n");
			stdinWriter.flush();
		}

		private void die() {
			stdinWriter.write("quit\n");
			stdinWriter.flush();
			stdoutRdr.interrupt();
			stderrRdr.interrupt();
			getPosTask.cancel(true);
			stdinWriter.close();
			mplayerProc.destroy();
		}

		class StdErrHandler extends LinedInputStreamHandler {
			public StdErrHandler(InputStream is) {
				super(is);
			}

			@Override
			public void handleLine(String line) {
				log.debug("Mplayer stderr output: " + line);
			}

			@Override
			protected void handleException(IOException e) {
				log.debug("Mplayer stderr handler caught IOException: " + e.getMessage());
			}
		}

		class StdOutHandler extends LinedInputStreamHandler {
			Pattern streamLengthPattern = Pattern.compile("^ID_LENGTH=([0-9\\.]+)$");
			Pattern playbackStartPattern = Pattern.compile("^Starting playback...$");
			Pattern propValPattern = Pattern.compile("^(\\S+)=(\\S+)$");
			Pattern endPattern = Pattern.compile("^Exiting... \\((.*)\\)$");

			public StdOutHandler(InputStream is) {
				super(is);
			}

			@Override
			public void handleLine(String line) {
				log.info("Got stdout from mplayer: " + line);
				if (waitingForPlayback) {
					Matcher m = streamLengthPattern.matcher(line);
					if (m.find()) {
						float lengthAccordingToMplayer = Float.parseFloat(m.group(1)) * 1000;
						long realLength = s.getDuration();
						mplayerStupidityRatio = lengthAccordingToMplayer / realLength;
						log.info("Mplayer thinks stream length is "+lengthAccordingToMplayer+" - discrepency "+mplayerStupidityRatio);
						return;
					}
					if (playbackStartPattern.matcher(line).matches()) {
						waitingForPlayback = false;
						log.debug("MPlayer playback starting...");
					}
					return;
				}
				Matcher m;
				m = endPattern.matcher(line);
				if (m.matches()) {
					// We're done here
					String reason = m.group(1);
					if (reason.equalsIgnoreCase("End of file")) {
						for (AudioPlayerListener listener : listeners) {
							try {
								listener.onCompletion();
							} catch (Exception e) {
								log.error(
										"Caught exception passing end of playback to audioplayerlistener " + listener,
										e);
							}
						}
					} else {
						for (AudioPlayerListener listener : listeners) {
							try {
								listener.onError(reason);
							} catch (Exception e) {
								log.error("Caught exception passing error to audioplayerlistener " + listener, e);
							}
						}
					}
					stop();
					return;
				}
				m = propValPattern.matcher(line);
				if (m.matches())
					readProperty(m.group(1), m.group(2));
				else
					log.debug("Read unexpected line from mplayer stdout: " + line);
			}

			private void readProperty(String prop, String val) {
				if (prop.equalsIgnoreCase("ans_time_pos")) {
					long usPos = (long) (Float.parseFloat(val) * 1000000);
					log.debug("Got mplayer progress: " + usPos + "us");
					for (AudioPlayerListener listener : listeners) {
						try {
							listener.onProgress(usPos);
						} catch (Exception e) {
							log.error("Caught exception passing progress to audioplayerlistener " + listener, e);
						}
					}
				}
			}

			@Override
			protected void handleException(IOException e) {
				log.debug("Mplayer stdout handler caught IOException: " + e.getMessage());
			}
		}
	}
}
