package com.robonobo.core.api;

import java.io.File;
import java.io.IOException;

public interface StreamIdGenerator {
	public String generateStreamId(File file) throws IOException;
}
