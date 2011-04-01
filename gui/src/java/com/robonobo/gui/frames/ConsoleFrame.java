package com.robonobo.gui.frames;

import gnu.iou.sh.Shell;
import gnu.iou.sh.Shell.Plugin;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import javax.swing.JFrame;

import com.robonobo.console.RobonoboConsole;
import com.robonobo.core.Platform;

public class ConsoleFrame extends JFrame {
	private RobonoboFrame frame;
	private Thread thread;

	public ConsoleFrame(RobonoboFrame frame) throws HeadlessException {
		this.frame = frame;
		if(Platform.getPlatform().shouldSetMenuBarOnDialogs())
			setJMenuBar(Platform.getPlatform().getMenuBar(frame));
		setTitle("robonobo console");
		setIconImage(RobonoboFrame.getRobonoboIconImage());
		Shell shell = new Shell(null, new ConsoleShellPlugin(frame), 20, 150);
		getContentPane().add(shell, BorderLayout.CENTER);
		pack();
		thread = new Thread(shell);
		thread.start();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				thread.interrupt();
			}
		});
	}
	
	class ConsoleShellPlugin implements Plugin {
		RobonoboFrame frame;
		
		public ConsoleShellPlugin(RobonoboFrame frame) {
			this.frame = frame;
		}

		public void console(DataInputStream stdin, PrintStream stdout, PrintStream stderr) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
			PrintWriter writer = new PrintWriter(stdout);
			RobonoboConsole console = new RobonoboConsole(frame.getController(), reader, writer);
			try {
				console.doRun();
			} catch (Exception e) {
				throw new IOException("Caught "+e.getClass().getName()+": "+e.getMessage());
			}
		}

		public boolean exception(Exception exc, PrintStream stdout, PrintStream stderr) {
			// Don't continue execution, just exit
			return false;
		}

		public String userVersion() {
			return "Robonobo Console v"+frame.getController().getVersion();
		}
	}
}
