package com.robonobo.gui.frames;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.Platform;

/**
 * Writes everything from the log4j root logger into a swing frame 
 * @author macavity
 * 
 */
public class Log4jMonitorFrame extends JFrame {
	int idealTextSize = 32768;
	JTextArea textArea;
	TextAreaAppender appender;

	public Log4jMonitorFrame(RobonoboFrame frame) {
		setTitle("robonobo Log");
		setIconImage(RobonoboFrame.getRobonoboIconImage());
		if(Platform.getPlatform().shouldSetMenuBarOnDialogs())
			setJMenuBar(Platform.getPlatform().getMenuBar(frame));
		appender = new TextAreaAppender();
		PatternLayout logLayout = new PatternLayout();
		logLayout.setConversionPattern("%d{HH:mm:ss:SSS} [%-5p] %-16t : %m (%C)%n");
		appender.setLayout(logLayout);
		Logger.getRootLogger().addAppender(appender);		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setRows(20);
		textArea.setColumns(150);
		textArea.setFont(new Font("Monospaced", 0, 12));
		getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		pack();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Logger.getRootLogger().removeAppender(appender);
			}
		});
	}

	class TextAreaAppender extends AppenderSkeleton {
		@Override
		protected void append(LoggingEvent e) {
			final String msg = layout.format(e);
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					textArea.append(msg);
					// Make sure the last line is always visible
					textArea.setCaretPosition(textArea.getDocument().getLength());
					int maxExcess = idealTextSize / 2;
					int excess = textArea.getDocument().getLength() - idealTextSize;
					if (excess >= maxExcess)
						
						textArea.replaceRange("", 0, excess);
				}
			});
		}

		@Override
		public void close() {
		}

		@Override
		public boolean requiresLayout() {
			return true;
		}
	}

	public int getIdealTextSize() {
		return idealTextSize;
	}

	public void setIdealTextSize(int maxTextSize) {
		this.idealTextSize = maxTextSize;
	}
}
