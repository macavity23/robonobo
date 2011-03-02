package com.robonobo.common.serialization;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class GenericXMLSerializer implements ObjectSerializer {
	
	public Class getDefaultClass() {
		return Object.class;
	}
	
	public void putObject(Object obj, OutputStream out) {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(out));
		encoder.writeObject(obj);
		encoder.close();
	}
	
	public Object getObject(InputStream in) {
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(in));
		return decoder.readObject();
	}
}
