package com.robonobo.gui.model;

import java.util.*;

import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class MyLibraryTableModel extends FreeformTrackListTableModel {
	// Keep track of mass-deleted tracks
	Set<String> deletedSids = new HashSet<String>();
	RobonoboFrame frame;

	public MyLibraryTableModel(RobonoboFrame frame) {
		super(frame.getController());
		this.frame = frame;
		// If everything's started already before we get here, load it now
		if (control.haveAllSharesStarted())
			allTracksLoaded();
	}

	public void allTracksLoaded() {
		final List<Track> trax = new ArrayList<Track>();
		synchronized (this) {
			streams.clear();
			streamIndices.clear();
			for (String streamId : control.getShares()) {
				Track t = control.getTrack(streamId);
				trax.add(t);
			}
			for (String streamId : control.getDownloads()) {
				Track t = control.getTrack(streamId);
				trax.add(t);
			}
		}
		if (trax.size() < TrackList.TRACKLIST_SIZE_THRESHOLD)
			add(trax, true);
		else {
			frame.runSlowTask("library loading", new CatchingRunnable() {
				public void doRun() throws Exception {
					add(trax, true);
				}
			});
		}
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void deleteTracks(List<String> streamIds) {
		// Rather than iterating through each stream id, deleting it, and waiting for the callbacks to update everything
		// - which takes AGES if we're deleting a lot of tracks - just mark them as deleted, and actually do the work in
		// the background
		ArrayList<Integer> delIdxs = new ArrayList<Integer>();
		synchronized (this) {
			deletedSids.addAll(streamIds);
			for (String sid : streamIds) {
				Integer idx = streamIndices.remove(sid);
				if (idx != null) {
					delIdxs.add(idx);
				}
			}
			// Fix stream indices
			Collections.sort(delIdxs);
			for (int i = delIdxs.size() - 1; i >= 0; i--) {
				int streamIdx = delIdxs.get(i);
				int doneStreamIdx = (i == delIdxs.size() - 1) ? streams.size() : delIdxs.get(i + 1);
				int numToDec = (i + 1);
				for (int j = streamIdx + 1; j < doneStreamIdx; j++) {
					String decSid = streams.get(j).getStreamId();
					streamIndices.put(decSid, j - numToDec);
				}
			}
			for (int i = delIdxs.size() - 1; i >= 0; i--) {
				int idx = delIdxs.get(i);
				// Explicit cast otherwise this calls streams.remove(Object), oops
				streams.remove(idx);
			}
		}
		if (delIdxs.size() > 0) {
			// Collect our deleted indices into as few events as possible by grabbing which contiguous blocks of indices
			// have been deleted
			final List<Integer> startIdxs = new ArrayList<Integer>();
			final List<Integer> endIdxs = new ArrayList<Integer>();
			int starti = 0;
			for (int i = 1; i < delIdxs.size(); i++) {
				int delIdx = delIdxs.get(i);
				if (starti < 0) {
					starti = delIdx;
					continue;
				}
				int prevDelIdx = delIdxs.get(i - 1);
				if (delIdx == (prevDelIdx + 1)) {
					// Just continuing this block
					continue;
				}
				// New contiguous block
				startIdxs.add(starti);
				endIdxs.add(prevDelIdx);
				starti = delIdx;
			}
			// Block at the end
			if (starti >= 0) {
				startIdxs.add(starti);
				endIdxs.add(delIdxs.get(delIdxs.size() - 1));
			}
			// Fire the delete events
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					for (int i = 0; i < startIdxs.size(); i++) {
						fireTableRowsDeleted(startIdxs.get(i), endIdxs.get(i));
					}
				}
			});
		}
		for (String sid : streamIds) {
			Track t = control.getTrack(sid);
			try {
				if (t instanceof DownloadingTrack)
					control.deleteDownload(sid);
				else if (t instanceof SharedTrack)
					control.deleteShare(sid);
			} catch (RobonoboException ex) {
				log.error("Error deleting share/download", ex);
			}
		}

	}

	public void trackUpdated(String streamId) {
		synchronized (this) {
			// If this is part of a mass-delete, just ignore it, we've already taken care of it
			if (deletedSids.contains(streamId)) {
				deletedSids.remove(streamId);
				return;
			}
		}
		Track t = control.getTrack(streamId);
		boolean shouldAdd = false;
		boolean shouldRm = false;
		int index = -1;
		if (t instanceof CloudTrack) {
			// We're not interested - if we currently have it, remove it
			synchronized (this) {
				if (streamIndices.containsKey(streamId))
					shouldRm = true;
			}
		} else {
			// We are interested - if we don't have it, add it
			synchronized (this) {
				if (streamIndices.containsKey(streamId))
					index = streamIndices.get(streamId);
				else
					shouldAdd = true;
			}
		}
		if (shouldAdd)
			add(t);
		else if (shouldRm)
			remove(t);
		else if (index >= 0) {
			// Updated
			final int findex = index;
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					fireTableRowsUpdated(findex, findex);
				}
			});
		}
	}
}
