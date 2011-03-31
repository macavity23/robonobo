package com.robonobo.gui.panels;

import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.PlaylistConfig;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.NewPlaylistTableModel;

@SuppressWarnings("serial")
public class NewPlaylistContentPanel extends MyPlaylistContentPanel {

	public NewPlaylistContentPanel(RobonoboFrame frame) {
		super(frame, new Playlist(), new PlaylistConfig(), new NewPlaylistTableModel(frame.getController()));
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				showMessage("How do I add tracks?", "<html>Drag tracks from your library, or a friend's library, or any other playlist, and drop them on the highlighted 'New Playlist' entry on the left, or on the track list below.<br>You can also drag files directly from your computer.</html>");
			}
		});
	}

	@Override
	protected void savePlaylist() {
		final Playlist p = getModel().getPlaylist();
		p.setTitle(titleField.getText());
		p.setDescription(descField.getText());
		final RobonoboController control = frame.getController();
		control.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				// Create the new playlist in midas
				try {
					p.getOwnerIds().add(control.getMyUser().getUserId());
					final Playlist updatedP = control.addOrUpdatePlaylist(p);
					SwingUtilities.invokeLater(new CatchingRunnable() {
						public void doRun() throws Exception {
							// A content panel should have been created for the new
							// playlist - switch to it now
							frame.getLeftSidebar().selectMyPlaylist(updatedP);
							// Now that they're not looking, re-init everything with
							// a new empty playlist
							Playlist newP = new Playlist();
							titleField.setText("");
							descField.setText("");
							if(iTunesCB != null)
								iTunesCB.setSelected(false);
							getModel().update(newP, true);
						}
					});
					control.checkPlaylistUpdate(updatedP.getPlaylistId());
				} catch (RobonoboException e) {
					log.error("Error creating playlist", e);
					return;
				}
			}
		});
	}
	
	@Override
	protected boolean allowDel() {
		return false;
	}
	
	@Override
	protected boolean allowShare() {
		return false;
	}
	
	@Override
	protected boolean addAsListener() {
		return false;		
	}
	
	@Override
	protected boolean showITunes() {
		return false;
	}
}
