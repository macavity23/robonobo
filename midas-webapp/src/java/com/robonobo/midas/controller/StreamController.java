package com.robonobo.midas.controller;

import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.robonobo.core.api.proto.CoreApi.StreamMsg;
import com.robonobo.midas.model.MidasStream;
import com.robonobo.midas.model.MidasUser;

@Controller
@RequestMapping("/streams/{streamId}")
public class StreamController extends BaseController {
	@RequestMapping(method=RequestMethod.GET)
	public void getStream(@PathVariable("streamId") String streamId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasStream stream;
		if("test".equals(streamId)) {
			stream = new MidasStream();
			stream.setDescription("A Test stream");
			stream.setTitle("Test");
			stream.setMimeType("audio/mpeg");
			stream.setStreamId("test");
		} else
			stream = midas.getStreamById(streamId);
		if(stream == null) {
			send404(req, resp);
			return;
		}
		log.debug("Returning stream " + stream.getStreamId());
		writeToOutput(stream.toMsg(), resp);
 	}

	@RequestMapping(method=RequestMethod.PUT)
	public void putStream(@PathVariable("streamId") String streamId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser authUser = getAuthUser(req);
		if(authUser == null) {
			send401(req, resp);
			return;
		}
		StreamMsg.Builder smb = StreamMsg.newBuilder();
		readFromInput(smb, req);
		MidasStream stream = new MidasStream(smb.build());
		if(!stream.getStreamId().equals(streamId))
			throw new IOException("streamIds don't match");
		MidasStream currentStream = midas.getStreamById(streamId);
		if(currentStream != null) {
			// TODO Check for fingerprinty stuff - for now just return ok
		} else {
			stream.setPublished(now());
			stream.setModified(now());
			log.info("Creating new stream " + stream.getStreamId());
			midas.saveStream(stream);
		}
	}
}
