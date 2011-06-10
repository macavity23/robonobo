package com.robonobo.core.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.FileUtil;
import com.robonobo.core.MD5StreamIdGenerator;
import com.robonobo.core.api.StreamIdGenerator;
import com.robonobo.core.api.model.Stream;
import com.robonobo.spi.FormatSupportProvider;

public class FormatService extends AbstractService {
	private Log log = LogFactory.getLog(getClass());
	private List<FormatSupportProvider> fsps = new ArrayList<FormatSupportProvider>();
	private StreamIdGenerator idGenerator = new MD5StreamIdGenerator();

	public FormatService() {
	}

	@Override
	public void startup() throws Exception {
		loadFSPs();
	}

	@Override
	public void shutdown() throws Exception {
		// Do nothing
	}

	public String getName() {
		return "Format service";
	}

	public String getProvides() {
		return "core.format";
	}

	private void loadFSPs() {
		loadFSPsFromConfig();
	}

	private void loadFSPsFromConfig() {
		String[] classNames = getRobonobo().getConfig().getFormatSupportProviders().split(",");
		for (String className : classNames) {
			try {
				FormatSupportProvider fsp = (FormatSupportProvider) Class.forName(className).newInstance();
				fsp.init(rbnb);
				addFsp(fsp);
			} catch (Exception e) {
				log.error("Error loading formatsupportprovider '"+className+"'", e);
			}
		}

	}

	private void addFsp(FormatSupportProvider newFsp) {
		// Don't add it if we've got one already
		Class<?> newFspClass = newFsp.getClass();
		for (FormatSupportProvider fsp : fsps) {
			if(newFspClass.equals(fsp.getClass()))
				return;
		}
		fsps.add(newFsp);
	}

	public FormatSupportProvider getFormatSupportProvider(String mimeType) {
		for (FormatSupportProvider fsp : fsps) {
			if (fsp.getMimeType().equals(mimeType) && fsp.supportsReception())
				return fsp;
		}
		return null;
	}

	public String getMimeTypeForFile(File f) {
		return getFspForExtension(FileUtil.getFileExtension(f)).getMimeType();
	}

	public Stream getStreamForFile(File f) throws IOException {
		Stream s = getFspForExtension(FileUtil.getFileExtension(f)).getStreamForFile(f);
		s.setStreamId(idGenerator.generateStreamId(f));
		return s;
	}

	private FormatSupportProvider getFspForExtension(String extension) {
		for (FormatSupportProvider fsp : fsps) {
			for(int i = 0; i < fsp.getSupportedFileExtensions().length; i++) {
				return fsp;
			}
		}
		// No match, so find the default one
		for (FormatSupportProvider fsp : fsps) {
			for(int i = 0; i < fsp.getSupportedFileExtensions().length; i++) {
				if("*".equalsIgnoreCase(fsp.getSupportedFileExtensions()[i]))
					return fsp;
			}
		}
		throw new Errot();
	}

}
