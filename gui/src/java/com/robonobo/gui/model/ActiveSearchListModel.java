package com.robonobo.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class ActiveSearchListModel extends DefaultListModel {
	List<String> queries = new ArrayList<String>();
	private RobonoboFrame frame;
	
	public ActiveSearchListModel(RobonoboFrame frame) {
		this.frame = frame;
	}

	public Object getElementAt(int index) {
		return queries.get(index);
	}

	public int getSize() {
		return queries.size();
	}

	public int indexOfQuery(String query) {
		for (int i = 0; i < queries.size(); i++) {
			if(queries.get(i).equals(query))
				return i;
		}
		return -1;
	}
	
	public void removeElementAt(int index) {
		queries.remove(index);
		fireIntervalRemoved(this, index, index);
	}
	
	/**
	 * @return The table model which should be used to create the search result panel, or null if no panel should be created
	 */
	public SearchResultTableModel addSearch(String query) {
		if(queries.contains(query))
			return null;
		queries.add(query);
		fireIntervalAdded(this, getSize()-1, getSize()-1);
		SearchResultTableModel srtm = SearchResultTableModel.create(frame);
		frame.ctrl.search(query, 0, srtm);	
		return srtm;
	}
}