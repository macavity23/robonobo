package com.robonobo.gui.components;

/**
 * We keep the chosen left sidebar item highlighted even when it is not the 'selected component', which changes at Swing's twisted whim
 * 
 * @author macavity
 * 
 */
public interface LeftSidebarComponent {
	/**
	 * Some other component has been selected, and we need to stop being so
	 */
	public void relinquishSelection();
}
