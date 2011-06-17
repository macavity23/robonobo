package com.robonobo.gui.components.base;

import static com.robonobo.gui.GuiUtil.*;

import java.awt.*;
import java.awt.font.*;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JPanel;

import com.robonobo.gui.GuiUtil;

@SuppressWarnings("serial")
public class LineBreakTextPanel extends JPanel {
	private int start;
	private int end;
	private LineBreakMeasurer lineBreakMeasurer;

	public LineBreakTextPanel(String text, Font font, Dimension startSz) {
		AttributedString aStr = new AttributedString(text);
		aStr.addAttribute(TextAttribute.FONT, font);
		AttributedCharacterIterator attributedCharacterIterator = aStr.getIterator();
		start = attributedCharacterIterator.getBeginIndex();
		end = attributedCharacterIterator.getEndIndex();
		setFont(font);
		lineBreakMeasurer = new LineBreakMeasurer(attributedCharacterIterator, new FontRenderContext(font.getTransform(), true, false));
		setPreferredSize(startSz);
	}

	public void paintComponent(Graphics gr) {
		makeTextLookLessRubbish(gr);
		Graphics2D g = (Graphics2D) gr;
		Dimension size = getSize();
		float width = (float) size.width;
		float x, y = 0;
		lineBreakMeasurer.setPosition(start);
		while (lineBreakMeasurer.getPosition() < end) {
			TextLayout textLayout = lineBreakMeasurer.nextLayout(width);
			y += textLayout.getAscent();
			x = 0;
			textLayout.draw(g, x, y);
			y += textLayout.getDescent() + textLayout.getLeading();
		}
	}
}
