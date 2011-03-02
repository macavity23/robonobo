package com.robonobo.common.serialization;

import java.io.*;

public interface ObjectSerializer {
	
	public Class getDefaultClass();
	
	public void putObject(Object obj, OutputStream out) throws IOException;
	
	public Object getObject(InputStream in) throws IOException;
}