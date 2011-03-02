package com.robonobo.gui.model;

import java.util.*;

import javax.swing.DefaultListModel;

import com.robonobo.core.RobonoboController;

@SuppressWarnings("serial")
public class ActiveSearchListModel extends DefaultListModel {
	List<String> queries = new ArrayList<String>();
	private RobonoboController control;
	
	public ActiveSearchListModel(RobonoboController control) {
		this.control = control;
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
		SearchResultTableModel srtm = new SearchResultTableModel(control);
		control.search(query, 0, srtm);	
		return srtm;
	}
}