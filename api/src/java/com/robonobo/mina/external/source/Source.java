package com.robonobo.mina.external.source;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.robonobo.mina.external.buffer.PageBuffer;


/**
 * A Source is an abstract concept of the original source of data.
 * 
 * This data is not necessarily in pages, it is probably unparsed and 
 * may not be randomly accessible.  A SeekableSource can be provided
 * directly to SourcePageBuffer to create a PageBuffer based on a file,
 * for example.  
 * 
 * A Broadcast is created with a Source and will internally implement some 
 * sort of Source => Page transformation.
 *
 * @author ray
 *
 */
public interface Source extends Serializable {
	public String getName();
	public String getSourceUrl() throws SourceException;
	/**
	 * @param workingDir This directory will be used to persist metadata files
	 */
	public PageBuffer getPageBuffer() throws IOException;
}
