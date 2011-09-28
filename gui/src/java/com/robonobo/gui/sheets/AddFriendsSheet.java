package com.robonobo.gui.sheets;

import static com.robonobo.common.util.TextUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.NetUtil;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class AddFriendsSheet extends Sheet {
	private static final String DEFAULT_EMAILS = "Email1, email2...";
	private RTextField emailField;
	private RButton sendFrBtn;
	private RButton cancelBtn;
	private Log log = LogFactory.getLog(getClass());

	public AddFriendsSheet(RobonoboFrame f, boolean haveFacebook) {
		super(f);
		Dimension sz = new Dimension(420, haveFacebook ? 385 : 460);
		setPreferredSize(sz);
		double[][] cellSizen = { { 20, TableLayout.FILL, 20 }, { 20, 30/*title*/, 10, 25/*fb subt*/, 10, 50/*fb blurb*/, 5, 30/*fb promise*/, 10, 30/*fb btn*/, 20, 25/*man subt*/, 10, 50/*man blurb*/, 5, 30/*email*/, 5, 30/*add btn*/, 20, 30/*cancel*/, 10 } };
		TableLayout tl = new TableLayout(cellSizen);
		setLayout(tl);
		setName("playback.background.panel");
		add(new RLabel24B("Add friends"), "1,1");
		if(haveFacebook) {
			add(new RLabel16B("Facebook details registered"), "1,3");
			add(makeText("Your Facebook account has been registered with robonobo. Any of your Facebook friends who sign up for robonobo will appear in your friends list automatically.", false), "1,5");
			tl.setRow(6, 0);
			tl.setRow(7, 0);
			tl.setRow(8, 0);
			tl.setRow(9, 0);
		} else {
			add(new RLabel16B("Add friends from Facebook"), "1,3");
			add(makeText("Add your Facebook details to your robonobo account: this adds all your Facebook friends who use robonobo, and will automatically add any who sign up for robonobo later.", false), "1,5");
			add(makeText("We will never spam you or your friends, or post to your Facebook without your permission.", true), "1,7");
			RButton addFbBtn = new RGlassButton("Add Facebook details (opens web page)");
			addFbBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						NetUtil.browse(frame.ctrl.getConfig().getWebsiteUrlBase() + "before-facebook-attach");
						frame.ctrl.watchMyUserConfig();
						setVisible(false);
					} catch (IOException e) {
						log.error("Caught ioexception browsing to facebook attach", e);
					}
				}
			});
			add(placeBtn(addFbBtn), "1,9");		
		}
		
		
		add(new RLabel16B("Add friends by email"),"1,11");
		add(makeText("Enter your friends' email addresses below to send them a friend request. This will invite them to robonobo if they have not joined already.", false), "1,13");
		emailField = new RTextField(DEFAULT_EMAILS);
		emailField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				sendFrBtn.setEnabled(targetSelected());
			}
		});
		emailField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (emailField.getText().equals(DEFAULT_EMAILS))
					emailField.setText("");
			}
		});
		add(emailField, "1,15");
		sendFrBtn = new RGlassButton("Send friend requests");
		sendFrBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						Set<String> emails = new HashSet<String>();
						String emailFieldTxt = emailField.getText();
						if (isNonEmpty(emailFieldTxt) && !DEFAULT_EMAILS.equals(emailFieldTxt)) {
							for (String emailStr : emailFieldTxt.split(",")) {
								if (emailStr.trim().length() > 0)
									emails.add(emailStr.trim());
							}
						}
						frame.ctrl.addFriends(emails);
						log.info("Add friend request sent for " + emails.size() + " emails");
					}
				});
				AddFriendsSheet.this.setVisible(false);
			}
		});
		add(placeBtn(sendFrBtn), "1,17");
		cancelBtn = new RRedGlassButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AddFriendsSheet.this.setVisible(false);
			}
		});
		add(placeBtn(cancelBtn), "1,19");
	}

	private LineBreakTextPanel makeText(String text, boolean bold) {
		return new LineBreakTextPanel(text, RoboFont.getFont(13, bold), 380);
	}
	
	private JPanel placeBtn(RButton btn) {
		JPanel p = new JPanel();
		double[][] cells = { { TableLayout.FILL, btn.getPreferredSize().width}, {TableLayout.FILL} };
		p.setLayout(new TableLayout(cells));
		p.add(btn, "1,0");
		return p;
	}
	
	@Override
	public void onShow() {
		emailField.requestFocusInWindow();
		emailField.selectAll();
	}

	@Override
	public JButton defaultButton() {
		return cancelBtn;
	}

	private boolean targetSelected() {
		return (emailField.getText().length() > 0 && !emailField.getText().equals(DEFAULT_EMAILS));
	}

}
