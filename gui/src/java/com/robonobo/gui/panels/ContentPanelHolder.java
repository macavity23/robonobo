package com.robonobo.gui.panels;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;

@SuppressWarnings("serial")
public class ContentPanelHolder extends JPanel {
	private Map<String, ContentPanel> panels = new HashMap<String, ContentPanel>();
	String currentPanel;
	Log log = LogFactory.getLog(getClass());
	
	ContentPanelHolder() {
		setLayout(new CardLayout());
	}

	void addContentPanel(String name, ContentPanel panel) {
		add(panel, name);
		panels.put(name, panel);
		if(name.equals(currentPanel))
			selectContentPanel(name);
	}

	ContentPanel getContentPanel(String name) {
		return panels.get(name);
	}
	
	ContentPanel currentContentPanel() {
		if(currentPanel == null)
			return null;
		return panels.get(currentPanel);
	}
	
	String currentPanelName() {
		return currentPanel;
	}
	
	ContentPanel removeContentPanel(String name) {
		ContentPanel panel = panels.remove(name);
		if(panel != null)
			remove(panel);
		return panel;
	}
	
	void selectContentPanel(final String panelName) {
		if(!panels.containsKey(panelName)) {
			log.error("Tried to select non-existent main panel: "+panelName);
			return;
		}
		final CardLayout cl = (CardLayout) getLayout();
		currentPanel = panelName;
		if(SwingUtilities.isEventDispatchThread())
			cl.show(this, panelName);
		else {
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					cl.show(ContentPanelHolder.this, panelName);
				}
			});
		}
	}
}
