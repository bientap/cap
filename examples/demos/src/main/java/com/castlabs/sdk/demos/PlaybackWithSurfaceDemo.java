package com.castlabs.sdk.demos;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.castlabs.android.player.AbstractPlayerListener;
import com.castlabs.android.player.PlayerController;
import com.castlabs.sdk.subtitles.SubtitleViewWrapper;

/**
 * This class showcases how to programmatically create a {@link PlayerController} and how to
 * attach to it a {@link android.view.Surface}.
 *
 * {@link SubtitleViewWrapper} is also used to show subtitles in an independent view.
 */
public class PlaybackWithSurfaceDemo extends Activity implements SurfaceHolder.Callback {

	PlayerController playerController;
	private SurfaceView surfaceView;
	private SubtitleViewWrapper subtitleViewWrapper;
	private RelativeLayout mainLayout;
	private float videoAspectRatio = 0f;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("","111111111111111111111111111111111");
		setContentView(R.layout.playback_surface);
		playerController = new PlayerController(this);
		surfaceView = findViewById(R.id.surfaceView);
		mainLayout = findViewById(R.id.mainLayout);
		subtitleViewWrapper = findViewById(R.id.castlabsSubtitleView);
		subtitleViewWrapper.bindToPlayerController(playerController);
		surfaceView.getHolder().addCallback(this);


		playerController.addPlayerListener(new AbstractPlayerListener() {
			@Override
			public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
				videoAspectRatio = (width * pixelWidthHeightRatio) / height;
				setSurfaceSize();
			}
		});

		try {
			final Bundle extras = getIntent().getExtras();
			playerController.open(extras);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setSurfaceSize() {
		if (videoAspectRatio == 0) {
			return;
		}
		int w = mainLayout.getWidth();
		int h = mainLayout.getHeight();

		float viewAR = (float) w / h;

		if (viewAR < videoAspectRatio) {
			// view is "taller" than video, adjust height
			h = (int) (w / videoAspectRatio);
		} else if (viewAR > videoAspectRatio) {
			// view is "wider" than video, adjust width
			w = (int) (h / videoAspectRatio);
		}
		surfaceView.setLayoutParams(new FrameLayout.LayoutParams(w, h));
		surfaceView.invalidate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		final View view = findViewById(R.id.surfaceView);
		ViewTreeObserver observer = view.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				setSurfaceSize();
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		surfaceView.getHolder().removeCallback(this);
		subtitleViewWrapper.unbindFromPlayerController();
		playerController.release();
		super.onDestroy();
	}

	// SurfaceHolder callback
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		playerController.setSurface(holder.getSurface());
		setSurfaceSize();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		subtitleViewWrapper.setMeasuredVideoDimensions(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		playerController.setSurface(null);
	}
}
