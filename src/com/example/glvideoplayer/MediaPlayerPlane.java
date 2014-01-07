package com.example.glvideoplayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

public class MediaPlayerPlane extends OESTexture {
	private FloatBuffer vertex;
	public MediaPlayer mediaPlayer;
	
	public MediaPlayerPlane(Context context, Uri uri) {
		try {
			
			mediaPlayer = new MediaPlayer();
			textureUpdated = false;
			surfaceTexture = createSurfaceTexture();
			
			Log.d("MediaPlayerPlane","open: " + uri.toString());
			mediaPlayer.setDataSource(context, uri);
			mediaPlayer.setSurface(new Surface(surfaceTexture));
			mediaPlayer.prepare();
			
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.start();
				}
			});
			
			mediaPlayer.start();
			// camera.setPreviewDisplay(null);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteBuffer bb = ByteBuffer.allocateDirect(4 * 4 * 4);
		bb.order(ByteOrder.nativeOrder());
		vertex = bb.asFloatBuffer();
		
		vertex.position(0);

		float bottom = 0f;
		float left = 0f;
		float sz = 1f;
		
		vertex.put(left);
		vertex.put(bottom);
		vertex.put(0);
		vertex.put(1);

		vertex.put(left);
		vertex.put(bottom + sz);
		vertex.put(0);
		vertex.put(0);

		vertex.put(left + sz);
		vertex.put(bottom + sz);
		vertex.put(1);
		vertex.put(0);

		vertex.put(left + sz);
		vertex.put(bottom);
		vertex.put(1);
		vertex.put(1);

		vertex.position(0);

	}
	
	@Override
	public void bindBuffer(GLShaderProgram glsl) {

		GLES20.glEnableVertexAttribArray(glsl.positionHandle);
		vertex.position(0);
		GLES20.glVertexAttribPointer(glsl.positionHandle, 4, GLES20.GL_FLOAT, false, 0, vertex);
//		checkGlError("drawGLES20 VertexAttribPointer bg vertex");

		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureName);
		
		// Log.d("","bindBuffer" + cameraTextureName);
	
	}
	
	@Override
	public void release() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer = null;
		}
		
		super.release();
	}
	
	public float getAspectRatio() {
		if (mediaPlayer == null) {
			return 1.0f;
		}
		
		return mediaPlayer.getVideoWidth() * 1f / mediaPlayer.getVideoHeight();
	}

}
