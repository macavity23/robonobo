package com.robonobo.gui.sheets;

import static com.robonobo.common.util.TextUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.event.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public abstract class PostUpdateSheet extends Sheet {
	private static final Pattern ENDS_WITH_WHITESPACE = Pattern.compile("^.*(\\s+)$");
	protected Playlist p;
	private String playlistUrl;
	private RTextArea msgText;
	private RLabel charRemainingLbl;
	RButton postBtn;
	RButton cancelBtn;
	Log log = LogFactory.getLog(getClass());

	public PostUpdateSheet(RobonoboFrame f, Playlist pl) {
		super(f);
		this.p = pl;
		playlistUrl = frame.getController().getConfig().getPlaylistUrlBase() + Long.toHexString(p.getPlaylistId());
		boolean showCharLimit = (charLimit() > 0);
		double[][] cellSizen = { { 10, 300, 10 }, { 10, 20, 5, showCharLimit ? 80 : 100, 5, showCharLimit ? 20 : 0, 0, 20, 5, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		add(new RLabel14B("Post playlist update to " + getServiceName()), "1,1");
		Document doc = (charLimit() < 0) ? new PlainDocument() : new LimitPlainDocument();
		msgText = new RTextArea(doc);
		msgText.setText("I updated my playlist '" + truncateTitle(pl.getTitle()) + "': ");
		msgText.setLineWrap(true);
		msgText.setWrapStyleWord(true);
		add(msgText, "1,3");
		msgText.requestFocus();
		msgText.selectAll();
		if (showCharLimit) {
			charRemainingLbl = new RLabel11(charsLeft() + " characters remaining");
			add(charRemainingLbl, "1,5");
			msgText.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					charRemainingLbl.setText(charsLeft() + " characters remaining");
				}
			});
		}
		add(new RLabel11B("The playlist url will be added to your message."), "1,7");
		add(new ButtonPanel(), "1,9,r,t");
	}

	protected String getMsg() {
		// Make sure there's a space on the end so that when we append the url it can be parsed cleanly
		String text = msgText.getText();
		if (ENDS_WITH_WHITESPACE.matcher(text).matches())
			return text;
		else
			return text + " ";
	}

	private int charsLeft() {
		if (charLimit() < 0)
			return Integer.MAX_VALUE;
		String msg = getMsg();
		return charLimit() - (msg.length() + playlistUrl.length());
	}

	protected abstract String getServiceName();

	/** <0 for no limit */
	protected abstract int charLimit();

	public void postUpdate() {
		frame.getController().postPlaylistServiceUpdate(getServiceName().toLowerCase(), p.getPlaylistId(), getMsg());
	}

	private String truncateTitle(String title) {
		if (charLimit() < 0)
			return title;
		int baseSz = "I updated my playlist '".length() + "': ".length() + playlistUrl.length();
		return truncate(title, charLimit() - baseSz);
	}

	@Override
	public void onShow() {
		msgText.requestFocusInWindow();
		msgText.selectAll();
	}

	@Override
	public JButton defaultButton() {
		return postBtn;
	}

	class ButtonPanel extends JPanel {
		public ButtonPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			postBtn = new RGlassButton("POST UPDATE");
			postBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.getController().getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							postUpdate();
						}
					});
					PostUpdateSheet.this.setVisible(false);
				}
			});
			add(postBtn);
			add(Box.createHorizontalStrut(10));
			cancelBtn = new RRedGlassButton("CANCEL");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PostUpdateSheet.this.setVisible(false);
				}
			});
			add(cancelBtn);
		}
	}

	class LimitPlainDocument extends PlainDocument {
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			String text = msgText.getText() + str;
			int textLen = text.length();
			if (!ENDS_WITH_WHITESPACE.matcher(text).matches())
				textLen++;
			textLen += playlistUrl.length();
			if (textLen <= charLimit())
				super.insertString(offs, str, a);
		}
	}
}
