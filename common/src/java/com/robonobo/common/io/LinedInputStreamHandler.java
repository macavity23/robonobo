package com.robonobo.common.io;

import java.io.*;

import com.robonobo.common.concurrent.CatchingRunnable;

public abstract class LinedInputStreamHandler extends CatchingRunnable {
	InputStream is;

	public LinedInputStreamHandler(InputStream is) {
		this.is = is;
	}

	@Override
	public void doRun() throws Exception {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rdr.readLine()) != null) {
				handleLine(line);
			}
		} catch (IOException e) {
			handleException(e);
		}
	}

	public abstract void handleLine(String line);

	protected void handleException(IOException e) {
		e.printStackTrace();
	}
}
