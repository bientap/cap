package com.castlabs.sdk.playerui.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.castlabs.android.player.IPlayerView;
import com.castlabs.android.player.models.SubtitleTrack;
import com.castlabs.android.views.PlayerViewProvider;
import com.castlabs.utils.Converter;

import java.util.List;

/**
 * This is a utility class that allows you to create a DialogFragment that can be used to
 * switch subtitle tracks. See the various {@code newInstance} methods on how to customize the
 * dialog.
 * <p/>
 * <b>Note</b> that the hosting activity that starts the dialog should implement the
 * {@link PlayerViewProvider} interface as the dialog needs to be able to access the underlying
 * {@link IPlayerView} to retrieve its data and apply the selection even when the system disposes
 * and recreates the dialog.
 *
 * @since 3.0.0
 */
@com.castlabs.PublishedSource
public class SubtitleSelectionDialogFragment extends AbstractSelectionDialog<SubtitleTrack>{

	/**
	 * Creates a new dialog fragment that shows the available subtitle tracks and allows selecting
	 * the new track.
	 * <p/>
	 * By default, this dialog will allow to disable the subtitle track.
	 * <p/>
	 * The default string representation of the subtitle track is its label (see {@link SubtitleTrack#getLabel()}).
	 *
	 * @param playerView The player view
	 * @param title    The dialog title
	 * @return The dialog fragment
	 */
	public static SubtitleSelectionDialogFragment newInstance(IPlayerView playerView, String title) {
		return newInstance(playerView, title, true);
	}

	/**
	 * Creates a new dialog fragment that shows the available subtitle tracks and allows selecting
	 * the new track.
	 * <p/>
	 * The default string representation of the subtitle track is its label (see {@link SubtitleTrack#getLabel()}).
	 *
	 * @param playerView The player view
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to disable the subtitle track.
	 *                     If you need to specify a custom label for the "disable"
	 *                     entry, use {@link #newInstance(IPlayerView, String, boolean, Converter)}
	 *                     and specify a custom converter
	 * @return The dialog fragment
	 */
	public static SubtitleSelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable) {
		return newInstance(playerView, title, allowDisable, new ToStringConverter<SubtitleTrack>());
	}

	/**
	 * Creates a new dialog fragment that shows the available subtitle tracks and allows selecting
	 * the new track.
	 *
	 * @param playerView The player view
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to disable the subtitle track.
	 * @param converter    The converter that is used to translate {@link SubtitleTrack} instanced to
	 *                     Strings that are used as labels in the dialog. Note that the converter
	 *                     should translate {@code null} to the string representation of the "disable"
	 *                     item if {@code allowDisable} is set to true.
	 * @return The dialog fragment
	 */
	public static SubtitleSelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable, Converter<SubtitleTrack, String> converter){
		SubtitleSelectionDialogFragment fragment = new SubtitleSelectionDialogFragment();
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
	protected SubtitleTrack getCurrent(@NonNull IPlayerView playerView) {
		return playerView.getPlayerController().getSubtitleTrack();
	}

	@Override
	protected List<SubtitleTrack> getAll(@NonNull IPlayerView playerView) {
		return playerView.getPlayerController().getSubtitleTracks();
	}

	@Override
	protected void applySelection(@NonNull IPlayerView playerView, SubtitleTrack selectedValue) {
		playerView.getPlayerController().setSubtitleTrack(selectedValue);
	}
}
