package com.robonobo.gui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.TrackListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;

/**
 * A track list containing tracks, maintained in the default stream order (see StreamComparator). Add the tracks you want in your subclass constructor and
 * implement trackUpdated() to maintain your track list.
 * 
 * @author mortw0
 * 
 */
@SuppressWarnings("serial")
public abstract class FreeformTrackListTableModel extends TrackListTableModel implements TrackListener {
	protected Log log = LogFactory.getLog(getClass());
	protected RobonoboController control;
	/**
	 * Unfortunately we need to keep track of all the streams here so we can keep the tracks sorted in the right order
	 */
	protected List<Stream> streams = new ArrayList<Stream>();
	protected StreamComparator comparator = new StreamComparator();
	/**
	 * We keep track of the indices of all our streams, so that when information about them is updated (upload/Download speed, status), we know which index
	 * we're talking about and can update the table swiftly
	 */
	protected Map<String, Integer> streamIndices = new HashMap<String, Integer>();

	public FreeformTrackListTableModel(RobonoboController controller) {
		this.control = controller;
		controller.addTrackListener(this);
	}

	public void tracksUpdated(Collection<String> streamIds) {
		// Could do something smarter here, but probably don't need to
		for (String streamId : streamIds) {
			trackUpdated(streamId);
		}
	}

	protected void add(Track t) {
		add(t, true);
	}

	protected void add(final Track t, final boolean fireUpdate) {
		try {
			CatchingRunnable meat = new CatchingRunnable() {
				@Override
				public void doRun() throws Exception {
					synchronized (FreeformTrackListTableModel.this) {
						Stream stream = t.getStream();
						String streamId = stream.getStreamId();
						if (streamIndices.containsKey(streamId))
							return;
						// See the binarySearch javadoc for the meaning of this
						// incantation
						final int newIndex = -Collections.binarySearch(streams, stream, comparator) - 1;
						if (newIndex < 0)
							throw new SeekInnerCalmException();
						// Update our stream indices
						for (int i = newIndex; i < streams.size(); i++) {
							String incSid = streams.get(i).getStreamId();
							streamIndices.put(incSid, i + 1);
						}
						streamIndices.put(streamId, newIndex);
						streams.add(newIndex, stream);
						if (fireUpdate)
							fireTableRowsInserted(newIndex, newIndex);
					}
				}
			};
			if (SwingUtilities.isEventDispatchThread())
				meat.run();
			else
				SwingUtilities.invokeLater(meat);
		} catch (Exception e) {
			log.error("Error adding sc to tablemodel");
		}
	}

	protected void remove(final Track t) {
		try {
			SwingUtilities.invokeLater(new CatchingRunnable() {
				@Override
				public void doRun() throws Exception {
					synchronized (FreeformTrackListTableModel.this) {
						Stream stream = t.getStream();
						String streamId = stream.getStreamId();
						if (!streamIndices.containsKey(streamId))
							return;
						final int index = streamIndices.get(streamId);
						// Update our stream indices
						for (int i = index + 1; i < streams.size(); i++) {
							String decSid = streams.get(i).getStreamId();
							streamIndices.put(decSid, i - 1);
						}
						streams.remove(index);
						streamIndices.remove(streamId);
						fireTableRowsDeleted(index, index);
					}
				}
			});
		} catch (Exception e) {
			log.error("Error removing sc from tm");
		}
	}

	@Override
	public synchronized String getStreamId(int index) {
		if (index < 0 || index >= streams.size())
			return null;
		return streams.get(index).getStreamId();
	}

	@Override
	public Track getTrack(int index) {
		String streamId;
		synchronized (this) {
			if (index < 0 || index >= streams.size())
				return null;
			streamId = streams.get(index).getStreamId();
		}
		Track result = control.getTrack(streamId);
		return result;
	}

	@Override
	public synchronized int getTrackIndex(String streamId) {
		if (streamIndices.containsKey(streamId)) {
			return streamIndices.get(streamId);
		}
		return -1;
	}

	@Override
	public synchronized int numTracks() {
		return streams.size();
	}

	@Override
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		try {
			super.fireTableRowsInserted(firstRow, lastRow);
		} catch (ArrayIndexOutOfBoundsException oob) {
			log.error("ArrayIndexOOB inserting into table");
		}
	}

	@Override
	public void fireTableRowsDeleted(int firstRow, int lastRow) {
		try {
			super.fireTableRowsDeleted(firstRow, lastRow);
		} catch (ArrayIndexOutOfBoundsException oob) {
			log.error("ArrayIndexOOB inserting into table");
		}
	}

	@Override
	public void fireTableRowsUpdated(int firstRow, int lastRow) {
		try {
			super.fireTableRowsUpdated(firstRow, lastRow);
		} catch (ArrayIndexOutOfBoundsException oob) {
			log.error("ArrayIndexOOB inserting into table");
		}
	}
}
