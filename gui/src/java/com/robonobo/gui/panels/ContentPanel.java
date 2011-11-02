package com.robonobo.gui.panels;

import static com.robonobo.common.util.CodeUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.CodeUtil;
import com.robonobo.core.Platform;
import com.robonobo.gui.*;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.StreamTransfer;
import com.robonobo.gui.model.TrackListTableModel;

/** The track list and the tabs below */
@SuppressWarnings("serial")
public abstract class ContentPanel extends JPanel {
	public TrackList trackList;
	public JPanel topPane;
	public JTabbedPane tabPane;
	protected RobonoboFrame frame;
	protected Log log = LogFactory.getLog(getClass());
	private List<MessagePanel> msgs = new ArrayList<MessagePanel>();
	protected Icon bangIcon = GuiUtil.createImageIcon("/icon/exclamation-green.png");

	public ContentPanel() {
	}

	public ContentPanel(RobonoboFrame frame, TrackListTableModel tableModel) {
		this.frame = frame;
		double[][] cellSizen = { { TableLayout.FILL }, { TableLayout.FILL, } };
		setLayout(new TableLayout(cellSizen));
		topPane = new JPanel();
		double[][] tpCells = { { TableLayout.FILL }, { TableLayout.FILL } };
		topPane.setLayout(new TableLayout(tpCells));
		trackList = new TrackList(frame, tableModel);
		trackList.getJTable().setDragEnabled(true);
		trackList.getJTable().setTransferHandler(createTrackListTransferHandler());
		topPane.add(trackList, "0,0");
		tabPane = new JTabbedPane(JTabbedPane.TOP);
		tabPane.setFont(RoboFont.getFont(16, true));
		tabPane.setForeground(RoboColor.DARK_GRAY);
		tabPane.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
		tabPane.addTab("track", new TrackTab());
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPane, tabPane);
		// If we have the menu bar, we have less space to play with, so split our splitpane in a different spot
		boolean haveMenuBar = Platform.getPlatform().frameContainsMenuBar();
		splitPane.setDividerLocation(haveMenuBar ? 355 : 400);
		add(splitPane, "0,0");
	}

	protected void addBangToTab(int idx) {
		// JTabbedPane.setTabComponentAt() is j6+, so for <=5 we just mark with the bang
		if (javaMajorVersion() >= 6) {
			RLabel16B lbl = new RLabel16B(tabPane.getTitleAt(idx), bangIcon, SwingConstants.LEFT);
			lbl.setForeground(RoboColor.GREEN);
			tabPane.setTabComponentAt(idx, lbl);
		} else
			tabPane.setIconAt(idx, bangIcon);
	}

	protected void removeBangFromTab(int idx) {
		// JTabbedPane.setTabComponentAt() is j6+, so for <=5 we just mark with the bang
		if (javaMajorVersion() >= 6) {
			tabPane.setTabComponentAt(idx, new RLabel16B(tabPane.getTitleAt(idx)));
		} else
			tabPane.setIconAt(idx, null);
	}

	/** Only call this from the swing UI thread, or else you might get concurrency issues */
	public void showMessage(String title, String htmlMsg, String propName) {
		MessagePanel mp = new MessagePanel(title, htmlMsg, propName);
		if (msgs.size() == 0)
			showMessage(mp);
		msgs.add(mp);
	}

	private void showMessage(MessagePanel mp) {
		TableLayout tl = (TableLayout) topPane.getLayout();
		tl.insertRow(0, 5);
		tl.insertRow(0, mp.getPreferredSize().height);
		topPane.add(mp, "0,0");
		topPane.revalidate();
	}

	private void messageClosed() {
		TableLayout tl = (TableLayout) topPane.getLayout();
		// Bug huntin - sometimes this is called when the layout has only 1 row
		if(tl.getNumRow() < 3) {
			log.error("ERROR: in ContentPanel.messageClosed, but numrows is "+tl.getNumRow());
			return;
		}
		tl.deleteRow(0);
		tl.deleteRow(0);
		msgs.remove(0);
		if (msgs.size() > 0) {
			MessagePanel mp = msgs.get(0);
			showMessage(mp);
		} else
			topPane.revalidate();
	}

	/** If non-null, this will be focused when the contentpanel is shown */
	public JComponent defaultComponent() {
		return null;
	}

	/** For dropping stuff onto the tracklist - default impl can't import nothin */
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return false;
	}

	/** For dropping onto the track list - default impl does nothing (and in fact will never get called) */
	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}

	private class MessagePanel extends JPanel {
		public MessagePanel(String title, String htmlMsg, final String propName) {
			double[][] cellSizen = { { 10, TableLayout.FILL, 70, 10 }, { 10, 25, 5, TableLayout.FILL, 10 } };
			setName("playback.background.panel");
			setLayout(new TableLayout(cellSizen));
			RLabel titleLbl = new RLabel18B(title);
			add(titleLbl, "1,1");
			RButton closeBtn = new RRedGlassButton("Close");
			closeBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageClosed();
					if (propName != null) {
						frame.ctrl.getExecutor().execute(new CatchingRunnable() {
							public void doRun() throws Exception {
								CodeUtil.setBeanProperty(frame.guiCfg, propName, false);
								frame.ctrl.saveConfig();
							}
						});
					}
				}
			});
			add(closeBtn, "2,1");
			HyperlinkPane msgPane = new HyperlinkPane(htmlMsg, RoboColor.MID_GRAY);
			add(msgPane, "1,3,2,3");
		}
	}

	class TrackTab extends JPanel {
		public TrackTab() {
			double[][] cellSizen = { { 5, TableLayout.FILL, 5 }, { 25, TableLayout.FILL } };
			setLayout(new TableLayout(cellSizen));
			add(new RLabel12B("This track has no associated information."), "1,0");
		}
	}

	private TransferHandler createTrackListTransferHandler() {
		return new TransferHandler() {
			@Override
			public int getSourceActions(JComponent c) {
				return COPY;
			}

			@Override
			protected Transferable createTransferable(JComponent c) {
				return new StreamTransfer(trackList.getSelectedStreamIds());
			}

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
				return ContentPanel.this.canImport(comp, transferFlavors);
			}

			@Override
			public boolean importData(JComponent comp, Transferable t) {
				return ContentPanel.this.importData(comp, t);
			}
		};
	}
}
