package com.robonobo.plugin.mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileFormat;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import com.robonobo.common.pageio.paginator.EqualSizeFilePaginator;
import com.robonobo.common.pageio.paginator.Paginator;
import com.robonobo.core.RobonoboInstance;
import com.robonobo.core.api.AudioPlayer;
import com.robonobo.core.api.Robonobo;
import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.spi.FormatSupportProvider;


public class Mp3FormatSupportProvider implements FormatSupportProvider {
	private static final int PAGE_SIZE = 32*1024;
	Robonobo robonobo;
	
	public void init(Robonobo r) {
		this.robonobo = r;
	}
	
	public RobonoboInstance getRobonobo() {
		return (RobonoboInstance)robonobo;
	}
	
	public String getFormatName() {
		return "MP3 Audio";
	}
	
	public String getMimeType() {
		return "audio/mpeg";
	}
	
	public String getDefaultFileExtension() {
		return "mp3";
	}
	
	public String[] getSupportedFileExtensions() {
		return new String[] {"mp3"};
	}
	
	public boolean supportsBroadcast() {
		return true;
	}
	
	public boolean supportsReception() {
		return true;
	}
	
	public Paginator getPaginator() {
		return new Mp3Paginator();
	}
	
	public Stream getStreamForFile(File f) throws IOException {
		AudioFileFormat fileFormat;
		try {
			MpegAudioFileReader reader = new MpegAudioFileReader();
			fileFormat = reader.getAudioFileFormat(f);
//			fileFormat = AudioSystem.getAudioFileFormat(f);
		} catch (UnsupportedAudioFileException e) {
			throw new IOException("File "+f.getAbsolutePath()+" does not appear to be an mp3 file");
		}
		if(!(fileFormat instanceof MpegAudioFileFormat))
			throw new IOException("File "+f.getAbsolutePath()+" does not appear to be an mp3 file");
		Stream s = new Stream();
		s.setMimeType(getMimeType());
		s.setSize(f.length());
		Map<String, Object> props = fileFormat.properties();
		s.setDuration((getLongProp(props, "duration")) / 1000); // mp3 duration is in microsecs
		String title = (getStringProp(props, "title")).trim();
		if(title.length() == 0)
			title = getTitleFromFileName(f);
		s.setTitle(title);
		String artist = (getStringProp(props, "author")).trim();
		if(artist.length() == 0)
			artist = "Unknown Artist";
		s.setAttrValue("artist", artist);
		String album = (getStringProp(props, "album")).trim();
		if(album.length() == 0)
			album = "Unknown Album";
		s.setAttrValue("album", album);
		s.setDescription((getStringProp(props, "comment")).trim());
		s.setAttrValue("year", (getStringProp(props, "date")).trim());
		s.setAttrValue("track", (getStringProp(props, "mp3.id3tag.track")).trim());
		return s;
	}
	
	private String getTitleFromFileName(File f) {
		Pattern titlePat = Pattern.compile("^(.*)\\..*?$");
		Matcher m = titlePat.matcher(f.getName());
		if(m.matches())
			return m.group(1);
		else
			return "Untitled Track";
			
	}

	public void paginate(File f, PageBuffer pageBuf) throws IOException {
		FileChannel fc = new FileInputStream(f).getChannel();
		new EqualSizeFilePaginator(PAGE_SIZE, f.length(), 0).paginate(fc, pageBuf);
	}

	public AudioPlayer getAudioPlayer(Stream s, PageBuffer pb, ThreadPoolExecutor ex) {
		return new Mp3AudioPlayer(s, pb, ex);
	}
	
	private String getStringProp(Map<String, Object> props, String propName) {
		String result = (String) props.get(propName);
		if(result == null)
			return "";
		return result;
	}
	
	private Long getLongProp(Map<String, Object> props, String propName) {
		Long result = (Long) props.get(propName);
		if(result == null)
			return 0l;
		return result;
	}
}
