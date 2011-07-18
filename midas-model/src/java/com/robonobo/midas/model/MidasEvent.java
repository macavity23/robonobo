package com.robonobo.midas.model;

/** Something that happens in midas. Used for reporting.
 * 
 * @author macavity */
public class MidasEvent {
	long eventId;
	long userId;
	String eventType;
	String eventInfo;

	public MidasEvent() {
	}

	public MidasEvent(long userId, String eventType, String eventInfo) {
		this.userId = userId;
		this.eventType = eventType;
		this.eventInfo = eventInfo;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEventInfo() {
		return eventInfo;
	}

	public void setEventInfo(String eventInfo) {
		this.eventInfo = eventInfo;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
}
