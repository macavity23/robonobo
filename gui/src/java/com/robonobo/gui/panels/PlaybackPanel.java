package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import static com.robonobo.gui.RoboColor.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.*;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.RoboFont;
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
	RobonoboFrame frame;
	RobonoboController control;
	RLabel titleLbl, artistLbl, albumLbl;
	PlaybackProgressBar playbackProgress;
	Stream playingStream = null;
	PlayState state = PlayState.Stopped;
	TrackList playingTrackList;
	String playingContentPanel;
	RButton prevBtn, dloadBtn, playPauseBtn, nextBtn, delBtn;
	boolean checkedNextTrack = false;
	boolean seeking = false;
	Log log = LogFactory.getLog(getClass());
	String prevSid, nextSid;

	public PlaybackPanel(final RobonoboFrame frame) {
		this.frame = frame;
		control = frame.getController();
		setLayout(new BorderLayout());
		setName("playback.background.panel");
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setBackground(MID_GRAY);
		JPanel titlesPanel = new JPanel();
		titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
		titlesPanel.setOpaque(false);
		titlesPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Double click on title shows the playing track in the content panel
				if (e.getClickCount() == 2) {
					if (playingStream != null) {
						frame.getLeftSidebar().selectForContentPanel(playingContentPanel);
						playingTrackList.scrollTableToStream(playingStream.getStreamId());
					}
					e.consume();
				}
			}
		});
		add(titlesPanel, BorderLayout.CENTER);
		titleLbl = new RLabel26("");
		titleLbl.setPreferredSize(new Dimension(450, 35));
		titleLbl.setMinimumSize(new Dimension(450, 35));
		titleLbl.setMaximumSize(new Dimension(450, 35));
		titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		titleLbl.setForeground(BLUE_GRAY);
		titlesPanel.add(titleLbl);
		artistLbl = new RLabel20B("");
		artistLbl.setPreferredSize(new Dimension(450, 20));
		artistLbl.setBorder(BorderFactory.createEmptyBorder(1, 10, 0, 0));
		titlesPanel.add(artistLbl);
		albumLbl = new RLabel18("");
		albumLbl.setPreferredSize(new Dimension(450, 20));
		albumLbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));
		titlesPanel.add(albumLbl);
		final JPanel playerPanel = new JPanel(new BorderLayout(5, 5));
		add(playerPanel, BorderLayout.EAST);
		playerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
		playbackProgress = new PlaybackProgressBar(frame);
		playbackProgress.lock();
		playerPanel.add(playbackProgress, BorderLayout.NORTH);
		final JPanel playerCtrlPanel = new JPanel(new BorderLayout());
		playerPanel.add(playerCtrlPanel, BorderLayout.CENTER);
		playerCtrlPanel.setOpaque(false);
		final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		playerCtrlPanel.add(buttonsPanel, BorderLayout.WEST);
		buttonsPanel.setOpaque(false);
		prevBtn = new RRoundButton();
		prevBtn.setIcon(prevIcon);
		prevBtn.setToolTipText(PREV_TOOLTIP);
		prevBtn.setPreferredSize(new Dimension(50, 50));
		prevBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prev();
			}
		});
		buttonsPanel.add(prevBtn);
		dloadBtn = new RRoundButton();
		dloadBtn.setIcon(dloadIcon);
		dloadBtn.setToolTipText(DOWNLOAD_TOOLTIP);
		dloadBtn.setPreferredSize(new Dimension(50, 50));
		dloadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TrackList tl = frame.getMainPanel().currentContentPanel().getTrackList();
				if (tl != null) {
					List<String> selSids = tl.getSelectedStreamIds();
					for (String sid : selSids) {
						Track t = control.getTrack(sid);
						try {
							if (t instanceof CloudTrack)
								control.addDownload(sid);
						} catch (RobonoboException ex) {
							log.error("Error adding download", ex);
						}
					}
				}
			}
		});
		buttonsPanel.add(dloadBtn);
		playPauseBtn = new RRoundButton();
		playPauseBtn.setIcon(playIcon);
		playPauseBtn.setToolTipText(PLAY_TOOLTIP);
		playPauseBtn.setPreferredSize(new Dimension(50, 50));
		playPauseBtn.addActionListener(new PlayPauseListener());
		buttonsPanel.add(playPauseBtn);
		nextBtn = new RRoundButton();
		nextBtn.setIcon(nextIcon);
		nextBtn.setToolTipText(NEXT_TOOLTIP);
		nextBtn.setPreferredSize(new Dimension(50, 50));
		nextBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});
		buttonsPanel.add(nextBtn);
		buttonsPanel.add(Box.createHorizontalStrut(50));
		delBtn = new RSquareDelButton();
		delBtn.setPreferredSize(new Dimension(40, 40));
		delBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TrackList tl = frame.getMainPanel().currentContentPanel().getTrackList();
				if (tl != null)
					tl.deleteSelectedTracks();
			}
		});
		buttonsPanel.add(delBtn);
		// checkButtonsEnabled();
		checkPlayPauseDeleteEnabled();
		updateNextPrev();
		control.addPlaybackListener(this);
		control.addTrackListener(this);
	}

	public void trackSelectionChanged() {
		// checkButtonsEnabled();
		checkPlayPauseDeleteEnabled();
		doRepaint();
	}

	public void next() {
		// Grab a copy of the sid to avoid a miniscule chance of it changing while this is called
		String sid = nextSid;
		if (sid != null)
			control.play(sid);
	}

	public void prev() {
		// If we're near the beginning of our track, go back to the previous track, else go back to the start of this
		// one
		if (playbackProgress.getTrackPosition() < PREV_TRACK_GRACE_PERIOD) {
			// Grab a copy of the sid to avoid a miniscule chance of it changing while this is called
			String sid = prevSid;
			if (sid != null)
				control.play(sid);
		} else {
			String sid = playingStream.getStreamId();
			TrackList t = playingTrackList;
			String cp = playingContentPanel;
			control.stopPlayback();
			playingTrackList = t;
			playingContentPanel = cp;
			control.play(sid);
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
		checkPlayPauseDeleteEnabled();
		// This causes a repaint
		updateNextPrev();
	}

	@Override
	public void playbackStarting() {
		state = PlayState.Starting;
		Stream s = frame.getController().currentPlayingStream();
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
		checkPlayPauseDeleteEnabled();
		doRepaint();
	}

	@Override
	public void playbackRunning() {
		state = PlayState.Playing;
		playPauseBtn.setIcon(pauseIcon);
		playPauseBtn.setToolTipText(PAUSE_TOOLTIP);
		playbackProgress.play();
		// checkButtonsEnabled();
		checkPlayPauseDeleteEnabled();
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
			if (!checkedNextTrack && positionMs > (playingStream.getDuration() - control.getConfig().getDownloadCacheTime() * 1000)) {
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
			control.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					control.preFetch(fetchSid);
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
		checkPlayPauseDeleteEnabled();
		doRepaint();
		playbackProgress.pause();
	}

	@Override
	public void playbackCompleted() {
		String sid = nextSid;
		if (sid != null) {
			// checkButtonsEnabled();
			checkPlayPauseDeleteEnabled();
			control.play(sid);
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
		playingTrackList = frame.getMainPanel().currentContentPanel().getTrackList();
		playingContentPanel = frame.getMainPanel().currentContentPanelName();
		List<String> selSids = playingTrackList.getSelectedStreamIds();
		if (selSids.size() > 0) {
			playingTrackList.clearTableSelection();
			final String sid = selSids.get(0);
			control.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					control.play(sid);
				}
			});
		}
	}

	public void trackListPanelChanged(ContentPanel cp) {
		// checkButtonsEnabled();
		checkPlayPauseDeleteEnabled();
		TrackList tl = cp.getTrackList();
		if(tl == null)
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
		frame.control.getExecutor().execute(new CatchingRunnable() {
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

	private void checkPlayPauseDeleteEnabled() {
		boolean tracksSelected = false;
		boolean allowDownload = false;
		boolean allowDel = false;
		if (frame.getMainPanel() != null && frame.getMainPanel().currentContentPanel() != null) {
			TrackList tl = frame.getMainPanel().currentContentPanel().getTrackList();
			if (tl != null) {
				List<String> selSids = tl.getSelectedStreamIds();
				tracksSelected = (selSids.size() > 0);
				// Only allow download if at least one of the selected tracks is a cloud track
				for (String sid : selSids) {
					Track t = control.getTrack(sid);
					if (t instanceof CloudTrack) {
						allowDownload = true;
						break;
					}
				}
				allowDel = tl.getModel().allowDelete() && tracksSelected;
			}
		}
		synchronized (this) {
			dloadBtn.setEnabled(allowDownload);
			delBtn.setEnabled(allowDel);
			// Enable play/pause button unless we are stopped and there are no tracks selected
			boolean ppEnabled = !(state == PlayState.Stopped && !tracksSelected);
			playPauseBtn.setEnabled(ppEnabled);
		}
	}

	private synchronized void updateDataAvailable() {
		Track t = frame.getController().getTrack(playingStream.getStreamId());
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
		RepaintManager.currentManager(this).markCompletelyDirty(this);
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
				control.pause();
				break;
			case Paused:
				control.play(null);
				break;
			}
		}
	}
}
