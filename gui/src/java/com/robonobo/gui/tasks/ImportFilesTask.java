package com.robonobo.gui.tasks;

import static com.robonobo.gui.GuiUtil.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.model.Stream;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.sheets.TaskProgressSheet;

public class ImportFilesTask extends Task {
	private List<File> files = new ArrayList<File>();
	private RobonoboFrame frame;

	public ImportFilesTask(RobonoboFrame frame, List<File> files) {
		this.frame = frame;
		this.files = files;
		title = "Importing " + files.size() + " files";
	}

	@Override
	public void runTask() throws Exception {
		statusText = "Reading file details";
		fireUpdated();
		final TaskProgressSheet tps = new TaskProgressSheet(frame, statusText, "Reading", files.size(), true);
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.showSheet(tps);
			}
		});
		final List<Stream> sl = new ArrayList<Stream>();
		int i = 0;
		for (File f : files) {
			Stream s = frame.ctrl.getStream(f);
			sl.add(s);
			tps.setProgress(++i);
		}
		final ChooseImportsSheet cfs = new ChooseImportsSheet(frame, files, sl, this);
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				if (tps.isVisible())
					tps.setVisible(false);
				frame.showSheet(cfs);
			}
		});
	}

	protected void streamsAdded(List<String> streamIds) {
		// Default impl does nothing
	}
}
