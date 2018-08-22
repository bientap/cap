package com.castlabs.sdk.playerui.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.castlabs.android.player.IPlayerView;
import com.castlabs.android.player.models.AudioTrack;
import com.castlabs.android.views.PlayerViewProvider;
import com.castlabs.utils.Converter;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a utility class that allows you to create a DialogFragment that can be used to
 * switch audio tracks. See the various {@code newInstance} methods on how to customize the
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
public class AudioTrackSelectionDialogFragment extends AbstractSelectionDialog<AudioTrack> {
	/**
	 * Used to store information about the show_supported flag in the arguments
	 */
	private static final String ARG_SHOW_SUPPORTED = "show_selected";

	/**
	 * Creates a new dialog fragment that shows the available audio tracks and allows selecting
	 * the new track.
	 * <p/>
	 * By default, this dialog will not allow to disable the audio track.
	 * <p/>
	 * The default string representation of the audio track is its label (see {@link AudioTrack#getLabel()}).
	 *
	 * @param playerView The player view
	 * @param title    The dialog title
	 * @return The dialog fragment
	 */
	public static AudioTrackSelectionDialogFragment newInstance(IPlayerView playerView, String title) {
		return newInstance(playerView, title, false);
	}

	/**
	 * Creates a new dialog fragment that shows the available audio tracks and allows selecting
	 * the new track.
	 * <p/>
	 * The default string representation of the audio track is its label (see {@link AudioTrack#getLabel()}).
	 *
	 * @param playerView     The player view
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to disable the audio track.
	 *                     If you need to specify a custom label for the "disable"
	 *                     entry, use {@link #newInstance(IPlayerView, String, boolean, Converter)}
	 *                     and specify a custom converter
	 * @return The dialog fragment
	 */
	public static AudioTrackSelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable) {
		return newInstance(playerView, title, allowDisable, new ToStringConverter<AudioTrack>());
	}

	/**
	 * Creates a new dialog fragment that shows the available audio tracks and allows selecting
	 * the new track.
	 *
	 * @param playerView     The player view
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to disable the audio track.
	 * @param converter    The converter that is used to translate {@link AudioTrack} instanced to
	 *                     Strings that are used as labels in the dialog. Note that the converter
	 *                     should translate {@code null} to the string representation of the "disable"
	 *                     item if {@code allowDisable} is set to true.
	 * @return The dialog fragment
	 */
	public static AudioTrackSelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable, Converter<AudioTrack, String> converter) {
		AudioTrackSelectionDialogFragment fragment = new AudioTrackSelectionDialogFragment();
		List<AudioTrack> audioTrackList = fragment.getAll(playerView);
		return prepareFragment(playerView, title, allowDisable, audioTrackList, converter, false);
	}

	/**
	 * Creates a new dialog fragment that shows the supported audio tracks on the device and allows selecting
	 * the new track.
	 *
	 * @param playerView   The player view
	 * @param title        The dialog title
	 * @param allowDisable If true, the dialog will allow to disable the audio track.
	 * @param converter    The converter that is used to translate {@link AudioTrack} instanced to
	 *                     Strings that are used as labels in the dialog. Note that the converter
	 *                     should translate {@code null} to the string representation of the "disable"
	 *                     item if {@code allowDisable} is set to true.
	 * @param showSupportedTracks It queries decoder to show just the tracks that are supported on either the device itself
	 *							  or passthrough.
	 * @throws MediaCodecUtil.DecoderQueryException in case an error occurs when decoder information is queried
	 * @return The dialog fragment
	 */
	public static AudioTrackSelectionDialogFragment newInstance(IPlayerView playerView, String title, boolean allowDisable, boolean showSupportedTracks, Converter<AudioTrack, String> converter) throws MediaCodecUtil.DecoderQueryException {
		AudioTrackSelectionDialogFragment fragment = new AudioTrackSelectionDialogFragment();
		List<AudioTrack> audioTrackList = fragment.getAll(playerView, showSupportedTracks);
		return prepareFragment(playerView, title, allowDisable, audioTrackList, converter, showSupportedTracks);
	}

	private static AudioTrackSelectionDialogFragment prepareFragment(IPlayerView playerView, String title,
																	 boolean allowDisable, List<AudioTrack> audioTrackList,
																	 Converter<AudioTrack, String> converter,
																	 boolean showSupportedTracks) {
		AudioTrackSelectionDialogFragment fragment = new AudioTrackSelectionDialogFragment();
		Items items = fragment.collectItems(fragment.getCurrent(playerView), audioTrackList, converter, allowDisable);
		fragment.setPlayerView(playerView);
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putBoolean(ARG_ALLOW_DISABLE, allowDisable);
		args.putStringArray(ARG_ITEMS, items.items);
		args.putInt(ARG_SELECTED, items.selectedItem);
		args.putBoolean(ARG_SHOW_SUPPORTED, showSupportedTracks);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	protected AudioTrack getCurrent(@NonNull IPlayerView playerView) {
		return playerView.getPlayerController().getAudioTrack();
	}

	@Override
	protected List<AudioTrack> getAll(@NonNull IPlayerView playerView) {
		return getAll(playerView, getArguments().getBoolean(ARG_SHOW_SUPPORTED, false));
	}

	protected List<AudioTrack> getAll(@NonNull IPlayerView playerView, boolean showSupportedTracks) {
		List<AudioTrack> audioTracks = playerView.getPlayerController().getAudioTracks();
		if(showSupportedTracks || getArguments().getBoolean(ARG_SHOW_SUPPORTED, false)){
			List<AudioTrack> audioFilteredTracks = new ArrayList<>();
			for (AudioTrack audioTrack : audioTracks) {
				try {
					if (audioTrack.isSupportedCodec()) {
						audioFilteredTracks.add(audioTrack);
					}
				} catch (MediaCodecUtil.DecoderQueryException e) {
					// ignore this track
				}
			}
			return audioFilteredTracks;
		}
		return audioTracks;
	}

	@Override
	protected void applySelection(@NonNull IPlayerView playerView, AudioTrack selectedValue) {
		playerView.getPlayerController().setAudioTrack(selectedValue);
	}
}


