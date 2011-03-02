package com.robonobo.midas.model;

import java.util.Date;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.StreamAttribute;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;


@Indexed
public class MidasStream extends com.robonobo.core.api.model.Stream {
	Date published;
	Date modified;
	
	public MidasStream() {
	}
	
	public MidasStream(StreamMsg msg) {
		super(msg);
	}
	
	public Date getPublished() {
		return published;
	}
	public void setPublished(Date published) {
		this.published = published;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	public void copyFrom(Stream s) {
		if(s instanceof MidasStream) {
			MidasStream ms = (MidasStream) s;
			if(ms.getPublished() != null)
				setPublished(ms.getPublished());
			if(ms.getModified() != null)
				setModified(ms.getModified());
		}
		super.copyFrom(s);
	}

	public void setAttrValue(String key, String value) {
		if(attrMap.containsKey(key))
			attrMap.get(key).setValue(value);
		else {
			StreamAttribute a = new MidasStreamAttribute(this, key, value);
			attributes.add(a);
			attrMap.put(key, a);
		}
	}

	// Redeclare these properties with annotations to make them indexed by lucene
	@Override
	@DocumentId
	public String getStreamId() {
		return super.getStreamId();
	}

	@Override
	@Field
	public String getTitle() {
		return super.getTitle();
	}
	
	@Override
	@Field
	public String getDescription() {
		return super.getDescription();
	}
}
