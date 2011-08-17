package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Point;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.User;
import com.robonobo.core.metadata.CommentCallback;
import com.robonobo.core.metadata.UserCallback;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.sheets.ConfirmSheet;

@SuppressWarnings("serial")
public abstract class CommentsTabPanel extends JPanel {
	static DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
	Map<Long, CommentPanel> pnlsById = new HashMap<Long, CommentPanel>();
	List<CommentPanel> topLvlCps = new ArrayList<CommentPanel>();
	JPanel cmtListPnl;
	JScrollPane cmtListScrollPane;
	NewCommentForm newCmtForm;
	RButton newCmtBtn;
	JPanel newCmtBtnPnl;
	Log log = LogFactory.getLog(getClass());
	RobonoboFrame frame;

	public CommentsTabPanel(RobonoboFrame frame) {
		this.frame = frame;
		double[][] cellSizen = { { 5, TableLayout.FILL, 0 }, { 5, TableLayout.FILL, 0 } };
		setLayout(new TableLayout(cellSizen));
		cmtListPnl = new JPanel();
		cmtListPnl.setOpaque(false);
		BoxLayout bl = new BoxLayout(cmtListPnl, BoxLayout.Y_AXIS);
		cmtListPnl.setLayout(bl);
		cmtListPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		cmtListScrollPane = new JScrollPane(cmtListPnl);
		cmtListScrollPane.setOpaque(false);
		cmtListScrollPane.setBackground(RoboColor.MID_GRAY);
		add(cmtListScrollPane, "1,1");
		newCmtBtn = new RGlassButton("Post New Comment...");
		newCmtBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CatchingRunnable hideNewCmtForm = new CatchingRunnable() {
					public void doRun() throws Exception {
						newCmtForm = null;
						layoutComments();
						newCmtBtn.setEnabled(true);
					}
				};
				newCmtForm = new NewCommentForm(hideNewCmtForm, -1);
				layoutComments();
				newCmtBtn.setEnabled(false);
				newCmtForm.textArea.requestFocusInWindow();
				// Scroll to this panel
				int offset = 0;
				for (CommentPanel cp : topLvlCps) {
					offset += cp.getHeight();
				}
				cmtListScrollPane.getViewport().setViewPosition(new Point(0, offset));
			}
		});
		// Ah, the joys of swing layout
		newCmtBtnPnl = new JPanel();
		double[][] ncbpCells = { { newCmtBtn.getPreferredSize().getWidth(), TableLayout.FILL }, { newCmtBtn.getPreferredSize().getHeight() } };
		newCmtBtnPnl.setLayout(new TableLayout(ncbpCells));
		newCmtBtnPnl.add(newCmtBtn, "0,0");
		layoutComments();
	}

	private void layoutComments() {
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				cmtListPnl.removeAll();
				for (CommentPanel cp : topLvlCps) {
					cmtListPnl.add(cp);
				}
				if (newCmtForm != null)
					cmtListPnl.add(newCmtForm);
				cmtListPnl.add(newCmtBtnPnl);
				cmtListScrollPane.revalidate();
			}
		});
	}

	protected abstract boolean canRemoveComment(Comment c);

	protected abstract void newComment(long parentCmtId, String text, CommentCallback cb);

	public void addComments(Collection<Comment> comments) {
		for (Comment cmt : comments) {
			if (pnlsById.containsKey(cmt.getCommentId()))
				continue;
			frame.getController().getOrFetchUser(cmt.getUserId(), new AddCommentCallback(cmt));
		}
	}

	/**
	 * The height offset from the top of the comment list to just below this comment
	 */
	private int offsetToComment(CommentPanel cp) {
		if(cp.c.getParentId() < 0) {
			// Top level comment - offset is total height of all top-level cmts up to and including this one
			int offset = 0;
			for (CommentPanel tlcp : topLvlCps) {
				offset += tlcp.getHeight();
				if(tlcp == cp)
					break;
			}
			return offset;
		}
		// Nested comment - offset is total height of all siblings up to and including this cmt, plus...
		// HERE
		return 0;
	}
	
	abstract class CommentRemover extends CatchingRunnable {
		protected CommentPanel cp;

		void doRemove(CommentPanel cp) {
			this.cp = cp;
			run();
		}
	}

	class AddCommentCallback implements UserCallback {
		Comment c;

		public AddCommentCallback(Comment c) {
			this.c = c;
		}

		@Override
		public void success(User u) {
			int topLvlWidth = getWidth() - 20;
			int indent = 60;
			boolean canRemove = canRemoveComment(c);
			CommentRemover remover = null;
			if (c.getParentId() <= 0) {
				// Top-level comment
				if (canRemove) {
					remover = new CommentRemover() {
						public void doRun() throws Exception {
							topLvlCps.remove(cp);
							pnlsById.remove(cp.c.getCommentId());
							layoutComments();
						}
					};
				}
				CommentPanel pnl = new CommentPanel(c, u, topLvlWidth, 0, remover);
				topLvlCps.add(pnl);
				pnlsById.put(c.getCommentId(), pnl);
				layoutComments();
			} else if (pnlsById.containsKey(c.getParentId())) {
				// Sub-comment to existing comment
				final CommentPanel parent = pnlsById.get(c.getParentId());
				int indentLvl = parent.indentLvl + 1;
				if (canRemove) {
					remover = new CommentRemover() {
						public void doRun() throws Exception {
							parent.removeSubPanel(cp);
						}
					};
				}
				CommentPanel pnl = new CommentPanel(c, u, (topLvlWidth - (indentLvl * indent)), indentLvl, remover);
				pnlsById.put(c.getCommentId(), pnl);
				parent.addSubPanel(pnl);
				layoutComments();
			} else {
				// Oops
				log.error("Cannot add comment " + c.getCommentId() + " - no parent comment " + c.getParentId());
			}
		}

		@Override
		public void error(long userId, Exception e) {
			log.error("Error fetching user " + userId + " for comment " + c.getCommentId(), e);
		}
	}

	class CommentPanel extends JPanel {
		Comment c;
		User u;
		List<CommentPanel> subPanels = new ArrayList<CommentPanel>();
		NewCommentForm newCmtForm;
		RLabel nameLbl;
		RLabel dateLbl;
		JPanel extendoPanel;
		LineBreakTextPanel textLbl;
		JPanel btnsPnl;
		int indentLvl;

		public CommentPanel(Comment c, User u, int totalWidth, int indentLvl, final CommentRemover remover) {
			this.c = c;
			this.u = u;
			this.indentLvl = indentLvl;
			int textWidth = totalWidth - 80;
			double[][] cellSizen = { { 0, 50, 10, TableLayout.FILL, 0 }, { 0, 50, TableLayout.FILL, 0 } };
			setLayout(new TableLayout(cellSizen));
			add(new JLabel(imgIconFromUrl(u.getImgUrl())), "1,1");
			extendoPanel = new JPanel();
			BoxLayout epl = new BoxLayout(extendoPanel, BoxLayout.Y_AXIS);
			extendoPanel.setLayout(epl);
			Border botLine = BorderFactory.createMatteBorder(0, 0, 1, 0, RoboColor.DARKISH_GRAY);
			Border margin = BorderFactory.createEmptyBorder(0, 0, 10, 0);
			extendoPanel.setBorder(BorderFactory.createCompoundBorder(margin, botLine));
			nameLbl = new RLabel16B(u.getFriendlyName());
			nameLbl.setAlignmentX(LEFT_ALIGNMENT);
			dateLbl = new RLabel11(df.format(c.getDate()));
			dateLbl.setAlignmentX(LEFT_ALIGNMENT);
			textLbl = new LineBreakTextPanel(c.getText(), RoboFont.getFont(13, false), textWidth);
			textLbl.setAlignmentX(LEFT_ALIGNMENT);
			add(extendoPanel, "3,1,3,2");
			btnsPnl = new JPanel();
			btnsPnl.setAlignmentX(LEFT_ALIGNMENT);
			double[][] btnsCellSizen = { { 80, 10, 100, TableLayout.FILL }, { 5, 30, 5 } };
			btnsPnl.setLayout(new TableLayout(btnsCellSizen));
			RButton replyBtn = new RGlassButton("Reply");
			replyBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CatchingRunnable hideNewCmt = new CatchingRunnable() {
						public void doRun() throws Exception {
							newCmtForm = null;
							relayoutPanel();
						}
					};
					newCmtForm = new NewCommentForm(hideNewCmt, CommentPanel.this.c.getCommentId());
					relayoutPanel();
					newCmtForm.textArea.requestFocusInWindow();
					// Scroll 
				}
			});
			btnsPnl.add(replyBtn, "0,1");
			if (remover != null) {
				RButton removeBtn = new RRedGlassButton("Remove");
				final CatchingRunnable doRemove = new CatchingRunnable() {
					public void doRun() throws Exception {
						remover.doRemove(CommentPanel.this);
						frame.getController().deleteComment(CommentPanel.this.c.getCommentId(), new CommentCallback() {
							public void success(Comment c) {
								// Do nothing
							}

							public void error(long commentId, Exception ex) {
								log.error("Error deleting comment", ex);
							}
						});
					}
				};
				removeBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.showSheet(new ConfirmSheet(frame, "Delete comment?", "Are you sure you want to delete this comment and all replies?", "Remove", doRemove));
					}
				});
				btnsPnl.add(removeBtn, "2,1");
				relayoutPanel();
			}
		}

		public void addSubPanel(CommentPanel sp) {
			subPanels.add(sp);
			relayoutPanel();
		}

		public void removeSubPanel(CommentPanel cp) {
			subPanels.remove(cp);
			relayoutPanel();
		}

		private void relayoutPanel() {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					extendoPanel.removeAll();
					extendoPanel.add(nameLbl);
					extendoPanel.add(dateLbl);
					extendoPanel.add(Box.createVerticalStrut(5));
					extendoPanel.add(textLbl);
					extendoPanel.add(Box.createVerticalStrut(5));
					extendoPanel.add(btnsPnl);
					extendoPanel.add(Box.createVerticalStrut(5));
					for (JPanel sp : subPanels) {
						sp.setAlignmentX(LEFT_ALIGNMENT);
						extendoPanel.add(sp);
					}
					if (newCmtForm != null) {
						newCmtForm.setAlignmentX(LEFT_ALIGNMENT);
						extendoPanel.add(newCmtForm);
					}
					extendoPanel.revalidate();
				}
			});
		}
	}

	class NewCommentForm extends JPanel {
		RTextArea textArea;
		RButton postBtn;

		public NewCommentForm(final Runnable remover, final long parentCmtId) {
			double[][] cellSizen = { { 0, 50, 10, TableLayout.FILL, 100, 10, 100, 10 }, { 0, 25, 0, 60, 10, 30, 10 } };
			setLayout(new TableLayout(cellSizen));
			User me = frame.getController().getMyUser();
			add(new JLabel(imgIconFromUrl(me.getImgUrl())), "1,1,1,3,LEFT,TOP");
			add(new RLabel14B("Post New Comment"), "3,1,LEFT,TOP");
			textArea = new RTextArea(3, 50);
			textArea.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					postBtn.setEnabled(textArea.getText().length() > 0);
				}
			});
			add(new JScrollPane(textArea), "3,3,6,3");
			RButton cancelBtn = new RRedGlassButton("CANCEL");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					remover.run();
				}
			});
			add(cancelBtn, "4,5");
			postBtn = new RGlassButton("POST");
			postBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					postBtn.setText("POSTING...");
					postBtn.setEnabled(false);
					newComment(parentCmtId, textArea.getText(), new CommentCallback() {
						public void success(Comment c) {
							runOnUiThread(remover);
						}

						public void error(long commentId, Exception ex) {
							// Should never happen
							runOnUiThread(remover);
							log.error("Error posting comment", ex);
						}
					});
				}
			});
			postBtn.setEnabled(false);
			add(postBtn, "6,5");
		}
	}
}
