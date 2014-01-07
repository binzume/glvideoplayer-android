package net.binzume.android.glvideoplayer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class VideoSurfaceView extends GLSurfaceView {
	
	private Renderer renderer;

	public VideoSurfaceView(Context context) {
		super(context);
	}

	public VideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setRenderer(Renderer renderer) {
		super.setRenderer(renderer);
		this.renderer = renderer;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (renderer instanceof GLVideoRenderer) {
			((GLVideoRenderer) renderer).destroy();
		}
		
		super.surfaceDestroyed(holder);
	}

}
