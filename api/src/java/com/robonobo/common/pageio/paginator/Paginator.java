package com.robonobo.common.pageio.paginator;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import com.robonobo.mina.external.buffer.PageBuffer;


/**
 * Takes a java.nio channel and figures out where the pages are, putting them into the supplied buffer 
 * @author macavity
 * 
 */
public interface Paginator {
	public void paginate(ByteChannel c, PageBuffer pageBuf) throws IOException;
}
