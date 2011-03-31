package com.robonobo.gui.panels;

import static com.robonobo.common.util.TimeUtil.*;
import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.*;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnModelExt;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.UserPlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.core.wang.WangListener;
import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class WangContentPanel extends ContentPanel implements WangListener, UserPlaylistListener {
	static final char WANG_CHAR = 0x65fa;
	static final int MAX_RECENT_TRANSACTIONS = 16;
	
	RLabel balanceLbl;
	NumberFormat balanceFmt;
	AccountTableModel tm;
	private RButton topUpBtn;
	
	public WangContentPanel(RobonoboFrame frame) {
		this.frame = frame;
		setName("playback.background.panel");
		double[][] cellSizen = { { 10, 220, TableLayout.FILL, 10 },
				{ 10, 30, 10, 120, 30, 20, 30, 5, TableLayout.FILL, 10 } };
		setLayout(new TableLayout(cellSizen));
		
		RLabel balanceTitle = new RLabel22B("Current balance: ");
		add(balanceTitle, "1,1");
		
		balanceLbl = new RLabel22(createImageIcon("/icon/wang_symbol.png", null));
		add(balanceLbl, "2,1,LEFT,CENTER");
		balanceFmt = NumberFormat.getInstance();
		balanceFmt.setMinimumFractionDigits(4);
		balanceFmt.setMaximumFractionDigits(16);
		
		String blurb = "<html>"+
			"<p>Wang ("+WANG_CHAR+") is the credit you have in the robonobo network. As you share tracks with others, your Wang will increase. As you download tracks, your Wang will shrink.</p>" +
			"<br><p>If you download tracks in advance using the download button or the 'Download tracks automatically' playlist option, you will use less Wang than if you play the tracks without downloading them first.</p>"+
			"<br><p><b>During alpha/beta testing only</b>, you can request an addition to your Wang here:</p>"+
			"</html>";
		RLabel blurbLbl = new RLabel12(blurb);
		add(blurbLbl, "1,3,2,3,LEFT,TOP");
		
		topUpBtn = new RGlassButton("Request Wang Top-Up");
		topUpBtn.setPreferredSize(topUpBtn.getMinimumSize());
		topUpBtn.setEnabled(false);
		topUpBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestTopUp();
			}
		});
		add(topUpBtn, "1,4");
		
		RLabel tableTitle = new RLabel22B("Recent transactions:");
		add(tableTitle, "1,6,2,6,LEFT,CENTER");
		
		tm = new AccountTableModel();
		JXTable table = new JXTable(tm);
		table.setFont(RoboFont.getFont(11, false));
		table.setRowHeight(18);
		table.setColumnControlVisible(false);
		table.setHorizontalScrollEnabled(false);
		table.setFillsViewportHeight(true);
		table.setBackground(Color.WHITE);
		table.setHighlighters(HighlighterFactory.createSimpleStriping());
		TableColumnModelExt cm = (TableColumnModelExt) table.getColumnModel();
		cm.getColumn(0).setPreferredWidth(55);
		cm.getColumn(1).setPreferredWidth(75);
		cm.getColumn(2).setPreferredWidth(75);
		cm.getColumn(3).setPreferredWidth(545);
		add(new JScrollPane(table), "1,8,2,8");
		frame.getController().addWangListener(this);
		frame.getController().addUserPlaylistListener(this);
	}
	
	protected void requestTopUp() {
		topUpBtn.setText("Requesting...");
		topUpBtn.setEnabled(false);
		frame.getController().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				try {
					frame.getController().requestTopUp();
					SwingUtilities.invokeLater(new CatchingRunnable() {
						public void doRun() throws Exception {
							topUpBtn.setText("Request sent");
						}
					});
				} catch(IOException e) {
					SwingUtilities.invokeLater(new CatchingRunnable() {
						public void doRun() throws Exception {
							topUpBtn.setText("Error (check log)");
						}
					});
				}
			}
		});
	}
	
	@Override
	public void balanceChanged(final double newBalance) {
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				String balTxt = balanceFmt.format(newBalance);
				// TODO make it red
				balanceLbl.setText(balTxt);
			}
		});
	}
	
	@Override
	public void accountActivity(final double creditValue, final String narration) {
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				tm.add(creditValue, narration);
			}
		});
	}
	
	@Override
	public void loggedIn() {
		topUpBtn.setEnabled(true);
	}
	
	@Override
	public void libraryChanged(Library lib) {
		// Do nothing
	}
	
	@Override
	public void playlistChanged(Playlist p) {
		// Do nothing
	}
	
	@Override
	public void userChanged(User u) {
		// Do nothing
	}
	
	@Override
	public void allUsersAndPlaylistsUpdated() {
		// Do nothing
	}
	
	@Override
	public void userConfigChanged(UserConfig cfg) {
		// Do nothing
	}
	
	class AccountTableModel extends AbstractTableModel {
		String[] colNames = { "Time", "Debit", "Credit", "Transaction" };
		LinkedList<AccountTableEntry> entries = new LinkedList<WangContentPanel.AccountTableEntry>();
		NumberFormat nf;
		DateFormat df;
		
		public AccountTableModel() {
			nf = NumberFormat.getInstance();
			nf.setMinimumFractionDigits(4);
			nf.setMaximumFractionDigits(8);
			df = new SimpleDateFormat("HH:mm:ss");
		}
		
		@Override
		public int getColumnCount() {
			return colNames.length;
		}
		
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}
		
		@Override
		public int getRowCount() {
			return entries.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			AccountTableEntry entry = entries.get(rowIndex);
			switch(columnIndex) {
			case 0:
				return df.format(entry.date);
			case 1:
				if(entry.creditValue < 0d)
					return nf.format(entry.creditValue);
				return null;
			case 2:
				if(entry.creditValue > 0d)
					return nf.format(entry.creditValue);
				return null;
			case 3:
				return entry.narration;
			}
			throw new SeekInnerCalmException();
		}
		
		public void add(double creditValue, String narration) {
			while(entries.size() > MAX_RECENT_TRANSACTIONS) {
				entries.removeFirst();
				fireTableRowsDeleted(0, 0);
			}
			entries.add(new AccountTableEntry(creditValue, narration));
			int idx = entries.size() - 1;
			fireTableRowsInserted(idx, idx);
		}
	}
	
	class AccountTableEntry {
		double creditValue;
		String narration;
		Date date = now();
		
		public AccountTableEntry(double creditValue, String narration) {
			this.creditValue = creditValue;
			this.narration = narration;
		}
	}
}
