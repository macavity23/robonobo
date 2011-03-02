package com.robonobo.core;

import java.io.File;
import java.io.IOException;


import com.robonobo.core.api.StreamIdGenerator;
import com.twmacinta.util.MD5;

public class MD5StreamIdGenerator implements StreamIdGenerator {

	public String generateStreamId(File file) throws IOException {
		return "md5:"+MD5.asHex(MD5.getHash(file));
	}
}
