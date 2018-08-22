package com.castlabs.sdk.playerui.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.castlabs.PublishedSource;
import com.castlabs.android.SdkConsts;
import com.castlabs.android.player.IPlayerView;
import com.castlabs.android.player.PlayerController;
import com.castlabs.android.player.models.VideoTrackQuality;
import com.castlabs.android.views.PlayerViewProvider;
import com.castlabs.utils.Converter;

import java.util.List;

/**
 * This is a utility class that allows you to create a DialogFragment that can be used to
 * switch video qualities. See the various {@code newInstance} methods on how to customize the
 * dialog.
 * <p/>
 * <b>Note</b> that the hosting activity that starts the dialog should implement the
 * {@link PlayerViewProvider} interface as the dialog needs to be able to access the underlying
 * {@link IPlayerView} to retrieve its data and apply the selection even when the system disposes
 * and recreates the dialog.
 *
 * @since 3.0.0
 */
@PublishedSource
public class VideoQualitySelectionDialogFragment extends AbstractSelectionDialog<VideoTrackQuality>{

	/**
	 * Creates a new dialog fragment that shows the available tracks and allows selecting
	 * the new track.
	 * <p/>
	 * By default, this dialog will allow to disable the manual selection and turn on adaptive
	 * bitrate switching.
	 * <p/>
	 * The default string representation of the track is its label (see {@link VideoTrackQuality#getLabel()}).
	 *
	 * @param playerView The player view
	 * @param title    The dialog title
	 * @return The dialog fragment
	 */
	public static VideoQualitySelectionDialogFragment newInstance(IPlayerView playerView, String title){
		return newInstance(playerView, title, true);
	}

	/**
	 * Creates a new dialog fragment that shows the available tracks and allows selecting
	 * the new track.
	 * <p/>
	 * The default string representation of the track is its label (see {@link VideoTrackQuality#getLabel()}).
	 *
	 * @param playerView   The player view
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to switch to automatic bitrate
	 *                     switching and disable the manual selection. The default label for the
	 *                     "disable" item will be "Auto". You can use a custom converter to specify
	 *                     a different label.
	 * @return The dialog fragment
	 */
	public static VideoQualitySelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable){
		return newInstance(playerView, title, allowDisable, new ToStringConverter<VideoTrackQuality>("Auto"));
	}

	/**
	 * Creates a new dialog fragment that shows the available tracks and allows selecting
	 * the new track.
	 * <p/>
	 * The default string representation of the track is its label (see {@link VideoTrackQuality#getLabel()}).
	 *
	 * @param playerView   The playerView
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to switch to automatic bitrate
	 *                     switching and disable the manual selection.
	 * @param converter    The converter that is used to translate {@link VideoTrackQuality} instanced to
	 *                     Strings that are used as labels in the dialog. Note that the converter
	 *                     should translate {@code null} to the string representation of the "disable"
	 *                     item if {@code allowDisable} is set to true.
	 * @return The dialog fragment
	 */
	public static VideoQualitySelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable, Converter<VideoTrackQuality, String> converter){
		VideoQualitySelectionDialogFragment fragment = new VideoQualitySelectionDialogFragment();
		Items items = fragment.collectItems(fragment.getCurrent(playerView), fragment.getAll(playerView), converter, allowDisable);
		fragment.setPlayerView(playerView);

		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putBoolean(ARG_ALLOW_DISABLE, allowDisable);
		args.putStringArray(ARG_ITEMS, items.items);
		args.putInt(ARG_SELECTED, items.selectedItem);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	protected VideoTrackQuality getCurrent(@NonNull IPlayerView playerView) {
		PlayerController pc = playerView.getPlayerController();
		VideoTrackQuality current = pc.getVideoQuality();
		return pc.getVideoQualityMode() == SdkConsts.VIDEO_QUALITY_ADAPTIVE ? null : current;
	}

	@Override
	protected List<VideoTrackQuality> getAll(@NonNull IPlayerView playerView) {
		return playerView.getPlayerController().getVideoQualities();
	}

	@Override
	protected void applySelection(@NonNull IPlayerView playerView, VideoTrackQuality selectedValue) {
		playerView.getPlayerController().setVideoQuality(selectedValue);
	}

}
