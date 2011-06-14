package com.robonobo.gui.model;

import static com.robonobo.gui.GuiUtil.*;

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
import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.ContiguousBlock;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.TrackListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.GuiUtil;

/**
 * A track list containing tracks, maintained in the default stream order (see StreamComparator). Add the tracks you
 * want in your subclass constructor and implement trackUpdated() to maintain your track list.
 * 
 * @author mortw0
 * 
 */
@SuppressWarnings("serial")
public abstract class FreeformTrackListTableModel extends AbstractTrackListTableModel implements TrackListener {
	protected Log log = LogFactory.getLog(getClass());
	protected RobonoboController control;
	/**
	 * Unfortunately we need to keep track of all the streams here so we can keep the tracks sorted in the right order
	 */
	protected List<Stream> streams = new ArrayList<Stream>();
	protected StreamComparator comparator = new StreamComparator();
	/**
	 * We keep track of the indices of all our streams, so that when information about them is updated (upload/Download
	 * speed, status), we know which index we're talking about and can update the table swiftly
	 */
	protected Map<String, Integer> streamIndices = new HashMap<String, Integer>();

	public FreeformTrackListTableModel(RobonoboController controller) {
		this.control = controller;
		controller.addTrackListener(this);
	}

	protected void add(Track t) {
		add(t, true);
	}

	/** Call only from within sync block AND on ui thread */
	private int doAdd(Track t) {
		Stream stream = t.getStream();
		String streamId = stream.getStreamId();
		if (streamIndices.containsKey(streamId))
			return -1;
		// See the binarySearch javadoc for the meaning of this
		// incantation
		final int newIndex = -Collections.binarySearch(streams, stream, comparator) - 1;
		if (newIndex < 0)
			throw new Errot();
		// Update our stream indices
		for (int i = newIndex; i < streams.size(); i++) {
			String incSid = streams.get(i).getStreamId();
			streamIndices.put(incSid, i + 1);
		}
		streamIndices.put(streamId, newIndex);
		streams.add(newIndex, stream);
		return newIndex;
	}

	protected void add(final Track t, final boolean fireUpdate) {
		try {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					int idx;
					synchronized (FreeformTrackListTableModel.this) {
						idx = doAdd(t);
					}
					if (fireUpdate && idx >= 0)
						fireTableRowsInserted(idx, idx);
				}
			});
		} catch (Exception e) {
			log.error("Error adding track to tablemodel");
		}
	}

	protected void add(final Collection<Track> trax, final boolean fireUpdate) {
		try {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					// Keep track of which indices have been added so we can fire as few events as possibleÊ
					ContiguousBlock cb = new ContiguousBlock();
					synchronized (FreeformTrackListTableModel.this) {
						for (Track t : trax) {
							int idx = doAdd(t);
							if (idx >= 0)
								cb.add(idx);
						}
					}
					if (fireUpdate) {
						int[] block;
						while ((block = cb.getNextBlock()) != null) {
							fireTableRowsInserted(block[0], block[1]);
						}
					}
				}
			});
		} catch (Exception e) {
			log.error("Error adding tracks to tablemodel");
		}
	}

	/** Call only from within sync block AND on ui thread */
	private int doRemove(final Track t) {
		Stream stream = t.getStream();
		String streamId = stream.getStreamId();
		if (!streamIndices.containsKey(streamId))
			return -1;
		final int index = streamIndices.get(streamId);
		// Update our stream indices
		for (int i = index + 1; i < streams.size(); i++) {
			String decSid = streams.get(i).getStreamId();
			streamIndices.put(decSid, i - 1);
		}
		streams.remove(index);
		streamIndices.remove(streamId);
		return index;
	}

	protected void remove(final Track t) {
		try {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					int idx;
					synchronized (FreeformTrackListTableModel.this) {
						idx = doRemove(t);
					}
					if (idx >= 0)
						fireTableRowsDeleted(idx, idx);
				}
			});
		} catch (Exception e) {
			log.error("Error removing sc from tm");
		}
	}

	protected void remove(final Collection<Track> trax, final boolean fireEvent) {
		try {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					// Keep track of which indices have been removed so we can fire as few events as possibleÊ
					ContiguousBlock cb = new ContiguousBlock();
					synchronized (FreeformTrackListTableModel.this) {
						for (Track t : trax) {
							int idx = doRemove(t);
							if (idx >= 0)
								cb.add(idx);
						}
					}
					int[] block;
					if (fireEvent) {
						while ((block = cb.getNextBlock()) != null) {
							fireTableRowsDeleted(block[0], block[1]);
						}
					}
				}
			});
		} catch (Exception e) {
			log.error("Error adding sc to tablemodel");
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
