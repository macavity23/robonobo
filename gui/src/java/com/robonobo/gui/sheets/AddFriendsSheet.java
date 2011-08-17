package com.robonobo.gui.sheets;

import static com.robonobo.common.util.TextUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class AddFriendsSheet extends Sheet {
	private static final String DEFAULT_EMAILS = "Email1, email2...";
	private Dimension size = new Dimension(365, 160);
	private RTextField emailField;
	private RButton shareBtn;
	private RButton cancelBtn;
	private Log log = LogFactory.getLog(getClass());

	public AddFriendsSheet(RobonoboFrame frame) {
		super(frame);
		setPreferredSize(size);
		double[][] cellSizen = { { 10, 90, 5, 250, 10 }, { 10, 25, 10, 30, 10, 25, 10, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		RLabel titleLbl = new RLabel16B("Add friends");
		add(titleLbl, "1,1,3,1");
		JPanel blurb = new LineBreakTextPanel("Enter your friends' email addresses below to invite them to robonobo:", RoboFont.getFont(13, false), 345);
		add(blurb, "1,3,3,3");
		RLabel newFriendLbl = new RLabel12("New friends:");
		add(newFriendLbl, "1,5");
		emailField = new RTextField(DEFAULT_EMAILS);
		emailField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				shareBtn.setEnabled(targetSelected());
			}
		});
		emailField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (emailField.getText().equals(DEFAULT_EMAILS))
					emailField.setText("");
			}
		});
		add(emailField, "3,5");
		add(new ButtonPanel(), "3,7,r,t");
	}

	@Override
	public void onShow() {
		emailField.requestFocusInWindow();
		emailField.selectAll();
	}

	@Override
	public JButton defaultButton() {
		return shareBtn;
	}

	private boolean targetSelected() {
		// Could bugger about with regexes here, but I don't think it's worth it
		return (emailField.getText().length() > 0 && !emailField.getText().equals(DEFAULT_EMAILS));
	}

	private class ButtonPanel extends JPanel {
		public ButtonPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			shareBtn = new RGlassButton("SHARE");
			shareBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.getController().getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							Set<String> emails = new HashSet<String>();
							String emailFieldTxt = emailField.getText();
							if (isNonEmpty(emailFieldTxt) && !DEFAULT_EMAILS.equals(emailFieldTxt)) {
								for (String emailStr : emailFieldTxt.split(",")) {
									if (emailStr.trim().length() > 0)
										emails.add(emailStr.trim());
								}
							}
							frame.control.addFriends(emails);
							log.info("Add friend request sent for " + emails.size() + " emails");
						}
					});
					AddFriendsSheet.this.setVisible(false);
				}
			});
			add(shareBtn);
			shareBtn.setEnabled(targetSelected());
			Dimension fillerD = new Dimension(20, 1);
			add(new Box.Filler(fillerD, fillerD, fillerD));
			cancelBtn = new RRedGlassButton("CANCEL");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					AddFriendsSheet.this.setVisible(false);
				}
			});
			add(cancelBtn);
		}
	}
}
