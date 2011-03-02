/* $Id: ta.java,v 1.2 2002/08/01 04:17:15 comoc Exp $ */

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

import gnu.iou.bbi;
import gnu.iou.bbo;
import gnu.iou.bbp;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;

/**
 * An interpreter text area used by shell provides a stream based I/O
 * environment like the command- line.
 * 
 * <p>
 * This object exports pipe streams that can be read and written by an
 * interpreter.
 * 
 * @see Shell
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public class ShellTextArea extends JTextArea implements KeyListener, DocumentListener {
	/**
	 * User reads pipe from GUI
	 */
	private final bbp pipe = new bbp();
	/**
	 * Shell GUI writes pipe
	 */
	private final PrintStream pipo;

	/**
	 * User reads from GUI (reads pipe).
	 */
	private final DataInputStream stdin;
	/**
	 * User writes to GUI using Shell Out.
	 * 
	 * @see to
	 */
	private final PrintStream stdout;
	/**
	 * User writes to GUI using Shell Out.
	 * 
	 * @see to
	 */
	private final PrintStream stderr;

	private int docmark = 0;

	private volatile boolean docupd = true;

	private final Document doc;

	private final Segment segment = new Segment();

	private final Vector history = new Vector();

	private int historx = 0;

	/**
	 * @param rows
	 *            Number of rows text area rows.
	 * 
	 * @param columns
	 *            Number of text area columns.
	 */
	public ShellTextArea(int rows, int columns) {
		super(rows, columns);

		this.doc = super.getDocument();

		this.doc.addDocumentListener(this);

		addKeyListener(this);

		setLineWrap(true);

		setFont(new Font("Monospaced", 0, 12));

		this.pipo = new PrintStream(new bbo(this.pipe));

		this.stdin = new DataInputStream(new bbi(this.pipe));

		this.stdout = new PrintStream(new to(this));

		this.stderr = this.stdout;

	}

	public DataInputStream getStdin() {
		return this.stdin;
	}

	public PrintStream getStdout() {
		return this.stdout;
	}

	public PrintStream getStderr() {
		return this.stderr;
	}

	/**
	 * Send argument string to interpreter.
	 */
	public void println(String str) {

		append(str + "\n");

		pipo.println(str);
	}

	/**
	 * @see java.awt.event.KeyListener
	 */
	public void keyPressed(KeyEvent e) {

		int code = e.getKeyCode();

		if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_LEFT) {

			if (this.docmark == getCaretPosition()) {
				e.consume();
			}
		} else if (code == KeyEvent.VK_HOME) {

			int caretPos = getCaretPosition();

			if (caretPos == this.docmark) {

				e.consume();

			} else if (caretPos > this.docmark) {

				if (!e.isControlDown()) {

					if (e.isShiftDown())

						moveCaretPosition(this.docmark);
					else
						setCaretPosition(this.docmark);

					e.consume();
				}
			}
		} else if (code == KeyEvent.VK_ENTER) {

			returnPressed();

			e.consume();

		} else if (code == KeyEvent.VK_UP) {

			this.historx--;

			if (0 <= this.historx) {

				if (this.history.size() <= this.historx)

					this.historx = this.history.size() - 1;

				if (0 <= this.historx) {

					String str = (String) this.history.elementAt(this.historx);

					int len = this.doc.getLength();

					replaceRange(str, this.docmark, len);

					int caretPos = this.docmark + str.length();

					select(caretPos, caretPos);

				} else
					this.historx++;

			} else
				this.historx++;

			e.consume();
		} else if (code == KeyEvent.VK_DOWN) {

			int caretPos = this.docmark;

			if (this.history.size() > 0) {

				this.historx++;

				if (this.historx < 0)
					this.historx = 0;

				int len = this.doc.getLength();

				if (this.historx < this.history.size()) {

					String str = (String) this.history.elementAt(this.historx);

					replaceRange(str, this.docmark, len);

					caretPos = this.docmark + str.length();
				} else {
					this.historx = this.history.size();

					replaceRange("", this.docmark, len);
				}
			}
			select(caretPos, caretPos);

			e.consume();
		}
	}

	/**
	 * @see java.awt.event.KeyListener
	 */
	public void keyTyped(KeyEvent e) {

		int keyChar = e.getKeyChar();

		if (keyChar == 0x8) { // KeyEvent.VK_BACK_SPACE

			if (this.docmark == getCaretPosition())

				e.consume();
		} else if (getCaretPosition() < this.docmark) {

			setCaretPosition(this.docmark);
		}
	}

	/**
	 * @see java.awt.event.KeyListener
	 */
	public synchronized void keyReleased(KeyEvent e) {
	}

	/**
	 * @see javax.swing.text.JTextComponent
	 */
	public void select(int start, int end) {

		requestFocus();

		super.select(start, end);
	}

	/**
	 * @see javax.swing.event.DocumentListener
	 */
	public synchronized void insertUpdate(DocumentEvent evt) {

		if (evt.getOffset() < this.docmark)

			this.docmark += evt.getLength();
	}

	/**
	 * @see javax.swing.event.DocumentListener
	 */
	public void removeUpdate(DocumentEvent evt) {

		int off = evt.getOffset();

		if (off < this.docmark) {

			int len = evt.getLength();

			if ((off + len) <= this.docmark)

				this.docmark -= len;
			else
				this.docmark = off;
		}
	}

	/**
	 * @see javax.swing.event.DocumentListener
	 */
	public void changedUpdate(DocumentEvent evt) {
	}

	/**
	 * Used from Shell Out.
	 * 
	 * @see to#write(int)
	 */
	protected synchronized void _usr_write(int b) throws IOException {

		char[] cary = new char[] { (char) (b & 0xff) };

		String txt = new String(cary);
		try {
			this.doc.insertString(this.doc.getLength(), txt, null);

			this.docmark = this.doc.getLength();

			setCaretPosition(this.docmark);
		} catch (BadLocationException ignored) {
			ignored.printStackTrace();
		}
	}

	/**
	 * Used from Shell Out.
	 * 
	 * @see to#write(byte[],int,int)
	 */
	protected synchronized void _usr_write(byte b[], int off, int len) throws IOException {

		String txt = new String(b, 0, off, len);

		try {
			this.doc.insertString(this.doc.getLength(), txt, null);

			this.docmark = this.doc.getLength();

			setCaretPosition(this.docmark);
		} catch (BadLocationException ignored) {
			ignored.printStackTrace();
		}
	}

	/**
	 * Called from "key pressed" event.
	 */
	private synchronized void returnPressed() {

		int len = this.doc.getLength();

		try {
			this.doc.getText(this.docmark, len - this.docmark, this.segment);

			if (0 < this.segment.count)

				this.history.addElement(this.segment.toString());

			this.historx = this.history.size();

			String str = new String(this.segment.array, this.segment.offset, this.segment.count);

			pipo.println(str);

			append("\n");

			this.docmark = doc.getLength();

		} catch (BadLocationException ignored) {

			ignored.printStackTrace();
		}
	}
}
