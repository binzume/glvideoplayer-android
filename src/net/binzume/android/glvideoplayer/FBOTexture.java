package net.binzume.android.glvideoplayer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

public abstract class FBOTexture {
	protected SurfaceTexture surfaceTexture;
	protected boolean textureUpdated;
	public int textureName = -1;

	private int FBO;
	private int RBOD;
	

	protected SurfaceTexture createSurfaceTexture(int width, int height) {

		// color
		int ret[] = new int[1];
		GLES20.glGenFramebuffers(1, ret, 0);
		FBO = ret[0];
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBO);
		
		// depth
		GLES20.glGenRenderbuffers(1, ret, 0);
		RBOD = ret[0];
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, RBOD);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, RBOD);
		
		
		int tex[] = new int[1];
		GLES20.glGenTextures(1, tex, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex[0], 0);

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		
		textureName = tex[0];
		final SurfaceTexture t = new SurfaceTexture(tex[0]);
		t.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
			
			@Override
			public void onFrameAvailable(SurfaceTexture surfaceTexture) {
				textureUpdated = true;
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
