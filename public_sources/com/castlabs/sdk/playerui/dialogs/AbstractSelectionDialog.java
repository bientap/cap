package com.castlabs.sdk.playerui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.castlabs.utils.Log;

import com.castlabs.PublishedSource;
import com.castlabs.android.player.IPlayerView;
import com.castlabs.android.views.PlayerViewProvider;
import com.castlabs.utils.Converter;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * This is a base class for utility selection dialogs that can be used to show and apply a selection
 * if items. Subclasses can set the player view reference explicitly when instance are created, but
 * for better compatibility with the fragment system, we encourage you to let the
 * hosting Activity implement {@link PlayerViewProvider}. That way dialogs can be recreated by
 * by the system and will still be able to resolve the player view to apply selections.
 *
 * @param <T> The source type
 */
@PublishedSource
public abstract class AbstractSelectionDialog<T> extends DialogFragment implements DialogInterface.OnClickListener {
	private static final String TAG = "AbstractSelectionDialog";
	/**
	 * The argument key for the dialog title
	 */
	public static final String ARG_TITLE = "title";
	/**
	 * The argument key to store the items
	 */
	public static final String ARG_ITEMS = "items";
	/**
	 * The argument key to store the currently selected item
	 */
	public static final String ARG_SELECTED = "selected";
	/**
	 * The argument key to store if disabling is allowed and an extra item will be displayed at
	 * the top of the list
	 */
	public static final String ARG_ALLOW_DISABLE = "allowDisable";

	@Nullable
	protected WeakReference<IPlayerView> playerViewRef;

	/**
	 * Implementations should return the currently selected value or null if no value is selected
	 *
	 * @param playerView the player view
	 * @return The currently selected value
	 */
	protected abstract T getCurrent(@NonNull IPlayerView playerView);

	/**
	 * Implementations should return a list of all available items
	 *
	 * @param playerView the player view
	 * @return All available items
	 */
	protected abstract List<T> getAll(@NonNull IPlayerView playerView);

	/**
	 * Implementations shoudl apply the selection. The selected value will be null if disabling
	 * is enabled and the "disable" item was selected
	 *
	 * @param playerView the player view
	 * @param selectedValue the selected item or null the "disable" item was selected
	 */
	protected abstract void applySelection(@NonNull IPlayerView playerView, @Nullable T selectedValue);

	/**
	 * If the parent activity implements {@link PlayerViewProvider}, the provider is used to
	 * return the player view, otherwise this returns the {@link IPlayerView} instance
	 * that is bound to this dialog.
	 *
	 * @return The player view instance
	 */
	@Nullable
	public IPlayerView getPlayerView() {
		if(getActivity() instanceof PlayerViewProvider){
			return ((PlayerViewProvider) getActivity()).getPlayerView();
		}
		if(this.playerViewRef != null){
			return this.playerViewRef.get();
		}
		return null;
	}

	/**
	 * Set the player view reference
	 *
	 * @param playerView The player view reference
	 */
	public void setPlayerView(@Nullable IPlayerView playerView){
		if(this.playerViewRef != null){
			this.playerViewRef.clear();
		}
		if(playerView != null) {
			this.playerViewRef = new WeakReference<>(playerView);
		}
	}

	protected Items collectItems(T current, List<T> all, Converter<T, String> converter, boolean allowToDisable) {
		if (converter == null) converter = new ToStringConverter<>();
		int disabledTrackOffset = allowToDisable ? 1 : 0;
		Items items = new Items();
		items.items = new String[all.size() + disabledTrackOffset];
		items.selectedItem = current == null && allowToDisable ? 0 : -1;
		if (allowToDisable) {
			String converted = converter.convert(null);
			if (converted == null) {
				converted = "Unknown";
			}
			items.items[0] = converted;
		}

		for (int i = 0; i < items.items.length - disabledTrackOffset; i++) {
			T audioTrack = all.get(i);
			String converted = converter.convert(audioTrack);
			if (converted == null) {
				converted = "Unknown";
			}
			items.items[i + disabledTrackOffset] = converted;
			if (audioTrack.equals(current)) {
				items.selectedItem = i + disabledTrackOffset;
			}
		}
		return items;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString(ARG_TITLE, "Select");
		String[] items = getArguments().getStringArray(ARG_ITEMS);
		int selectedItem = getArguments().getInt(ARG_SELECTED, -1);

		// create the list of items including the disabled item
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title).setSingleChoiceItems(items, selectedItem, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		IPlayerView playerView = getPlayerView();
		if(playerView == null){
			Log.e(TAG, "No PlayerView could be resolved. Unable to apply click selection!");
			getDialog().dismiss();
			return;
		}
		boolean allowToDisable = getArguments().getBoolean(ARG_ALLOW_DISABLE, true);
		if (allowToDisable && which == 0) {
			applySelection(playerView, null);
		} else {
			applySelection(playerView, getAll(playerView).get(which - (allowToDisable ? 1 : 0)));
		}
		getDialog().dismiss();
	}

	/**
	 * Converter implementation that delegates to {@code toString()} for the source objects.
	 * You can configure the value that will be used for null values.
	 *
	 * @param <T> The source type
	 */
	protected static class ToStringConverter<T> implements Converter<T, String> {

		private String nullValue;

		/**
		 * Create a new instance that uses "Disable" for null values
		 */
		public ToStringConverter() {
			this("Disable");
		}

		/**
		 * Create a new instance and use the given string for null values
		 *
		 * @param nullValue the string used for null values
		 */
		public ToStringConverter(String nullValue) {
			this.nullValue = nullValue;
		}

		@Nullable
		@Override
		public String convert(@Nullable T source) {
			return source == null ? nullValue : source.toString();
		}
	}

	/**
	 * Utility class that is used to collect items and the currently selected item
	 */
	class Items {
		String[] items;
		int selectedItem;
	}
}
