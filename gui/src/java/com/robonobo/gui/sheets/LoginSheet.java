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
	private Log log = LogFactory.getLog(getClass());

	public LoginSheet(RobonoboFrame rFrame, Runnable onLogin) throws HeadlessException {
		super(rFrame);
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
		emailField.setEnabled(false);
		passwordField.setEnabled(false);
		loginBtn.setEnabled(false);
		statusLbl.setForeground(RoboColor.DARKISH_GRAY);
		statusLbl.setText("Logging in...");
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				try {
					frame.getController().login(emailField.getText(), new String(passwordField.getPassword()));
					LoginSheet.this.setVisible(false);
					if (onLogin != null)
						frame.getController().getExecutor().execute(onLogin);
				} catch(UnauthorizedException e) {
					statusLbl.setForeground(RoboColor.RED);
					statusLbl.setText("Login failed");
				} catch(Exception e) {
					statusLbl.setForeground(RoboColor.RED);
					statusLbl.setText("Server error");
				} finally {
					emailField.setEnabled(true);
					passwordField.setEnabled(true);
					loginBtn.setEnabled(true);
				}
			}
		});
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
//			cancelBtn.addKeyListener(LoginSheet.this);
			cancelBtn.getActionMap().put("ESCAPE", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					LoginSheet.this.setVisible(false);
				}
			});
			add(cancelBtn);

			add(Box.createHorizontalStrut(10));

			loginBtn = new RGlassButton("LOGIN");
			loginBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tryLogin();
				}
			});
//			loginBtn.addKeyListener(LoginSheet.this);
			add(loginBtn);

		}
	}
}
