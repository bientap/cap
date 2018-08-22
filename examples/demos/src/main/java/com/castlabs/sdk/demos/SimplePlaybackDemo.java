package com.castlabs.sdk.demos;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.castlabs.android.player.AbstractPlayerListener;
import com.castlabs.android.player.PlaybackState;
import com.castlabs.android.player.PlayerView;
import com.castlabs.android.player.exceptions.CastlabsPlayerException;
import com.castlabs.android.views.SubtitlesViewComponent;
import com.castlabs.sdk.playerui.PlayerControllerProgressBar;
import com.castlabs.sdk.playerui.PlayerControllerView;
import com.google.android.exoplayer2.ExoPlaybackException;

public class SimplePlaybackDemo extends Activity {
	private static final String TAG = "SimplePlaybackDemo";
	private static final String SAVED_PLAYBACK_STATE_BUNDLE_KEY = "SAVED_PLAYBACK_STATE_BUNDLE_KEY";
	// This is the player view that we use to start playback
	private PlayerView playerView;

	// This is an example implementation that demonstrates how you can
	// listen to changes on the player controller and how you can access the
	// SubtitleViewComponent to apply adjustments.
	//
	// In this example we add some padding to move the subtitles view up a bit when
	// we are showing the controller.
	private PlayerControllerView.Listener playerControllerViewListener = new PlayerControllerView.Listener() {
		@Override
		public void onVisibilityChanged(int visibility) {
			// This is how you can get a component, in this case the SubtitlesViewComponent
			// from the player view.
			SubtitlesViewComponent svc = playerView.getComponent(SubtitlesViewComponent.class);
			if (svc != null) {
				if (visibility == View.VISIBLE) {
					svc.componentView().view.setPadding(0, 0, 0, playerControllerView.getHeight());
				} else {
					svc.componentView().view.setPadding(0, 0, 0, 0);
				}
			}
		}
	};
	private PlayerControllerView playerControllerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("","22222222222222222222222222222222222222");
		setContentView(R.layout.activity_simple_playback_demo);

		// Get the view components from the layout
		playerView = (PlayerView) findViewById(R.id.player_view);
		// Get the controller view
		playerControllerView = (PlayerControllerView) findViewById(R.id.player_controls);

		Bundle playbackBundle = null;
		if (savedInstanceState == null) {
			Log.d(TAG, "Opening playback from intent bundle");
			// This demo assumes that you send an intent to this Activity that contains the
			// playback information.
			if (getIntent() != null) {
				playbackBundle = getIntent().getExtras();
			} else {
				Snackbar.make(playerView, "No intent specified", Snackbar.LENGTH_INDEFINITE).show();
			}
		} else {
			Log.d(TAG, "Opening playback from saved state bundle");
			playbackBundle = savedInstanceState.getBundle(SAVED_PLAYBACK_STATE_BUNDLE_KEY);
		}

		if (playbackBundle != null) {
			try {
				//playerView.getPlayerController().addPlayerListener();
				playerView.getPlayerController().addPlayerListener(new AbstractPlayerListener() {
					@Override
					public void onError(@NonNull CastlabsPlayerException error) {
						if (error.getType() == CastlabsPlayerException.TYPE_BEHIND_LIVE_WINDOW){
							Log.e(TAG, "Error type: hhhhhhhhhhhhhhhhhhhhhhhhhh");
							playerView.getPlayerController().play();
							return;
						}
						Snackbar.make(playerView, "Error while opening player: " + error.getType()+ " ; " + error.getMessage(),
								Snackbar.LENGTH_INDEFINITE).show();
					}
				});
				// Need to pass the bundle on to the PlayerController
				// to start playback. The open() method might throw an Exception in case the bundle
				// contains not all mandatory parameters or the parameters are malformed.
				playerView.getPlayerController().open(playbackBundle);
			} catch (Exception e) {
				Log.e(TAG, "Error while opening player: " + e.getMessage(), e);
				Snackbar.make(playerView, "Error while opening player: " + e.getMessage(),
						Snackbar.LENGTH_INDEFINITE).show();
			}
		} else {
			Snackbar.make(playerView, "Can not start playback: no bundle specified", Snackbar.LENGTH_INDEFINITE).show();
		}
	}

	// Delegate the onStart event to the player views lifecycle delegate.
	// The delegate will make sure that the screen safer will be disabled and
	// the display will not go to sleep
	@Override
	protected void onStart() {
		super.onStart();
		playerView.getLifecycleDelegate().start(this);
	}

	// Delegate the onResume event to the player views lifecycle delegate.
	// The delegate ensures that the player recovers from a saved state. This needs to
	// be implemented to ensure the the user can for example go to the home screen and
	// come back to this activity.
	@Override
	protected void onResume() {
		super.onResume();
		// Bind the controller view and its listener
		playerControllerView.bind(playerView);
		playerControllerView.addListener(playerControllerViewListener);

		PlayerControllerProgressBar progressBar = (PlayerControllerProgressBar) findViewById(R.id.progress_bar);
		progressBar.bind(playerView.getPlayerController());

		playerView.getLifecycleDelegate().resume();
	}

	// Delegate the onStop event to the player views lifecycle delegate.
	// We release the player when the activity is stopped. This will release all the player
	// resources and save the current playback state. Saving the state is required so the
	// onResume callback can recover properly.
	@Override
	protected void onStop() {
		super.onStop();

		// Unbind the player controller view and remove the listener
		playerControllerView.unbind();
		playerControllerView.removeListener(playerControllerViewListener);

		playerView.getLifecycleDelegate().releasePlayer(false);
	}

	// Save the playback state when the activity is destroyed in order to correctly
	// resume after the activity is re-created again i.e. onCreate is called
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Bundle savedStateBundle = new Bundle();
		PlaybackState playbackState = playerView.getPlayerController().getPlaybackState();
		if (playbackState != null) {
			playerView.getPlayerController().getPlaybackState().save(savedStateBundle);
			outState.putBundle(SAVED_PLAYBACK_STATE_BUNDLE_KEY, savedStateBundle);
		}
		super.onSaveInstanceState(outState);
	}
}
