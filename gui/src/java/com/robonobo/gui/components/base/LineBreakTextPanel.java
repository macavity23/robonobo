package com.robonobo.gui.components.base;

import static com.robonobo.gui.GuiUtil.*;

import java.awt.*;
import java.awt.font.*;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LineBreakTextPanel extends JPanel {
	String text;
	private int start;
	private int end;
	private float width;
	private LineBreakMeasurer measurer;

	public LineBreakTextPanel(String text, Font font, int width) {
		this.text = text;
		AttributedString aStr = new AttributedString(text);
		aStr.addAttribute(TextAttribute.FONT, font);
		AttributedCharacterIterator attributedCharacterIterator = aStr.getIterator();
		start = attributedCharacterIterator.getBeginIndex();
		end = attributedCharacterIterator.getEndIndex();
		setFont(font);
		measurer = new LineBreakMeasurer(attributedCharacterIterator, new FontRenderContext(font.getTransform(), true, false));
		// Figure out our height
		int height = 0;
		measurer.setPosition(start);
		this.width = (float) width;
		while (measurer.getPosition() < end) {
			TextLayout layout = getNextLayout();
			height += layout.getAscent() + layout.getDescent() + layout.getLeading() + 2;
		}
		Dimension sz = new Dimension(width, height);
		setPreferredSize(sz);
		setMaximumSize(sz);
	}

	public void paintComponent(Graphics gr) {
		makeTextLookLessRubbish(gr);
		Graphics2D g = (Graphics2D) gr;
		Dimension size = getSize();
		width = (float) size.width;
		float x, y = 0;
		measurer.setPosition(start);
		while (measurer.getPosition() < end) {
			TextLayout textLayout = getNextLayout();
			y += textLayout.getAscent();
			x = 0;
			textLayout.draw(g, x, y);
			// Add 1 to make spacing nicer
			y += textLayout.getDescent() + textLayout.getLeading() + 1;
		}
	}

	private TextLayout getNextLayout() {
		// Make sure we break at \n chars
		int next = measurer.nextOffset(width);
		int limit = next;
		for (int i = measurer.getPosition() + 1; i < next; i++) {
			char c = text.charAt(i);
			if (c == '\n') {
				limit = i;
				break;
			}
		}
		return measurer.nextLayout(width, limit, false);
	}
}
