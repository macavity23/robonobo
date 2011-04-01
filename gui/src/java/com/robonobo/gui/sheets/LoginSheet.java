package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.ComponentOrientation;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.serialization.UnauthorizedException;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

public class LoginSheet extends Sheet {
	private RButton loginBtn;
	private RButton cancelBtn;
	private RTextField emailField;
	private RPasswordField passwordField;
	private RLabel statusLbl;
	private Runnable onLogin;
	private boolean cancelAllowed;
	private Log log = LogFactory.getLog(getClass());

	public LoginSheet(RobonoboFrame rFrame, boolean cancelAllowed, Runnable onLogin) throws HeadlessException {
		super(rFrame);
		this.cancelAllowed = cancelAllowed;
		this.onLogin = onLogin;
		double[][] cellSizen = { { 10, 100, 10, 180, 10 }, { 10, 25, 5, 25, 5, 25, 5, 25, 5, 20, 5, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");

		add(new RLabel14B("Please login to robonobo"), "1,1,3,1,CENTER,CENTER");

		String blurbTxt = "<html><center>Visit <a href=\"http://robonobo.com\">http://robonobo.com</a> for an account.<br><br></center></html>";
		add(new HyperlinkPane(blurbTxt, RoboColor.MID_GRAY), "1,3,3,3");

		add(new RLabel12("Email:"), "1,5,r,f");
		emailField = new RTextField();
		String email = frame.getController().getConfig().getMetadataServerUsername();
		if(email != null)
			emailField.setText(email);
		add(emailField, "3,5");

		add(new RLabel12("Password:"), "1,7,r,f");

		passwordField = new RPasswordField();
		String pwd = frame.getController().getConfig().getMetadataServerPassword();
		if(pwd != null)
			passwordField.setText(pwd);
		add(passwordField, "3,7");

		statusLbl = new RLabel12("");
		add(statusLbl, "1,9,3,9,RIGHT,CENTER");

		add(new ButtonPanel(), "1,11,3,11");

	}

	@Override
	public void onShow() {
		emailField.requestFocusInWindow();
		emailField.selectAll();
	}

	@Override
	public JButton defaultButton() {
		return loginBtn;
	}
	
	public JTextField getEmailField() {
		return emailField;
	}
	
	public JPasswordField getPasswordField() {
		return passwordField;
	}
	
	public void tryLogin() {
		if(!SwingUtilities.isEventDispatchThread())
			throw new SeekInnerCalmException();
		emailField.setEnabled(false);
		passwordField.setEnabled(false);
		loginBtn.setEnabled(false);
		statusLbl.setForeground(RoboColor.DARKISH_GRAY);
		statusLbl.setText("Logging in...");		
		// Do this buggering about with runnables to avoid blocking the ui thread as we login
		frame.getController().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				String errorMsg = null;
				try {
					frame.getController().login(emailField.getText(), new String(passwordField.getPassword()));
				} catch(UnauthorizedException e) {
					errorMsg = "Login failed";
				} catch(Exception e) {
					errorMsg = "Server error";
					log.error("Caught exception logging in", e);
				}
				final String flarp = errorMsg;
				SwingUtilities.invokeLater(new CatchingRunnable() {
					public void doRun() throws Exception {
						emailField.setEnabled(true);
						passwordField.setEnabled(true);
						loginBtn.setEnabled(true);
						if(flarp == null)
							LoginSheet.this.setVisible(false);
						else {
							statusLbl.setForeground(RoboColor.RED);
							statusLbl.setText(flarp);
						}
					}
				});
				if(errorMsg == null && onLogin != null)
					onLogin.run();
			}
		});
	}

	public boolean getCancelAllowed() {
		return cancelAllowed;
	}
	
	private class ButtonPanel extends JPanel {
		public ButtonPanel() {
			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			// Laying out right-to-left
			cancelBtn = new RRedGlassButton("CANCEL");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					LoginSheet.this.setVisible(false);
				}
			});
			cancelBtn.setEnabled(cancelAllowed);
			add(cancelBtn);

			add(Box.createHorizontalStrut(10));

			loginBtn = new RGlassButton("LOGIN");
			loginBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tryLogin();
				}
			});
			add(loginBtn);

		}
	}
}
