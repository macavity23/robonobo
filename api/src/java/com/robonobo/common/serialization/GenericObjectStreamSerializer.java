package com.robonobo.common.serialization;

import java.io.*;

public abstract class GenericObjectStreamSerializer implements ObjectSerializer {
	public abstract Class getDefaultClass();

	public Object getObject(InputStream in) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(in);
		Object obj;
		try {
			obj = ois.readObject();
		} catch(ClassNotFoundException e) {
			// Can't happen!
			throw new RuntimeException(e);
		}
		ois.close();
		return obj;
	}

	public void putObject(Object obj, OutputStream out) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(obj);
		oos.close();
	}
}
