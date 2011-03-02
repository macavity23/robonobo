package net.robonobo.plugin.mp3.parser;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import com.robonobo.plugin.mp3.Frame;
import com.robonobo.plugin.mp3.Mp3Parser;

import junit.framework.TestCase;

public class Mp3ParserTest extends TestCase{
	Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void setUp() throws Exception {
		BasicConfigurator.configure();
	}
	
	public void testMp3Parsing() throws Exception  {
		FileChannel chan = new FileInputStream(new File("06 Juju Mama.mp3")).getChannel();
		Mp3Parser p = new Mp3Parser(chan);
		Frame f = null;
		do {
			f = p.nextFrame();
			log.info(f);
		}
		while(f!=null);
	}
}
