package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class DeletePlaylistSheet extends Sheet {
	Playlist p;
	Log log = LogFactory.getLog(getClass());
	private RButton delBtn;
	
	public DeletePlaylistSheet(RobonoboFrame rFrame, Playlist pl) {
		super(rFrame);
		this.p = pl;
		double[][] cellSizen = { {10, TableLayout.FILL, 100, 5, 100, 10}, { 10, 25, 10, TableLayout.FILL, 10, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		RLabel title = new RLabel14B("Delete playlist '"+p.getTitle()+"'");
		add(title, "1,1,4,1,LEFT,CENTER");
		
		RLabel blurb = new RLabel12("<html><center>Are you sure you want to delete this playlist?</center></html>");
		add(blurb, "1,3,4,3");
		
		delBtn = new RGlassButton("Delete");
		delBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.leftSidebar.selectMyMusic();
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						frame.ctrl.deletePlaylist(p);
					}
				});
				DeletePlaylistSheet.this.setVisible(false);
			}
		});
		add(delBtn, "2,5");
		RButton cancelBtn = new RRedGlassButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DeletePlaylistSheet.this.setVisible(false);
			}
		});
		add(cancelBtn, "4,5");
	}
	
	@Override
	public void onShow() {
	}
	
	@Override
	public JButton defaultButton() {
		return delBtn;
	}
}
