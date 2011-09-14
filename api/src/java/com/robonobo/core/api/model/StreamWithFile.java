package com.robonobo.core.api.model;

import java.io.File;

/**
 * Allows us to store a file along with the stream - this is not persisted or remoted
 */
public class StreamWithFile extends Stream {
	public File file;
}
