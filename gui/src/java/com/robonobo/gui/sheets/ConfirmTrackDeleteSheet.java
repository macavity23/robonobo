package com.robonobo.gui.sheets;

import java.util.List;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.TrackListTableModel;

public class ConfirmTrackDeleteSheet extends ConfirmWithFeckOffSheet {
	private TrackListTableModel model;
	private List<String> sids;

	public ConfirmTrackDeleteSheet(RobonoboFrame frame, TrackListTableModel model, final List<String> sids) {
		super(frame, "Please confirm delete", "Are you sure you want to " + model.longDeleteTracksDesc()+"?", "Warn when deleting tracks", true, "Delete");
		this.model = model;
		this.sids = sids;
	}

	@Override
	protected void confirmed(final boolean feckOffSelected) {
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				if (!feckOffSelected) {
					frame.guiCfg.setConfirmTrackDelete(false);
					frame.ctrl.saveConfig();
				}
				model.deleteTracks(sids);
			}
		});
		setVisible(false);
	}
}
