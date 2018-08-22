package com.castlabs.sdk.demos;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;

import com.castlabs.android.SdkConsts;
import com.castlabs.android.player.PlayerService;
import com.castlabs.android.player.PlayerView;

public class SimplePlayServiceDemo extends Activity {
	// ID that we use to identify the background notifications
	private static final int NOTIFICATION_ID = 1;
	// We use this main handler to post delayed messaged to hide the system
	// controls after 3 seconds
	private final Handler handler = new Handler();
	// This is the player view that we use to start playback
	private PlayerView playerView;
	// This is the service binder that we get once we connected to the service
	private PlayerService.Binder playerServiceBinder;
	// We store the bundle that contains the playback information here
	// so the service can use it once connected
	private Bundle playbackBundle;
	// The service connection
	//
	// The connection is responsible to wither start playback or recover from background
	// playback
	private final ServiceConnection playerServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			// get the service binder and initialize the service
			playerServiceBinder = (PlayerService.Binder) service;
			// Initialize the service binder. This will return true if
			// playback was recovered from background.
			boolean backgrounded = playerServiceBinder.init(playerView);

			// from now on you can use playerView.getPlayerController() to
			// interact with the controller
			if (!backgrounded) {
				if (playbackBundle != null) {
					// The player was not restored from background and we have a bundle to open.
					// This indicates that we came from an onCreate() call instead of resuming
					// an existing activity
					try {
						playerView.getPlayerController().open(playbackBundle);
					} catch (Exception e) {
						Snackbar.make(playerView,
								"Error while opening playback bundle: " + e.getMessage(),
								Snackbar.LENGTH_INDEFINITE).show();
					}
				} else {
					// the player was not restored from background and no bundle was passed.
					// In this case we delegate to the views lifecycle delegate to eventually
					// restore a session
					playerView.getLifecycleDelegate().resume();
				}
			}
			playbackBundle = null;
		}

		public void onServiceDisconnected(ComponentName name) {
			playerServiceBinder.release(playerView, true);
			playerServiceBinder = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("","33333333333333333333333333333333333");
		setContentView(R.layout.activity_simple_playback_demo);

		// Get the view components from the layout
		playerView = (PlayerView) findViewById(R.id.player_view);

		// Hide the system toolbars
		//
		// We use the system UI flags to put this activity into a stable layout state
		// and hide the system toolbars. This ensures that we can use the full real estate
		// to show the video playback but avoid going into immersive fullscreen mode.
		//
		// With this setting, the system toolbars (the status bar and the navigation controls)
		// will become visible if the user interacts with the activity. In this demo, we
		// simply hide the controls again again after 3 seconds.
		hideSystemControls();
		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
				new View.OnSystemUiVisibilityChangeListener() {
					@Override
					public void onSystemUiVisibilityChange(int visibility) {
						if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
							// The navigation bar is no longer hidden, so we hide it
							// again in a few seconds
							delayedHideSystemControls();
						}
					}
				});

		// In this demo, we are using a background service so we can not start interacting
		// with the player controller before the service is connected and initialised.
		// We connect the service in onResume() and store the playback bundle here
		// so the service can use it to start playback later.
		if (getIntent() != null) {
			playbackBundle = getIntent().getExtras();
		} else {
			Snackbar.make(playerView, "No intent specified", Snackbar.LENGTH_INDEFINITE).show();
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

	// Because we are using a service, we are NOT delegating directly to the player views lifecycle
	// event. Instead, we make sure that a connection to the player service is established. The
	// binder implementation will then take care of initializing playback or to recover from
	// a background playback session (see the implementation of playerServiceConnection).
	@Override
	protected void onResume() {
		super.onResume();
		Intent serviceIntent = new Intent(this, PlayerService.class);
		bindService(serviceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
	}

	// With the player service, we have to cover a few use cases when the activity is stopped.
	//
	// 1) If we have no service connection, we delegate to the lifecycle delegate of the
	//    view to release the player and any locks acquired when the activity started.
	//
	// 2) If isFinishing() returns true, we assume the user wants to actually leave the
	//    activity and not start background playback. In that case we release the player
	//    through the binder implementation
	// 3) If the activity is not finishing, we send the player to the background using the
	//    service binder implementation
	//
	// In both cases, we unbind from the service and reset the binder.
	@Override
	protected void onStop() {
		super.onStop();
		if (playerServiceBinder == null) {
			// We have no service connection, so we release the player without background playback
			playerView.getLifecycleDelegate().releasePlayer(false);
		} else {
			if (!isFinishing()) {
				// The activity is not finishing but left through other means, i.e. Home button.
				// In this case, we send the player to background and display a notification
				playerServiceBinder.releaseToBackground(
						playerView,
						NOTIFICATION_ID,
						createBackgroundNotification(),
						true);
			} else {
				// We are not sending the player to background since the activity is finishing
				// so we fully release the player
				playerServiceBinder.release(playerView, true);
			}
			// in both cases we we unbind the service
			unbindService(playerServiceConnection);
			playerServiceBinder = null;
		}
	}

	// Utility function that we use to create a dummy notification that is displayed when the
	// player is going to background
	private Notification createBackgroundNotification() {
		Bundle openContentBundle = new Bundle();
		playerView.getPlayerController().saveState(openContentBundle);

		Intent targetIntent = new Intent(getApplicationContext(), getClass());
		targetIntent.putExtras(openContentBundle);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(getClass());
		stackBuilder.addNextIntent(targetIntent);
		PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(getApplicationContext());
		builder.setAutoCancel(false);
		builder.setOngoing(true);
		builder.setContentTitle("Background Playback");
		builder.setContentText(openContentBundle.getString(SdkConsts.INTENT_URL));
		builder.setSmallIcon(R.drawable.notofications_icon);
		builder.setContentIntent(pi);

		return builder.build();
	}


	// Utility method that hides the system controls and makes sure
	// the player view can use all available screen real estate.
	private void hideSystemControls() {
		final View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_FULLSCREEN |
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
						View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	private void delayedHideSystemControls() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hideSystemControls();
			}
		}, 3000);
	}
}
