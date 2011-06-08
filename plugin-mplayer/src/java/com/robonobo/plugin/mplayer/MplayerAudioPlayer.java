package com.robonobo.plugin.mplayer;

import static java.lang.Math.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.io.LinedInputStreamHandler;
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
	private static final String MPLAYER_ARGS = "-slave -quiet -hr-mp3-seek -cache-min 10";

	ScheduledThreadPoolExecutor executor;
	File mplayerExe;
	MplayerHttpServer server;
	MplayerProcHandler handler;
	Status status = Status.Starting;
	List<AudioPlayerListener> listeners = new ArrayList<AudioPlayerListener>();
	Log log = LogFactory.getLog(getClass());
	Stream s;
	PageBuffer pb;
	int serverListenPort;

	// TODO starting up mplayer and shutting it down every time here - need to refactor audioplayer to have
	// persistent instances for better responsiveness
	public MplayerAudioPlayer(ScheduledThreadPoolExecutor executor, Stream s, PageBuffer pb, File mplayerExe)
			throws IOException {
		this.executor = executor;
		this.mplayerExe = mplayerExe;
		if (!mplayerExe.canExecute())
			throw new IOException("mplayer exe " + mplayerExe.getAbsolutePath()
					+ " does not exist or is not executable");
		this.s = s;
		this.pb = pb;
		server = new MplayerHttpServer();
		server.start();
		serverListenPort = server.getPort();
		server.addStream(s, pb);
	}

	@Override
	public void play() throws IOException {
		if (status == Status.Playing)
			return;
		if (status == Status.Paused)
			handler.resume();
		else
			handler = new MplayerProcHandler();
		status = Status.Playing;
	}

	@Override
	public void pause() throws IOException {
		if (status == Status.Paused)
			return;
		handler.pause();
		status = Status.Paused;
	}

	@Override
	public void stop() {
		handler.die();
		server.stop();
		status = Status.Stopped;
	}

	@Override
	public void seek(long ms) throws IOException {
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

	private String getMplayerUrl(Stream s) {
		return "http://localhost:" + serverListenPort + "/" + s.getStreamId() + ".mp3";
	}

	class MplayerProcHandler {
		Thread stdoutRdr, stderrRdr;
		PrintWriter stdinWriter;
		Process mplayerProc;
		boolean waitingForPlayback = true;
		Future<?> getPropsTask;

		public MplayerProcHandler() throws IOException {
			String cmdLine = mplayerExe.getAbsolutePath() + " " + MPLAYER_ARGS + " " + getMplayerUrl(s);
			log.debug("DEBUG: run mplayer with cmdline: "+cmdLine);
//			mplayerProc = Runtime.getRuntime().exec(cmdLine);
//			stdoutRdr = new Thread(new StdOutHandler(mplayerProc.getInputStream()));
//			stdoutRdr.start();
//			stderrRdr = new Thread(new StdErrHandler(mplayerProc.getErrorStream()));
//			stderrRdr.start();
//			stdinWriter = new PrintWriter(mplayerProc.getOutputStream());
//			getPropsTask = executor.scheduleAtFixedRate(new CatchingRunnable() {
//				public void doRun() throws Exception {
//					if (status == Status.Playing) {
//						stdinWriter.write("pausing_keep get_property time_pos\n");
//						// stdinWriter.write("pausing_keep get_property path\n");
//						stdinWriter.flush();
//					}
//				}
//			}, 500, 200, TimeUnit.MILLISECONDS);
		}

		private void pause() {
			stdinWriter.write("pause\n");
			stdinWriter.flush();
		}

		// private void play() {
		// // TODO If we pause then playback a different stream, and we get snippets of the previous stream, see
		// // mplayer slave mode doc for how to workaround
		// stdinWriter.write("loadfile " + getMplayerUrl(s) + "\n");
		// stdinWriter.flush();
		// }

		private void resume() {
			stdinWriter.write("pause\n");
			stdinWriter.flush();
		}

		private void seek(long ms) {
			int secs = round(ms / 1000f);
			log.debug("Seeking to: " + secs);
			stdinWriter.write("seek " + secs + " 2\n");
		}

		private void die() {
			stdinWriter.write("quit\n");
			stdinWriter.flush();
			stdoutRdr.interrupt();
			stderrRdr.interrupt();
			getPropsTask.cancel(true);
			stdinWriter.close();
			mplayerProc.destroy();
		}

		class StdErrHandler extends LinedInputStreamHandler {
			final String[] ignoreLines = { "nop_streaming_read error : Bad file descriptor" };

			public StdErrHandler(InputStream is) {
				super(is);
			}

			@Override
			public void handleLine(String line) {
				for (String ignoreLine : ignoreLines) {
					if (line.trim().equalsIgnoreCase(ignoreLine))
						return;
				}
				log.debug("Mplayer stderr output: " + line);
			}

			@Override
			protected void handleException(IOException e) {
				log.debug("Mplayer stderr handler caught IOException: " + e.getMessage());
			}
		}

		class StdOutHandler extends LinedInputStreamHandler {
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
