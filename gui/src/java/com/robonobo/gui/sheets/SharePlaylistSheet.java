package com.robonobo.gui.sheets;

import static com.robonobo.common.util.TextUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.User;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class SharePlaylistSheet extends Sheet {
	private static final String DEFAULT_EMAILS = "Email1, email2...";
	private Playlist p;
	private Dimension size = new Dimension(445, 305);
	private JList friendList;
	private RTextField emailField;
	private RButton shareBtn;
	private RButton cancelBtn;
	private Log log = LogFactory.getLog(getClass());
	private RobonoboController control;
	private Vector<UserWrapper> friends;

	public SharePlaylistSheet(RobonoboFrame frame, Playlist p) {
		super(frame);
		this.p = p;
		control = frame.ctrl;
		setPreferredSize(size);
		double[][] cellSizen = { { 10, 170, 5, 250, 10 }, { 10, 25, 5, 50, 5, 20, 5, 100, 10, 25, 10, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		RLabel titleLbl = new RLabel16B("Share playlist '"+p.getTitle()+"':");
		add(titleLbl, "1,1,3,1");
		String blurbText = "When you share a playlist with friends, they will be notified by email. They will be able to update the playlist, and you will see any changes they make.";
		LineBreakTextPanel blurbPnl = new LineBreakTextPanel(blurbText, RoboFont.getFont(13, false), 425);
		add(blurbPnl, "1,3,3,3");
		add(new RLabel14B("Share with:"), "1,5,3,5");
		RLabel exFriendLbl = new RLabel12("Existing friends:");
		add(exFriendLbl, "1,7,l,t");
		friends = new Vector<UserWrapper>();
		for (Long friendId : control.getMyUser().getFriendIds()) {
			User user = control.getKnownUser(friendId);
			if (user != null)
				friends.add(new UserWrapper(user));
		}
		friendList = new JList(friends);
		friendList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		friendList.setSelectionModel(new FriendListSelectionModel());
		friendList.setCellRenderer(new FriendListCellRenderer());
		friendList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				shareBtn.setEnabled(targetSelected());
			}
		});
		friendList.setBorder(BorderFactory.createLineBorder(RoboColor.DARK_GRAY));
		JScrollPane flScrollPane = new JScrollPane(friendList);
		add(flScrollPane, "3,7");

		RLabel newFriendLbl = new RLabel12("New friends:");
		add(newFriendLbl, "1,9");
		emailField = new RTextField(DEFAULT_EMAILS);
		emailField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				shareBtn.setEnabled(targetSelected());
			}
		});
		emailField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(emailField.getText().equals(DEFAULT_EMAILS))
					emailField.setText("");
			}
		});
		add(emailField, "3,9");

//		int invitesLeft = control.getMyUser().getInvitesLeft();
//		if (invitesLeft <= 0)
//			emailField.setEnabled(false);
//		RLabel inviteLbl = new RLabel12(numItems(invitesLeft, "invite") + " left");
//		add(inviteLbl, "1,7");

		add(new ButtonPanel(), "3,11,r,t");
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
		if(friendList.getSelectedIndices().length > 0)
			return true;
		// Could bugger about with regexes here, but I don't think it's worth it
		return (emailField.getText().length() > 0 && !emailField.getText().equals(DEFAULT_EMAILS)) ;
	}
	
	private class ButtonPanel extends JPanel {
		public ButtonPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			shareBtn = new RGlassButton("Share");
			shareBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							Set<Long> selFriendIds = new HashSet<Long>();
							for (Object obj : friendList.getSelectedValues()) {
								selFriendIds.add(((UserWrapper) obj).u.getUserId());
							}
							Set<String> emails = new HashSet<String>();
							String emailFieldTxt = emailField.getText();
							if (isNonEmpty(emailFieldTxt) && !DEFAULT_EMAILS.equals(emailFieldTxt)){
								for (String emailStr : emailFieldTxt.split(",")) {
									if (emailStr.trim().length() > 0)
										emails.add(emailStr.trim());
								}
							}
							try {
								control.sharePlaylist(p, selFriendIds, emails);
								User me = control.getMyUser();
								// This is just for show in the UI, the server will be updating this figure at their end
								me.setInvitesLeft(me.getInvitesLeft() - emails.size());
								log.info("Playlist '"+p.getTitle()+"' shared");
							} catch (RobonoboException ex) {
								log.error("Caught exception sharing playlist", ex);
							}
						}
					});
					SharePlaylistSheet.this.setVisible(false);
				}
			});
			add(shareBtn);
			shareBtn.setEnabled(targetSelected());
			Dimension fillerD = new Dimension(20, 1);
			add(new Box.Filler(fillerD, fillerD, fillerD));
			cancelBtn = new RRedGlassButton("Cancel");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					SharePlaylistSheet.this.setVisible(false);
				}
			});
			add(cancelBtn);
		}
	}

	private class UserWrapper {
		User u;

		public UserWrapper(User u) {
			this.u = u;
		}

		@Override
		public String toString() {
			return u.getFriendlyName() + " (" + u.getEmail() + ")";
		}
	}

	private class FriendListCellRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			lbl.setFont(RoboFont.getFont(12, false));
			return lbl;
		}
	}

	private class FriendListSelectionModel extends DefaultListSelectionModel {
		List<Integer> ownerIdxs = new ArrayList<Integer>();
		public FriendListSelectionModel() {
			for(int i=0;i<friends.size();i++) {
				if(p.getOwnerIds().contains(friends.get(i).u.getUserId())) {
					ownerIdxs.add(i);
					super.addSelectionInterval(i, i);
				}
			}
		}
		
		@Override
		public void setSelectionInterval(int index0, int index1) {
			super.setSelectionInterval(index0, index1);
			// Can't deselect owners
			for(int ownerIdx : ownerIdxs) {
				addSelectionInterval(ownerIdx, ownerIdx);
			}
		}
		
		@Override
		public void removeSelectionInterval(int index0, int index1) {
			for(int i=index0;i<=index1;i++) {
				if(!ownerIdxs.contains(i))
					super.removeSelectionInterval(i, i);
			}
		}
	}
}
