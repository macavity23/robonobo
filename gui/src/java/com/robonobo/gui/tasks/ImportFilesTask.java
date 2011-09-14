package com.robonobo.gui.tasks;

import static com.robonobo.gui.GuiUtil.*;

import java.io.File;
import java.util.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.StreamWithFile;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.StreamComparator;
import com.robonobo.gui.sheets.TaskProgressSheet;

public class ImportFilesTask extends Task {
	protected List<File> files = new ArrayList<File>();
	protected RobonoboFrame frame;

	public ImportFilesTask(RobonoboFrame frame, List<File> files) {
		this.frame = frame;
		this.files = files;
		if(files != null)
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
		final List<StreamWithFile> sl = new ArrayList<StreamWithFile>();
		int i = 0;
		for (File f : files) {
			if(cancelRequested) {
				cancelConfirmed();
				return;
			}
			Stream s = frame.ctrl.getStream(f);
			StreamWithFile swf = new StreamWithFile();
			swf.copyFrom(s);
			swf.file = f;
			sl.add(swf);
			tps.setProgress(++i);
			statusText = "Reading file "+i+" of "+files.size();
			fireUpdated();
		}
		Collections.sort(sl, new StreamComparator());
		final ChooseImportsSheet cfs = new ChooseImportsSheet(frame, sl, null, this);
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
