package com.robonobo.core.api.model;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.api.proto.CoreApi.StreamAttributeMsg;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;

public class Stream implements Comparable<Stream> {
	protected static Pattern trackNumPat = Pattern.compile("^(\\d+)");
	private static final String ARTIST = "artist";
	private static final String ALBUM = "album";
	private static final String TRACK = "track";
	protected String mimeType;
	public String streamId;
	public String title;
	protected String description = "";
	protected BufferedImage thumbnail;
	protected Set<StreamAttribute> attributes = new HashSet<StreamAttribute>();
	protected Map<String, StreamAttribute> attrMap = new HashMap<String, StreamAttribute>();
	protected long size;
	/** Millisecs */
	protected long duration;
	protected Date updated;
	/**
	 * These are kept as attributes but we keep a field too for fast retrieval, Streams are used often for sorting and
	 * such
	 */
	public String artist;
	public String album;
	/**
	 * Also kept as an attribute - this will be -1 if the attribute is not present
	 */
	public int track = -1;

	public Stream() {
	}

	public Stream(StreamMsg msg) {
		setStreamId(msg.getId());
		setMimeType(msg.getMimeType());
		setTitle(msg.getTitle());
		setSize(msg.getSize());
		setDuration(msg.getDuration());
		for (StreamAttributeMsg saMsg : msg.getAttributeList()) {
			setAttrValue(saMsg.getName(), saMsg.getValue());
		}
	}

	public StreamMsg toMsg() {
		StreamMsg.Builder b = StreamMsg.newBuilder();
		b.setId(streamId);
		b.setMimeType(mimeType);
		b.setTitle(title);
		b.setSize(size);
		b.setDuration(duration);
		for (StreamAttribute sa : attributes) {
			b.addAttribute(sa.toMsg());
		}
		return b.build();
	}

	public void copyFrom(Stream s) {
		if (s.getTitle() != null)
			setTitle(s.getTitle());
		if (s.getDescription() != null)
			setDescription(s.getDescription());
		if (s.getStreamId() != null)
			setStreamId(s.getStreamId());
		if (s.getMimeType() != null)
			setMimeType(s.getMimeType());
		setDuration(s.getDuration());
		if (s.getSize() > 0)
			setSize(s.getSize());
		if (s.getUpdated() != null)
			setUpdated(s.getUpdated());
		// Don't just copy the attributes set as setAttrValue() sets the
		// Attribute->Stream link
		for (StreamAttribute sa : s.getAttributes()) {
			setAttrValue(sa.getName(), sa.getValue());
		}
	}

	/**
	 * If any of our properties are null, fill them from the supplied stream
	 * 
	 * @return if any properties were changed
	 */
	public boolean fillBlanks(Stream s) {
		boolean changed = false;
		if (title == null) {
			setTitle(s.getTitle());
			changed = true;
		}
		if (description == null) {
			setDescription(s.getDescription());
			changed = true;
		}
		if (streamId == null) {
			setStreamId(s.getStreamId());
			changed = true;
		}
		if (mimeType == null) {
			setMimeType(s.getMimeType());
			changed = true;
		}
		if (duration <= 0) {
			duration = s.duration;
			changed = true;
		}
		if (size <= 0) {
			size = s.size;
			changed = true;
		}
		if (s.getUpdated() != null) {
			setUpdated(s.getUpdated());
			changed = true;
		}
		// Don't just copy the attributes set as setAttrValue() sets the
		// Attribute->Stream link
		for (StreamAttribute sa : s.getAttributes()) {
			if (getAttrValue(sa.getName()) != null) {
				setAttrValue(sa.getName(), sa.getValue());
				changed = true;
			}
		}
		if (changed)
			updated = TimeUtil.now();
		return changed;
	}

	public boolean equals(Object obj) {
		return (obj instanceof Stream && ((Stream) obj).getStreamId().equals(getStreamId()));
	}

	/**
	 * Millisecs
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Bytes
	 */
	public long getSize() {
		return size;
	}

	public Date getUpdated() {
		return updated;
	}

	public int hashCode() {
		return getClass().getName().hashCode() ^ streamId.hashCode();
	}

	/**
	 * Millisecs
	 */
	public void setDuration(long length) {
		this.duration = length;
	}

	/**
	 * Bytes
	 */
	public void setSize(long size) {
		this.size = size;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("[Stream:id=").append(getStreamId());
		sb.append(",title=").append(getTitle()).append("]");
		return sb.toString();
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = truncate(mimeType, 128);
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = truncate(title, 256);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = truncate(description, 512);
	}

	public BufferedImage getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(BufferedImage thumbnail) {
		this.thumbnail = thumbnail;
	}

	public Set<StreamAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<StreamAttribute> attributes) {
		this.attributes = attributes;
		for (StreamAttribute a : attributes) {
			attrMap.put(a.getName(), a);
		}
	}

	public void setAttrValue(String key, String value) {
		if (attrMap.containsKey(key))
			attrMap.get(key).setValue(value);
		else {
			StreamAttribute a = new StreamAttribute(this, key, value);
			a.setStream(this);
			attributes.add(a);
			attrMap.put(key, a);
		}
		if (key.equals(ARTIST))
			artist = value;
		else if (key.equals(ALBUM))
			album = value;
		else if(key.equals(TRACK))
			track = trackFromStr(value);
	}

	public String getAttrValue(String key) {
		StreamAttribute sa = attrMap.get(key);
		if (sa == null)
			return null;
		return sa.getValue();
	}

	public int compareTo(Stream o) {
		int titleCmp = title.compareTo(o.title);
		if (titleCmp != 0)
			return titleCmp;
		return streamId.compareTo(o.streamId);
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public int getTrack() {
		return track;
	}

	private static int trackFromStr(String trackStr) {
		Matcher m = trackNumPat.matcher(trackStr);
		return m.find() ? Integer.parseInt(m.group()) : -1;
	}

	private String truncate(String str, int maxLen) {
		if (str.length() <= maxLen)
			return str;
		return str.substring(0, maxLen);
	}
}
