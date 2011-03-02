package com.robonobo.gui;

import static com.robonobo.common.util.ByteUtil.*;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.ByteUtil;

/**
 * robonobo only uses one font family, Bitstream Vera Sans. We can't rely on this being available on the system, so we
 * bundle it in our jar and build it as required. This class keeps track of our instantiated font instances and re-uses
 * them to save ram.
 * 
 * @author macavity
 */
public class RoboFont {
	private static final String BOLD_FONT_PATH = "/VeraBd.ttf";
	private static final String REG_FONT_PATH = "/Vera.ttf";
	static final String FONT_NAME = "Bitstream Vera Sans";
	// static final String FONT_NAME = "Ubuntu";
	static Log log = LogFactory.getLog(RoboFont.class);
	static Font basePlainFont;
	static Font baseBoldFont;
	static Map<Integer, Font> derivedPlainFonts;
	static Map<Integer, Font> derivedBoldFonts;

	static {
		derivedPlainFonts = new HashMap<Integer, Font>();
		derivedBoldFonts = new HashMap<Integer, Font>();

		// See if our font is available to us from the system
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Set<String> fontNames = new HashSet<String>(Arrays.asList(ge.getAvailableFontFamilyNames()));
		if (fontNames.contains(FONT_NAME))
			getFontFromSystem();
		else
			getFontFromIncludedTtf();
	}

	private RoboFont() {
		// Never instantiate this class
	}

	private static void getFontFromSystem() {
		log.info("Loading font '"+FONT_NAME+"' from system");
		basePlainFont = new Font(FONT_NAME, Font.PLAIN, 12);
		baseBoldFont = new Font(FONT_NAME, Font.BOLD, 12);
		derivedPlainFonts.put(12, basePlainFont);
		derivedBoldFonts.put(12, baseBoldFont);
	}

	private static void getFontFromIncludedTtf() {
		log.info("Loading font '"+FONT_NAME+"' from bundled ttf file");
		// There seems to be a weird bug in some versions of java 5 that throws a FontFormatException when we load
		// direct from an inputstream, but works fine if we pass it a file... strange, but whatever, we just copy it out
		try {
			File plainTtfFile = File.createTempFile("robofont", "ttf");
			plainTtfFile.deleteOnExit();
			File boldTtfFile = File.createTempFile("robofont", "ttf");
			boldTtfFile.deleteOnExit();
			InputStream ris = RoboFont.class.getResourceAsStream(REG_FONT_PATH);
			streamDump(ris, new FileOutputStream(plainTtfFile));
			InputStream bis = RoboFont.class.getResourceAsStream(BOLD_FONT_PATH);
			streamDump(bis, new FileOutputStream(boldTtfFile));
			Font onePoint = Font.createFont(Font.TRUETYPE_FONT, plainTtfFile);
			basePlainFont = onePoint.deriveFont(Font.PLAIN, 12);
			derivedPlainFonts.put(12, basePlainFont);
			onePoint = Font.createFont(Font.TRUETYPE_FONT, boldTtfFile);
			baseBoldFont = onePoint.deriveFont(Font.BOLD, 12);
			derivedBoldFonts.put(12, baseBoldFont);
		} catch (Exception e) {
			throw new SeekInnerCalmException(e);
		}
	}

	public static Font getFont(int size, boolean bold) {
		if (bold) {
			if (!derivedBoldFonts.containsKey(size))
				derivedBoldFonts.put(size, baseBoldFont.deriveFont(Font.BOLD, size));
			return derivedBoldFonts.get(size);
		} else {
			if (!derivedPlainFonts.containsKey(size))
				derivedPlainFonts.put(size, basePlainFont.deriveFont(Font.PLAIN, size));
			return derivedPlainFonts.get(size);
		}
	}
}
