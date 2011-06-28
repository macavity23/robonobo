package com.robonobo.gui.model;

import java.util.List;

import javax.swing.table.TableModel;

import com.robonobo.core.api.model.Track;

public interface TrackListTableModel extends TableModel {
	public int getColumnCount();

	public String getColumnName(int column);

	public int getRowCount();

	public Class<?> getColumnClass(int columnIndex);

	public Object getValueAt(int rowIndex, int columnIndex);

	// By default we hide the track num and stream id
	public int[] hiddenCols();

	/** Return true in a subclass to have onScroll() called every time the track list is scrolled (as long as
	 * wantScrollEventsNow() returns true) */
	public boolean wantScrollEventsEver();

	/** Return true in a subclass to have onScroll() called with the in-view indexen (wantScrollEventsEver() must also
	 * return true) */
	public boolean wantScrollEventsNow();

	/** @param indexen
	 *            The items currently in-viewport (model indexes, not view). Note this is called on the UI thread, so be
	 *            thrifty with your cycles! */
	public void onScroll(int[] indexen);

	public Track getTrack(int index);

	public String getStreamId(int index);

	/** Are we allowed to delete tracks from this tracklist? */
	public boolean allowDelete();

	public void deleteTracks(List<String> streamIds);

	/** This might have to iterate the tracklist, so call it sparingly! */
	public int getTrackIndex(String sid);
	
	/** The description attached to UI elements that delete tracks from this tracklist */
	public String deleteTracksDesc();
}