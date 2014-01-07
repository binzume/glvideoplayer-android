package com.example.glvideoplayer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;

public abstract class OESTexture {
	protected SurfaceTexture surfaceTexture;
	protected boolean textureUpdated;
	public int textureName = -1;

	

	protected static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	protected SurfaceTexture createSurfaceTexture() {
		int tex[] = new int[1];
		GLES20.glGenTextures(1, tex, 0);
//		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, tex[0]);
		GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		textureName = tex[0];
		Log.d("OESTexture", "createSurfaceTexture textureName:" + textureName);
		final SurfaceTexture t = new SurfaceTexture(tex[0]);
		t.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
			
			@Override
			public void onFrameAvailable(SurfaceTexture surfaceTexture) {
				textureUpdated = true;
				// Log.d("OESTexture", "onFrameAvailable textureName:" + textureName);
			}
		});
		return t;
	}
	
	public void update() {
		if (textureUpdated) {
			textureUpdated = false;
			surfaceTexture.updateTexImage();
			// cameraTexture.getTransformMatrix(mtx);
		}
	}
	
	abstract public void bindBuffer(GLShaderProgram glsl);
	
	public void release() {
		if (textureName > 0) {
			GLES20.glDeleteTextures(0, new int[]{textureName}, 0);
			textureName = -1;
		}
		if (surfaceTexture != null) {
			surfaceTexture.release();
			surfaceTexture = null;
		}
	}

}
