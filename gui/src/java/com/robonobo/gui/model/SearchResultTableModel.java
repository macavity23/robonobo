package com.robonobo.gui.model;

import java.util.List;

import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.SearchListener;
import com.robonobo.core.api.model.*;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class SearchResultTableModel extends FreeformTrackListTableModel implements SearchListener, FoundSourceListener {

	public SearchResultTableModel(RobonoboController controller) {
		super(controller);
	}

	public void trackUpdated(String streamId) {
		int updateIndex = -1;
		synchronized (this) {
			if (streamIndices.containsKey(streamId))
				updateIndex = streamIndices.get(streamId);
		}
		if (updateIndex >= 0) {
			if (SwingUtilities.isEventDispatchThread())
				fireTableRowsUpdated(updateIndex, updateIndex);
			else {
				final int i = updateIndex;
				SwingUtilities.invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						fireTableRowsUpdated(i, i);
					}
				});
			}
		}
	}

	public void allTracksLoaded() {
		// Do nothing
	}

	public void die() {
		for (Stream s : streams) {
			control.stopFindingSources(s.getStreamId(), this);
		}
		streams.clear();
		streamIndices.clear();
	}

	public void gotNumberOfResults(int numResults) {
		// Do nothing
	}

	public void foundResult(final Stream s) {
		Track t = control.getTrack(s.getStreamId());
		add(t);
		if (t instanceof CloudTrack)
			control.findSources(s.getStreamId(), this);
	}

	public void foundBroadcaster(String streamId, String nodeId) {
		trackUpdated(streamId);
	}
	
	@Override
	public boolean allowDelete() {
		return false;
	}
	
	@Override
	public void deleteTracks(List<String> streamIds) {
		throw new Errot();
	}
}
