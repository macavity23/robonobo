package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import static com.robonobo.gui.RoboColor.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.PlaybackListener;
import com.robonobo.core.api.TrackListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.components.PlaybackProgressBar;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PlaybackPanel extends JPanel implements PlaybackListener, TrackListener {
	private static final String PAUSE_TOOLTIP = "Pause playback";
	private static final String PLAY_TOOLTIP = "Play selected tracks";
	private static final String DOWNLOAD_TOOLTIP = "Download selected tracks";
	private static final String NEXT_TOOLTIP = "Next track";
	private static final String PREV_TOOLTIP = "Previous track";
	private static final String LOVE_TOOLTIP = "Love these tracks";
	/** If we're within this time (ms) after the start of a track, calling prev() goes to the previous track (otherwise,
	 * returns to the start of the current one) */
	public static final int PREV_TRACK_GRACE_PERIOD = 5000;

	enum PlayState {
		Stopped, Starting, Playing, Paused
	};

	ImageIcon prevIcon = createImageIcon("/icon/play_back.png", PREV_TOOLTIP);
	ImageIcon nextIcon = createImageIcon("/icon/play_next.png", NEXT_TOOLTIP);
	ImageIcon dloadIcon = createImageIcon("/icon/play_download.png", DOWNLOAD_TOOLTIP);
	ImageIcon playIcon = createImageIcon("/icon/play_play.png", PLAY_TOOLTIP);
	ImageIcon pauseIcon = createImageIcon("/icon/play_pause.png", PAUSE_TOOLTIP);
	ImageIcon loveIcon = createImageIcon("/icon/play_love.png", LOVE_TOOLTIP);
	RobonoboFrame frame;
	RobonoboController ctrl;
	RLabel titleLbl, artistLbl, albumLbl;
	PlaybackProgressBar playbackProgress;
	Stream playingStream = null;
	PlayState state = PlayState.Stopped;
	TrackList playingTrackList;
	String playingContentPanel;
	RButton prevBtn, loveBtn, dloadBtn, playPauseBtn, nextBtn, delBtn;
	boolean checkedNextTrack = false;
	boolean seeking = false;
	Log log = LogFactory.getLog(getClass());
	String prevSid, nextSid;

	public PlaybackPanel(final RobonoboFrame frame) {
		this.frame = frame;
		ctrl = frame.ctrl;
		setName("playback.background.panel");
		double[][] cells = { { 10, TableLayout.FILL, 10, 325, 10 }, { 10, TableLayout.FILL, 10 } };
		setLayout(new TableLayout(cells));
		setBackground(MID_GRAY);
		JPanel titlesPnl = new JPanel();
		titlesPnl.setLayout(new BoxLayout(titlesPnl, BoxLayout.Y_AXIS));
		titlesPnl.setOpaque(false);
		titlesPnl.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Double click on title shows the playing track in the content panel
				if (e.getClickCount() == 2) {
					if (playingStream != null) {
						frame.leftSidebar.selectForContentPanel(playingContentPanel);
						playingTrackList.scrollTableToStream(playingStream.getStreamId());
					}
					e.consume();
				}
			}
		});
		add(titlesPnl, "1,1");
		titleLbl = new RLabel26("");
		titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		titleLbl.setForeground(BLUE_GRAY);
		titlesPnl.add(titleLbl);
		artistLbl = new RLabel20B("");
		artistLbl.setBorder(BorderFactory.createEmptyBorder(1, 10, 0, 0));
		titlesPnl.add(artistLbl);
		albumLbl = new RLabel18("");
		albumLbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));
		titlesPnl.add(albumLbl);
		JPanel controlPnl = new JPanel();
		double[][] controlCells = { { 50, 3, 50, 3, 50, 3, 50, 3, 50, TableLayout.FILL, 40, 3 }, { 3, 24, 5, 50, TableLayout.FILL } };
		controlPnl.setLayout(new TableLayout(controlCells));
		controlPnl.setOpaque(false);
		add(controlPnl, "3,1");
		playbackProgress = new PlaybackProgressBar(frame);
		controlPnl.add(playbackProgress, "0,1,11,1");
		prevBtn = new RRoundButton();
		prevBtn.setIcon(prevIcon);
		prevBtn.setToolTipText(PREV_TOOLTIP);
		prevBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prev();
			}
		});
		controlPnl.add(prevBtn, "0,3");
		loveBtn = new RRoundButton();
		loveBtn.setIcon(loveIcon);
		loveBtn.setToolTipText(LOVE_TOOLTIP);
		loveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TrackList tl = frame.mainPanel.currentContentPanel().trackList;
				if (tl != null) {
					final List<String> selSids = tl.getSelectedStreamIds();
					ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							ctrl.love(selSids);
						}
					});
				}
			}
		});
		controlPnl.add(loveBtn, "2,3");
		dloadBtn = new RRoundButton();
		dloadBtn.setIcon(dloadIcon);
		dloadBtn.setToolTipText(DOWNLOAD_TOOLTIP);
		dloadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TrackList tl = frame.mainPanel.currentContentPanel().trackList;
				if (tl != null) {
					List<String> selSids = tl.getSelectedStreamIds();
					final List<String> dlSids = new ArrayList<String>();
					for (String sid : selSids) {
						Track t = ctrl.getTrack(sid);
						if (t instanceof CloudTrack)
							dlSids.add(sid);
					}
					ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							for (String sid : dlSids)
								ctrl.addDownload(sid);
						}
					});
				}
			}
		});
		controlPnl.add(dloadBtn, "4,3");
		playPauseBtn = new RRoundButton();
		playPauseBtn.setIcon(playIcon);
		playPauseBtn.setToolTipText(PLAY_TOOLTIP);
		playPauseBtn.addActionListener(new PlayPauseListener());
		controlPnl.add(playPauseBtn, "6,3");
		nextBtn = new RRoundButton();
		nextBtn.setIcon(nextIcon);
		nextBtn.setToolTipText(NEXT_TOOLTIP);
		nextBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});
		controlPnl.add(nextBtn, "8,3");
		delBtn = new RSquareDelButton();
		delBtn.setPreferredSize(new Dimension(40, 40));
		delBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TrackList tl = frame.mainPanel.currentContentPanel().trackList;
				if (tl != null)
					tl.deleteSelectedTracks();
			}
		});
		controlPnl.add(delBtn, "10,3,CENTER,CENTER");
		checkActionBtnsEnabled();
		updateNextPrev();
		ctrl.addPlaybackListener(this);
		ctrl.addTrackListener(this);
		// Make sure the component is ready as this requires our dimensions to be setup
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				playbackProgress.lock();
			}
		});
	}

	public void trackSelectionChanged() {
		// checkButtonsEnabled();
		checkActionBtnsEnabled();
		doRepaint();
	}

	public void next() {
		// Grab a copy of the sid to avoid a miniscule chance of it changing while this is called
		String sid = nextSid;
		if (sid != null)
			ctrl.play(sid);
	}

	public void prev() {
		// If we're near the beginning of our track, go back to the previous track, else go back to the start of this
		// one
		if (playbackProgress.getTrackPosition() < PREV_TRACK_GRACE_PERIOD) {
			// Grab a copy of the sid to avoid a miniscule chance of it changing while this is called
			String sid = prevSid;
			if (sid != null)
				ctrl.play(sid);
		} else {
			String sid = playingStream.getStreamId();
			TrackList t = playingTrackList;
			String cp = playingContentPanel;
			ctrl.stopPlayback();
			playingTrackList = t;
			playingContentPanel = cp;
			ctrl.play(sid);
		}
	}

	@Override
	public void playbackStopped() {
		state = PlayState.Stopped;
		synchronized (this) {
			playingStream = null;
			playingTrackList = null;
			playingContentPanel = null;
			titleLbl.setText(null);
			artistLbl.setText(null);
			albumLbl.setText(null);
			playbackProgress.lock();
			prevBtn.setEnabled(false);
			nextBtn.setEnabled(false);
			playPauseBtn.setIcon(playIcon);
			playPauseBtn.setToolTipText(PLAY_TOOLTIP);
		}
		// checkButtonsEnabled();
		checkActionBtnsEnabled();
		// This causes a repaint
		updateNextPrev();
	}

	@Override
	public void playbackStarting() {
		state = PlayState.Starting;
		Stream s = frame.ctrl.currentPlayingStream();
		if (!s.equals(playingStream)) {
			synchronized (this) {
				playingStream = s;
				checkedNextTrack = false;
				titleLbl.setText(s.getTitle());
				artistLbl.setText(s.getAttrValue("artist"));
				albumLbl.setText(s.getAttrValue("album"));
				playbackProgress.setTrackDuration(s.getDuration());
				playbackProgress.setTrackPosition(0);
				updateDataAvailable();
			}
			updateNextPrev();
		}
		playPauseBtn.setIcon(pauseIcon);
		playPauseBtn.setToolTipText(PAUSE_TOOLTIP);
		playbackProgress.starting();
		// checkButtonsEnabled();
		checkActionBtnsEnabled();
		doRepaint();
	}

	@Override
	public void playbackRunning() {
		state = PlayState.Playing;
		playPauseBtn.setIcon(pauseIcon);
		playPauseBtn.setToolTipText(PAUSE_TOOLTIP);
		playbackProgress.play();
		// checkButtonsEnabled();
		checkActionBtnsEnabled();
		doRepaint();
	}

	@Override
	public void playbackProgress(long microsecs) {
		if (playingStream == null)
			return;
		if (seeking)
			return;
		long positionMs = microsecs / 1000;
		playbackProgress.setTrackPosition(positionMs);
		String preFetchStreamId = null;
		synchronized (this) {
			if (!checkedNextTrack && positionMs > (playingStream.getDuration() - ctrl.getConfig().getDownloadCacheTime() * 1000)) {
				// Pre-download next track if necessary
				preFetchStreamId = nextSid;
				if (preFetchStreamId == null)
					log.debug("Not prefetching - no next stream");
				else
					log.debug("Prefetching next stream " + preFetchStreamId);
				checkedNextTrack = true;
			}
		}
		if (preFetchStreamId != null) {
			final String fetchSid = preFetchStreamId;
			ctrl.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					ctrl.preFetch(fetchSid);
				}
			});
		}
	}

	@Override
	public void playbackPaused() {
		state = PlayState.Paused;
		playPauseBtn.setIcon(playIcon);
		playPauseBtn.setToolTipText(PLAY_TOOLTIP);
		// checkButtonsEnabled();
		checkActionBtnsEnabled();
		doRepaint();
		playbackProgress.pause();
	}

	@Override
	public void playbackCompleted() {
		String sid = nextSid;
		if (sid != null) {
			// checkButtonsEnabled();
			checkActionBtnsEnabled();
			ctrl.play(sid);
		} else
			playbackStopped();
	}

	@Override
	public void playbackError(String error) {
		// TODO How to show error to the user?
		playbackCompleted();
	}

	@Override
	public void seekStarted() {
		seeking = true;
	}

	@Override
	public void seekFinished() {
		seeking = false;
	}

	@Override
	public void allTracksLoaded() {
		// Do nothing
	}

	@Override
	public synchronized void tracksUpdated(Collection<Track> streamIds) {
		if (playingStream != null && streamIds.contains(playingStream.getStreamId()))
			updateDataAvailable();
	}

	@Override
	public synchronized void trackUpdated(String streamId, Track t) {
		if (playingStream != null && playingStream.getStreamId().equals(streamId))
			updateDataAvailable();
	}

	public void playSelectedTracks() {
		playingTrackList = frame.mainPanel.currentContentPanel().trackList;
		playingContentPanel = frame.mainPanel.currentContentPanelName();
		List<String> selSids = playingTrackList.getSelectedStreamIds();
		if (selSids.size() > 0) {
			playingTrackList.clearTableSelection();
			final String sid = selSids.get(0);
			ctrl.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					ctrl.play(sid);
				}
			});
		}
	}

	public void trackListPanelChanged(ContentPanel cp) {
		// checkButtonsEnabled();
		checkActionBtnsEnabled();
		TrackList tl = cp.trackList;
		if (tl == null)
			delBtn.setToolTipText("");
		else
			delBtn.setToolTipText(tl.getModel().deleteTracksTooltipDesc());
	}

	private void updateNextPrev() {
		if (playingTrackList == null || playingStream == null) {
			prevSid = nextSid = null;
			prevBtn.setEnabled(false);
			nextBtn.setEnabled(false);
			doRepaint();
			return;
		}
		// getPrevAndNextSids might have to iterate over tracklist (10^4 trax) so fire off a thread
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				if (playingTrackList == null || playingStream == null)
					prevSid = nextSid = null;
				else {
					String[] arr = playingTrackList.getPrevAndNextSids(playingStream.streamId);
					prevSid = arr[0];
					nextSid = arr[1];
				}
				prevBtn.setEnabled(prevSid != null);
				nextBtn.setEnabled(nextSid != null);
				doRepaint();
			}
		});
	}

	private void checkActionBtnsEnabled() {
		boolean tracksSelected = false;
		boolean allowDownload = false;
		boolean allowDel = false;
		boolean allowLove = false;
		if (frame.mainPanel != null && frame.mainPanel.currentContentPanel() != null) {
			ContentPanel cp = frame.mainPanel.currentContentPanel();
			TrackList tl = cp.trackList;
			if (tl != null) {
				List<String> selSids = tl.getSelectedStreamIds();
				tracksSelected = (selSids.size() > 0);
				allowLove = (tracksSelected && !frame.ctrl.lovingAll(selSids));
				// If this is our own library, there won't be any cloud tracks, don't iterate through everything!
				if (cp instanceof MyLibraryContentPanel) {
					allowDownload = false;
					allowDel = tracksSelected;
				} else {
					// Only allow download if at least one of the selected tracks is a cloud track
					for (String sid : selSids) {
						Track t = ctrl.getTrack(sid);
						if (t instanceof CloudTrack) {
							allowDownload = true;
							break;
						}
					}
					allowDel = tl.getModel().allowDelete() && tracksSelected;
				}
			}
		}
		synchronized (this) {
			loveBtn.setEnabled(allowLove);
			dloadBtn.setEnabled(allowDownload);
			delBtn.setEnabled(allowDel);
			// Enable play/pause button unless we are stopped and there are no tracks selected
			boolean ppEnabled = !(state == PlayState.Stopped && !tracksSelected);
			playPauseBtn.setEnabled(ppEnabled);
		}
	}

	private synchronized void updateDataAvailable() {
		Track t = frame.ctrl.getTrack(playingStream.getStreamId());
		float dataAvailable = 0;
		if (t instanceof SharedTrack)
			dataAvailable = 1f;
		else if (t instanceof DownloadingTrack) {
			DownloadingTrack dt = (DownloadingTrack) t;
			dataAvailable = (float) dt.getBytesDownloaded() / playingStream.getSize();
		} else
			dataAvailable = 0;
		playbackProgress.setDataAvailable(dataAvailable);
	}

	private void doRepaint() {
		markAsDirty(this);
	}

	class PlayPauseListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (state) {
			case Stopped:
				playSelectedTracks();
				break;
			case Starting: // fall through
			case Playing:
				ctrl.pause();
				break;
			case Paused:
				ctrl.play(null);
				break;
			}
		}
	}
}
