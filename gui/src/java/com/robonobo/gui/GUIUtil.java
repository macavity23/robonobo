package com.robonobo.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.robonobo.common.exceptions.SeekInnerCalmException;

public class GUIUtil {
	public static final int DEFAULT_NUM_SHAKES = 10;
	public static final int DEFAULT_SHAKE_FORCE = 5;

	public static ImageIcon createImageIcon(String path, String description) {
		URL imgUrl = GUIUtil.class.getResource(path);
		if (imgUrl == null)
			return null;
		return new ImageIcon(imgUrl, description);
	}

	public static Image getImage(String path) {
		try {
			return ImageIO.read(GUIUtil.class.getResource(path));
		} catch (IOException e) {
			throw new SeekInnerCalmException(e);
		}
	}

	public static void shakeWindow(final Window win, int numShakes, int shakeForce) {
		final Rectangle origRect = win.getBounds();
		for (int i = 0; i < numShakes; i++) {
			int x;
			if (i % 2 == 0)
				x = shakeForce;
			else
				x = -shakeForce;
			win.setBounds(origRect.x + x, origRect.y, origRect.width, origRect.height);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			win.setBounds(origRect); // Reset to original position
		}
	}

	public static void shakeWindow(Window win) {
		GUIUtil.shakeWindow(win, DEFAULT_NUM_SHAKES, DEFAULT_SHAKE_FORCE);
	}

	/** In MS windows, if we don't call this in paintComponent(), text looks like poop */
	public static void makeTextLookLessRubbish(Graphics gr) {
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}
}
