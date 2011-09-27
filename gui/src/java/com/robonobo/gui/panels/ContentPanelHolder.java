package com.robonobo.gui.panels;

import java.awt.CardLayout;
import java.util.*;
import java.util.Map.Entry;

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
	
	void removeContentPanels(String prefix) {
		Iterator<Entry<String, ContentPanel>> it = panels.entrySet().iterator();
		while(it.hasNext()) {
			if(it.next().getKey().startsWith(prefix))
				it.remove();
		}
	}
	
	void selectContentPanel(final String panelName) {
		if(!panels.containsKey(panelName)) {
			log.error("Tried to select non-existent main panel: "+panelName);
			return;
		}
		currentPanel = panelName;
		if(SwingUtilities.isEventDispatchThread())
			showPanel(panelName);
		else {
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					showPanel(panelName);
				}
			});
		}
	}

	private void showPanel(final String panelName) {
		CardLayout cl = (CardLayout) getLayout();
		cl.show(this, panelName);
		getContentPanel(panelName).setVisible(false);
		getContentPanel(panelName).setVisible(true);
	}
}
