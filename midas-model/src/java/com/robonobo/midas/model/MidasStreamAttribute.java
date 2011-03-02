package com.robonobo.midas.model;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import com.robonobo.core.api.model.StreamAttribute;


@Indexed
public class MidasStreamAttribute extends StreamAttribute {

	public MidasStreamAttribute() {
	}
	
	public MidasStreamAttribute(MidasStream midasStream, String key, String value) {
		super(midasStream, key, value);
	}

	// Redeclare these properties with annotations to make them indexed by lucene
	@Override
	@DocumentId
	public long getId() {
		return super.getId();
	}
	
	@Override
	@Field
	public String getValue() {
		return super.getValue();
	}
}
