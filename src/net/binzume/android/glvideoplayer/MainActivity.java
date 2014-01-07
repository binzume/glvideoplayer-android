package net.binzume.android.glvideoplayer;

import net.binzume.android.glvideoplayer.R;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.TargetApi;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private GLSurfaceView glview;
	private GLVideoRenderer renderer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getActionBar().hide();

		glview = (GLSurfaceView) findViewById(R.id.glview);
		glview.setEGLContextClientVersion(2);
		
		renderer = new GLVideoRenderer(this);
		
		if (getIntent().getData() != null) {
			renderer.setVideoUri(getIntent().getData());
		} else {
			String path = Environment.getExternalStorageDirectory() + "/Android/data/net.binzume.android.nicoplayer/sm9.mp4";
			renderer.setVideoUri(Uri.parse("file://"+path));
		}
		
		glview.setRenderer(renderer);
		
		glview.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (renderer.commentTexture != null) {
					renderer.commentTexture.draw();
				}
				glview.postDelayed(this, 100);
			}
		}, 100);
	}
	

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onStart() {
		super.onStart();
		glview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		glview.setKeepScreenOn(true);
	}

	@Override
	protected void onStop() {
		
		// renderer.destroy();

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (renderer != null && renderer.videoTexture != null) {
				int pos = ((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.getCurrentPosition();
				((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.seekTo(pos - 8000);;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (renderer != null && renderer.videoTexture != null) {
				int pos = ((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.getCurrentPosition();
				((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.seekTo(pos + 8000);;
			}
			break;
		case KeyEvent.KEYCODE_SPACE:
			if (renderer != null && renderer.videoTexture != null) {
				if (((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.isPlaying()) {
					((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.pause();
					glview.setSystemUiVisibility( View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				} else {
					((MediaPlayerPlane)renderer.videoTexture).mediaPlayer.start();
					glview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				}
			}
			break;
		case KeyEvent.KEYCODE_C:
			if (renderer != null) {
				renderer.commentVisible = !renderer.commentVisible;
			}
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

}
