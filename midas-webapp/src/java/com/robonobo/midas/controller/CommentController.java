package com.robonobo.midas.controller;

import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.proto.CoreApi.CommentMsg;
import com.robonobo.core.api.proto.CoreApi.CommentMsgList;
import com.robonobo.midas.model.*;

@Controller
public class CommentController extends BaseController {
	@RequestMapping(value = "/comment/byid/{commentId}", method = RequestMethod.DELETE)
	public void deleteComment(@PathVariable("commentId") String commentIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		long commentId = Long.parseLong(commentIdStr);
		MidasComment c = midas.getComment(commentId);
		if (c == null) {
			send404(req, resp);
			return;
		}
		// They can only delete the comment if they are the user who originally made it, or else an owner of the resource the comment is on
		if (c.getUserId() != u.getUserId()) {
			Matcher m = Comment.RESOURCE_ID_PAT.matcher(c.getResourceId());
			if(!m.matches())
				throw new Errot();
			if(m.group(1).equals("playlist")) {
				long plId = Long.parseLong(m.group(2));
				MidasPlaylist p = midas.getPlaylistById(plId);
				if(!p.getOwnerIds().contains(u.getUserId())) {
					send401(req, resp);
					return;
				}
			} else if(m.group(1).equals("library")) {
				long libUid = Long.parseLong(m.group(2));
				if(libUid != u.getUserId()) {
					send401(req, resp);
					return;
				}
			} else 
				throw new Errot();
		}
		midas.deleteComment(c);
	}

	@RequestMapping(value = "/comments/{itemType}/{itemId}", method = RequestMethod.GET)
	public void getAllComments(@PathVariable("itemType") String itemType, @PathVariable("itemId") String itemIdStr,
			@RequestParam(value = "since", required = false) String sinceStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MidasUser u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		long itemId = Long.parseLong(itemIdStr);
		Date since = null;
		if(sinceStr != null)
			since = new Date(Long.parseLong(sinceStr));
		List<MidasComment> cl;
		if(itemType.equalsIgnoreCase("playlist")) {
			MidasPlaylist p = midas.getPlaylistById(itemId);
			if (p.getVisibility().equals(Playlist.VIS_ME)) {
				if (!p.getOwnerIds().contains(u.getUserId())) {
					send401(req, resp);
					return;
				}
			} else if (p.getVisibility().equals(Playlist.VIS_FRIENDS)) {
				if (!p.getOwnerIds().contains(u.getUserId())) {
					boolean allowed = false;
					for (long ownerId : p.getOwnerIds()) {
						MidasUser owner = midas.getUserById(ownerId);
						if (owner.getFriendIds().contains(u.getUserId())) {
							allowed = true;
							break;
						}
					}
					if (!allowed) {
						send401(req, resp);
						return;
					}
				}
			}
			cl = midas.getCommentsForPlaylist(itemId, since);
		} else if(itemType.equalsIgnoreCase("library")) {
			if (u.getUserId() != itemId) {
				MidasUser libraryU = midas.getUserById(itemId);
				if (!libraryU.getFriendIds().contains(itemId)) {
					send401(req, resp);
					return;
				}
			}
			cl = midas.getCommentsForLibrary(itemId, since);
		} else {
			send404(req, resp);
			return;
		}
		CommentMsgList.Builder b = CommentMsgList.newBuilder();
		for(MidasComment c : cl) {
			b.addComment(c.toMsg());
		}
		writeToOutput(b.build(), resp);
	}

	@RequestMapping(value = "/comment/{itemType}/{itemId}", method = RequestMethod.PUT)
	public void newComment(@PathVariable("itemType") String itemType, @PathVariable("itemId") String itemIdStr, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		MidasUser u = getAuthUser(req);
		if (u == null) {
			send401(req, resp);
			return;
		}
		long itemId = Long.parseLong(itemIdStr);
		CommentMsg.Builder b = CommentMsg.newBuilder();
		readFromInput(b, req);
		MidasComment c = new MidasComment(b.build());
		c.setUserId(u.getUserId());
		c.setDate(now());
		if (itemType.equalsIgnoreCase("playlist")) {
			MidasPlaylist p = midas.getPlaylistById(itemId);
			if (p.getVisibility().equals(Playlist.VIS_ME)) {
				if (!p.getOwnerIds().contains(u.getUserId())) {
					send401(req, resp);
					return;
				}
			} else if (p.getVisibility().equals(Playlist.VIS_FRIENDS)) {
				if (!p.getOwnerIds().contains(u.getUserId())) {
					boolean allowed = false;
					for (long ownerId : p.getOwnerIds()) {
						MidasUser owner = midas.getUserById(ownerId);
						if (owner.getFriendIds().contains(u.getUserId())) {
							allowed = true;
							break;
						}
					}
					if (!allowed) {
						send401(req, resp);
						return;
					}
				}
			}
			c = midas.newCommentForPlaylist(c, itemId);
		} else if (itemType.equalsIgnoreCase("library")) {
			if (u.getUserId() != itemId) {
				MidasUser libraryU = midas.getUserById(itemId);
				if (!libraryU.getFriendIds().contains(u.getUserId())) {
					send401(req, resp);
					return;
				}
			}
			c = midas.newCommentForLibrary(c, itemId);
		} else {
			send404(req, resp);
			return;
		}
		writeToOutput(c.toMsg(), resp);
	}
}
