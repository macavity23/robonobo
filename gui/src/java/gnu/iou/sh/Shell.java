/* $Id: shell.java,v 1.2 2002/08/01 04:17:15 comoc Exp $ */

/* 
 *  `gnu.iou' I/O buffers and utilities.
 *  Copyright (C) 1998, 1999, 2000, 2001, 2002 John Pritchard.
 *
 *  This program is free software; you can redistribute it or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package gnu.iou.sh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

/**
 * An extensible interpreter pane requires a plugin for an interpreter.
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public class Shell extends JPanel implements Runnable {

	//

	/**
	 * The shell will call on an interpretor with this interface.
	 * 
	 * @author John Pritchard
	 */
	public static interface Plugin {

		/**
		 * String displayed in the UI, under the console, with the interpreter's
		 * name and version.
		 */
		public String userVersion();

		/**
		 * The interpreter handles I/O on this interface as if on the command
		 * line, until this method throws an exception.
		 * 
		 * @param stdin
		 *            GUI console input.
		 * 
		 * @param stdout
		 *            GUI console output.
		 * 
		 * @param stderr
		 *            GUI console output.
		 */
		public void console(DataInputStream stdin, PrintStream stdout, PrintStream stderr) throws IOException;

		/**
		 * Called with an exception from the "console" method. If the "console"
		 * method returns, interpretation continues. However, if it throws an
		 * exception, this method is called with that exception to verify that
		 * the shell interpretation thread should exit.
		 * 
		 * @param exc
		 *            Exception thrown by the console.
		 * 
		 * @param stdout
		 *            GUI console output.
		 * 
		 * @param stderr
		 *            GUI console output.
		 * 
		 * @return Whether to continue the shell intepretation thread or return.
		 */
		public boolean exception(Exception exc, PrintStream stdout, PrintStream stderr);

	}

	//

	/**
	 * Instantiate plugin dynamically from classname. The class has a public,
	 * simple (no args) constructor.
	 * 
	 * @param classname
	 *            Fully qualified name of a class implementing the <code>`shell.plugin'</code>
	 *            interface.
	 */
	protected final static Plugin create(String classname) {
		try {
			Class cla = Class.forName(classname);

			return (Plugin) cla.newInstance();
		} catch (ClassNotFoundException cnf) {
			throw new IllegalArgumentException("Plugin class not found (" + classname + ").");

		} catch (ClassCastException ccx) {
			throw new IllegalArgumentException("Class not a `shell.plugin' (" + classname + ").");
		} catch (InstantiationException insx) {
			throw new IllegalArgumentException("Plugin class is abstract (" + classname + ").");
		} catch (IllegalAccessException ilac) {
			throw new IllegalArgumentException("Plugin constructor is not public (" + classname + ").");
		}
	}

	//

	private Plugin interp;

	private DataInputStream stdin;

	private PrintStream stdout;

	private PrintStream stderr;

	private Thread actor = null;

	protected JLabel status = new JLabel();

	private final ShellTextArea window;

	/**
	 * Construct a new GUI for the interpreter plugin, no border.
	 * 
	 * @param classname
	 *            Interpreter plugin class name.
	 */
	public Shell(String classname) {

		this(null, create(classname), 0, 0);
	}

	/**
	 * Construct a new GUI for the interpreter plugin, no border.
	 * 
	 * @param interp
	 *            Interpreter plugin.
	 */
	public Shell(Plugin interp) {

		this(null, interp, 0, 0);
	}

	/**
	 * Construct a new GUI for the interpreter plugin with a border.
	 * 
	 * @param interp
	 *            Interpreter plugin.
	 * 
	 * @param border
	 *            Set this border on this pane.
	 */
	public Shell(Border border, Plugin interp) {

		this(border, interp, 0, 0);
	}

	/**
	 * Construct a new GUI for the interpreter plugin with fixed dimensions, no
	 * border.
	 * 
	 * @param interp
	 *            Interpreter plugin.
	 * 
	 * @param rows
	 *            Number of rows in text area.
	 * 
	 * @param columns
	 *            Number of columns in text area.
	 */
	public Shell(Plugin interp, int rows, int columns) {

		this(null, interp, rows, columns);
	}

	/**
	 * Construct a new GUI for the interpreter plugin with fixed dimensions and
	 * an optional border.
	 * 
	 * @param border
	 *            Optional border around this panel.
	 * 
	 * @param interp
	 *            Interpreter plugin.
	 * 
	 * @param rows
	 *            Number of rows in text area.
	 * 
	 * @param columns
	 *            Number of columns in text area.
	 */
	public Shell(Border border, Plugin interp, int rows, int columns) {
		super();

		if (null == interp)
			throw new IllegalArgumentException("Require interpreter.");
		else
			this.interp = interp;

		if (null != border)
			setBorder(border);

		this.window = new ShellTextArea(rows, columns);

		this.stdin = this.window.getStdin();

		this.stdout = this.window.getStdout();

		this.stderr = this.window.getStderr();

		setLayout(new BorderLayout());

		JScrollPane scroller = new JScrollPane(this.window);

		add(scroller, BorderLayout.CENTER);

		this.status.setFont(new Font("SansSerif", Font.PLAIN, 10));

		add(this.status, BorderLayout.SOUTH);

	}

	/**
	 * The interpreter's version string for the UI.
	 */
	public String userVersion() {

		return this.interp.userVersion();
	}

	/**
	 * Send the argument statement to be evaluated in the interpreter.
	 */
	public void println(String stmt) throws IOException {
		if (null != stmt)
			this.window.println(stmt);
	}

	/**
	 * Run the interpreter.
	 */
	public void run() {

		status_reset();

		while (true) {
			try {
				this.interp.console(stdin, stdout, stderr);

				status_reset();
			} catch (Exception exc) {

				if (this.interp.exception(exc, stdout, stderr))

					continue;
				else
					return;
			}
		}
	}

	/**
	 * Get status label text.
	 */
	public String status() {

		return this.status.getText();
	}

	/**
	 * Set status label text.
	 */
	public void status(String msg) {
		this.status.setText(msg);
	}

	/**
	 * Set status label to interp user version string.
	 */
	public void status_reset() {
		this.status.setText(this.interp.userVersion());
	}

	/**
	 * Create and start thread for this shell.
	 */
	public void start() {

		if (null == this.actor) {

			this.actor = new Thread(this);

			this.actor.start();
		}
	}

	public void stop() {

		if (null != this.actor) {

			this.actor.stop();

			this.actor = null;
		}
	}

	public void suspend() {

		if (null != this.actor)
			this.actor.suspend();
	}

	public void resume() {

		if (null != this.actor)
			this.actor.resume();
	}

	//

	/**
	 * Command line function creates a frame UI. Requires "plugin" classname
	 * argument.
	 */
	public final static void main(String[] argv) {
		try {
			if (null == argv || 1 != argv.length)
				throw new IllegalArgumentException();

			Shell sh = new Shell(argv[0]);

			sh.setPreferredSize(new Dimension(500, 400));

			JFrame frame = new JFrame(sh.userVersion());

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			frame.setContentPane(sh);

			frame.pack();

			frame.show();
		} catch (IllegalArgumentException ilarg) {
			String msg = ilarg.getMessage();

			if (null == msg) {
				System.err.println();
				System.err.println(" Usage: shell classname");
				System.err.println();
				System.err.println("   Instantiate the named class as a ");
				System.err.println("   shell plugin interpreter.");
				System.err.println();
			} else
				System.err.println("Error: " + msg);

			System.exit(1);
		}
	}
}
