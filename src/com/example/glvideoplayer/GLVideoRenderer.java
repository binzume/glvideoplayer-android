package com.example.glvideoplayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

/**
 * OpenGL使ってvideoをレンダリングする
 * 
 * @author kosuke_kawahira
 */
public class GLVideoRenderer implements GLSurfaceView.Renderer {

	private final Context context;
	private int screenWidth, screenHeight;
	private int width, height;
	public GLShaderProgram glProgram;
	private int frame = 0;
	
	public boolean commentVisible = false;
	public boolean cameraVisible = false;

	public OESTexture videoTexture;
	public NicoCommentPlane commentTexture;
	private ShortBuffer mBgIndex;

	public GLVideoRenderer(Context context) {
		this.context = context;
	}
	
	private Uri videoUri;
	
	public void setVideoUri(Uri uri) {
		videoUri = uri;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		int[] version = new int[2];
		egl.eglInitialize(dpy, version);

		checkGlError("init0");

		glProgram = new GLShaderProgram(context, R.raw.plane_v, R.raw.oculus_f);

		GLES20.glClearColor(0, 0, 0.3f, 1.0f);
		//GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		//GLES20.glEnable(GLES20.GL_CULL_FACE);
		//GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		//GLES20.glFrontFace(GLES20.GL_CW);

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		// GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		GLES20.glDisable(GLES20.GL_DITHER);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		width = 1280;
		height = 720;

		checkGlError("init");
	}

	public void destroy() {
		if (videoTexture != null) {
			videoTexture.release();
			videoTexture = null;
		}
		if (commentTexture != null) {
			commentTexture.release();
			commentTexture = null;
		}
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// 描画
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT);
		// GLES20.glEnable(GL10.GL_TEXTURE_2D);
		// GLES20.glActiveTexture(GL10.GL_TEXTURE0);

		frame++;

		if (videoTexture != null) {
			videoTexture.update();
			commentTexture.update();
		}

		if (videoTexture == null && frame > 20 && cameraVisible) {
			DeviceCameraPlane cameraPlane = new DeviceCameraPlane();
			this.videoTexture = cameraPlane;
			Camera camera = cameraPlane.camera;
			if (camera != null) {
				camera.setErrorCallback(new Camera.ErrorCallback() {
					@Override
					public void onError(int error, Camera camera) {
						Log.d("CameraView", "onError: error=" + error);
					}
				});
				Camera.Parameters params = camera.getParameters();
				cameraPlane.setPreviewSize(params, 640, 480);
				Log.d("CameraView", "Start preview");
				camera.startPreview();
			}
		}

		if (videoTexture == null && videoUri != null) {
			this.videoTexture = new MediaPlayerPlane(context, videoUri);
			commentTexture = new NicoCommentPlane();
		}

		if (videoTexture != null && videoTexture.textureName > 0) {
			GLES20.glUseProgram(glProgram.program);

			

			if (mBgIndex == null) {
				ByteBuffer bb = ByteBuffer.allocateDirect(6 * 2);
				bb.order(ByteOrder.nativeOrder());
				mBgIndex = bb.asShortBuffer();

				mBgIndex.put((short) 3);
				mBgIndex.put((short) 1);
				mBgIndex.put((short) 0);

				mBgIndex.put((short) 3);
				mBgIndex.put((short) 2);
				mBgIndex.put((short) 1);
				mBgIndex.flip();
			}
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			videoTexture.bindBuffer(glProgram);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
			commentTexture.bindBuffer(glProgram);
			
			float aspect = 4.0f / 3.0f;
			if (videoTexture instanceof MediaPlayerPlane) {
				aspect = ((MediaPlayerPlane) videoTexture).getAspectRatio();
			}

			glProgram.resetMatrix();
			glProgram.translate(0, height+ 40);
			glProgram.scale(640, -height);
			glProgram.glColor4f(2.0f, +0.1f, 1.0f, aspect);

			GLES20.glUniform1i(glProgram.texExtHandle, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mBgIndex);

			if (commentVisible) {
				GLES20.glUniform1i(glProgram.texExtHandle, 2);
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mBgIndex);
			}

			
			glProgram.resetMatrix();
			glProgram.translate(640, height + 40);
			glProgram.scale(640, -height);
			glProgram.glColor4f(2.0f, -0.1f, 1.0f, aspect);

			GLES20.glUniform1i(glProgram.texExtHandle, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mBgIndex);
		
			if (commentVisible) {
				GLES20.glUniform1i(glProgram.texExtHandle, 2);
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mBgIndex);
			}
			
		}

		checkGlError("drawFrame");
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		setRenderSize(screenWidth, screenHeight);

		checkGlError("change");
	}

	private void setRenderSize(int screenWidth, int screenHeight) {

		float scale = (float) screenWidth / width;
		if (height * scale > screenHeight) {
			scale = (float) screenHeight / height;
		}

		// 左上原点
		float[] mat = new float[16];
		android.opengl.Matrix.setIdentityM(mat, 0);
		android.opengl.Matrix.orthoM(mat, 0, 0, screenWidth, screenHeight, 0, -1, 1);
		// android.opengl.Matrix.translateM(mat, 0, 0, screenHeight - height * scale, 0);
		android.opengl.Matrix.scaleM(mat, 0, scale, scale, 1.0f);

		GLES20.glViewport(0, 0, screenWidth, screenHeight);
		glProgram.setProjectionMatrix(mat);

		android.opengl.Matrix.setIdentityM(mat, 0);
		GLES20.glUniformMatrix4fv(glProgram.texMatrixHandle, 1, false, mat, 0);

		// this.scale = scale;

	}

	private void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("opengl", op + ": glError " + error);
		}
	}

}
