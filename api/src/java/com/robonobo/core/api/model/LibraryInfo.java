package com.robonobo.core.api.model;

import java.util.Date;

public class LibraryInfo {
	int numUnseen;
	Date lastChecked;

	public LibraryInfo(int numUnseen, Date lastChecked) {
		this.numUnseen = numUnseen;
		this.lastChecked = lastChecked;
	}

	public int getNumUnseen() {
		return numUnseen;
	}

	public void setNumUnseen(int numUnseen) {
		this.numUnseen = numUnseen;
	}

	public Date getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Date lastChecked) {
		this.lastChecked = lastChecked;
	}
}
