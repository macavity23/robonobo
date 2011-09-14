package com.robonobo.gui.tasks;

import static com.robonobo.common.util.FileUtil.*;
import static com.robonobo.gui.GuiUtil.*;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.StreamWithFile;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.StreamComparator;
import com.robonobo.gui.sheets.TaskProgressSheet;

public class ImportITunesTask extends ImportFilesTask {
	public ImportITunesTask(RobonoboFrame f) {
		super(f, null);
		title = "Importing from iTunes";
	}

	@Override
	public void runTask() throws Exception {
		log.info("Running import iTunes task");
		statusText = "Reading list of files from iTunes";
		completion = 0;
		fireUpdated();
		final TaskProgressSheet tps = new TaskProgressSheet(frame, "Reading file details from iTunes", "Reading", 0, true);
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				tps.progressBar.setString(statusText);
				frame.showSheet(tps);
			}
		});
		FileFilter mp3Filter = new FileFilter() {
			public boolean accept(File f) {
				return "mp3".equalsIgnoreCase(getFileExtension(f));
			}
		};
		files = frame.ctrl.getITunesLibrary(mp3Filter);
		statusText = "Reading playlists from iTunes";
		fireUpdated();
		if (tps.isVisible()) {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					tps.total = files.size();
					tps.progressBar.setMaximum(files.size());
					tps.progressBar.setString(statusText);
				}
			});
		}
		Map<String, List<File>> plMap = frame.ctrl.getITunesPlaylists(mp3Filter);
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
		final ChooseImportsSheet cfs = new ChooseImportsSheet(frame, sl, plMap, this);
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				if (tps.isVisible())
					tps.setVisible(false);
				frame.showSheet(cfs);
			}
		});
	}
}
