package com.robonobo.gui;

import static com.robonobo.gui.GuiUtil.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.metadata.PlaylistHandler;
import com.robonobo.gui.frames.RobonoboFrame;

public class UriHandler {
	RobonoboFrame frame;
	Log log = LogFactory.getLog(getClass());
	
	public UriHandler(RobonoboFrame frame) {
		this.frame = frame;
	}

	public void handle(String uri) {
		Pattern uriPat = Pattern.compile("^rbnb:(\\w+):(.*)$");
		Matcher m = uriPat.matcher(uri);
		if (m.matches()) {
			log.info("Opening URI "+uri);
			String objType = m.group(1);
			String objId = m.group(2);
			if (objType.equalsIgnoreCase("focus")) {
				// Do nothing, arg handler will bring us to front anyway
				return;
			}
			if (objType.equalsIgnoreCase("playlist")) {
				final long pId = Long.parseLong(objId, 16);
				frame.getController().getOrFetchPlaylist(pId, new PlaylistHandler() {
					public void success(final Playlist p) {
						runOnUiThread(new CatchingRunnable() {
							public void doRun() throws Exception {
								frame.getLeftSidebar().showPlaylist(p);
							}
						});
					}
					
					public void error(long playlistId, Exception ex) {
					}
				});
				return;
			}
		} else 
			log.error("Received invalid rbnb uri: " + uri);

	}
}
