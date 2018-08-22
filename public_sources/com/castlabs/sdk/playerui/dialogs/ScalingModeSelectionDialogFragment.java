package com.castlabs.sdk.playerui.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.castlabs.android.player.IPlayerView;
import com.castlabs.android.player.PlayerView;
import com.castlabs.android.views.PlayerViewProvider;
import com.castlabs.utils.Converter;

import java.util.Arrays;
import java.util.List;

/**
 * This is a utility class that allows you to create a DialogFragment that can be used to
 * switch scaling modes (see {@link PlayerView#setScalingMode(int)}). See the various
 * {@code newInstance} methods on how to customize the dialog.
 * <p/>
 * <b>Note</b> that the hosting activity that starts the dialog should implement the
 * {@link PlayerViewProvider} interface as the dialog needs to be able to access the underlying
 * {@link IPlayerView} to retrieve its data and apply the selection even when the system disposes
 * and recreates the dialog.
 *
 * @since 3.0.0
 */
@com.castlabs.PublishedSource
public class ScalingModeSelectionDialogFragment extends AbstractSelectionDialog<Integer> {
	private static final String[] DEFAULT_NAMES = new String[]{
			"Fit", "Crop", "Stretch"
	};

	/**
	 * Creates a new dialog fragment that shows the available scaling modes and allows selecting
	 * the new mode.
	 *
	 * @param playerView The player view
	 * @param title    the dialog title
	 * @return The dialog fragment
	 */
	public static ScalingModeSelectionDialogFragment newInstance(IPlayerView playerView, String title){
		return newInstance(playerView, title, new ScalingModeConverter());
	}

	/**
	 * Creates a new dialog fragment that shows the available scaling modes and allows selecting
	 * the new mode.
	 *
	 * @param playerView  The player view
	 * @param title     the dialog title
	 * @param converter The converter that is used to translate the existing scaling modes to string
	 *                  representations
	 * @return The dialog fragment
	 */
	public static ScalingModeSelectionDialogFragment newInstance(IPlayerView playerView, String title, Converter<Integer, String> converter){
		ScalingModeSelectionDialogFragment fragment = new ScalingModeSelectionDialogFragment();
		AbstractSelectionDialog.Items items = fragment.collectItems(fragment.getCurrent(playerView), fragment.getAll(playerView), converter, false);
		fragment.setPlayerView(playerView);

		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putBoolean(ARG_ALLOW_DISABLE, false);
		args.putStringArray(ARG_ITEMS, items.items);
		args.putInt(ARG_SELECTED, items.selectedItem);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	protected Integer getCurrent(@NonNull IPlayerView playerView) {
		return playerView.getScalingMode();
	}

	@Override
	protected List<Integer> getAll(@NonNull IPlayerView playerView) {
		return Arrays.asList(PlayerView.SCALING_MODE_FIT, PlayerView.SCALING_MODE_CROP, PlayerView.SCALING_MODE_STRETCH);
	}

	@Override
	protected void applySelection(@NonNull IPlayerView playerView, Integer selectedValue) {
		playerView.setScalingMode(selectedValue);
	}

	public static class ScalingModeConverter implements Converter<Integer, String>{

		@Nullable
		@Override
		public String convert(@Nullable Integer source) {
			return DEFAULT_NAMES[source];
		}
	}
}
