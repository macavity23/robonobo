package com.robonobo.gui.components.base;

import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.util.NetUtil;
import com.robonobo.gui.RoboFont;

/**
 * Like a JLabel, but may contain hyperlinks, which when clicked will open the platform's default browser
 */
public class HyperlinkPane extends JEditorPane {
	Log log = LogFactory.getLog(getClass());
	
	public HyperlinkPane(String text, Color bgColor) {
		setContentType("text/html");
		setEditable(false);
		setOpaque(false);
		setBorder(null);
		addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
					String url = e.getURL().toString();
					try {
						NetUtil.browse(url);
					} catch (Exception ex) {
						log.error("Error opening hyperlink", ex);
					}
				}
			}
		});
		setText(text);
		setFont(RoboFont.getFont(12, false));
		StyledDocument doc = (StyledDocument) getDocument();
		SimpleAttributeSet bgAttSet = new SimpleAttributeSet();
		StyleConstants.setBackground(bgAttSet, bgColor);
		doc.setParagraphAttributes(0, doc.getLength(), bgAttSet, false);
	}
}
