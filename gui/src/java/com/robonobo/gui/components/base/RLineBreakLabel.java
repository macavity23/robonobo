package com.robonobo.gui.components.base;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.*;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.Icon;

public class RLineBreakLabel extends RLabel12 {

	public RLineBreakLabel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RLineBreakLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLineBreakLabel(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	public RLineBreakLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLineBreakLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLineBreakLabel(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public void paint(Graphics graphics) {
		Graphics2D g2d = (Graphics2D) graphics;
		FontRenderContext frc = g2d.getFontRenderContext();
		AttributedString as = new AttributedString(getText());
		AttributedCharacterIterator charIter = as.getIterator();
		LineBreakMeasurer measurer = new LineBreakMeasurer(charIter, frc);
		float formatWidth = (float) getSize().width;
		float drawPosY = 0;
		measurer.setPosition(0);
		// Get lines from lineMeasurer until the entire
		// paragraph has been displayed.
		while (measurer.getPosition() < getText().length()) {
			// Retrieve next layout.
			TextLayout layout = measurer.nextLayout(formatWidth);
			// Move y-coordinate by the ascent of the layout.
			drawPosY += layout.getAscent();
			// Compute pen x position. If the paragraph is
			// right-to-left, we want to align the TextLayouts
			// to the right edge of the panel.
			float drawPosX;
			if (layout.isLeftToRight()) {
				drawPosX = 0;
			} else {
				drawPosX = formatWidth - layout.getAdvance();
			}

			// Draw the TextLayout at (drawPosX, drawPosY).
			layout.draw(g2d, drawPosX, drawPosY);

			// Move y-coordinate in preparation for next layout.
			drawPosY += layout.getDescent() + layout.getLeading();
		}
	}
}
