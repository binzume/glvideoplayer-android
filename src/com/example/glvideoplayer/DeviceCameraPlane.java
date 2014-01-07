package com.example.glvideoplayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;

public class DeviceCameraPlane extends OESTexture {
	private FloatBuffer vertex;
	public Camera camera;
	
	public DeviceCameraPlane() {
		try {
			camera = Camera.open();
			textureUpdated = false;
			surfaceTexture = createSurfaceTexture();
			camera.setPreviewTexture(surfaceTexture);
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

		float bottom = -0.9f;
		float left = -0.9f;
		float sz = 1.8f;
		
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
	
	
	public void setPreviewSize(Camera.Parameters params, int width, int height) {
		List<Camera.Size> supported = params.getSupportedPreviewSizes();
		if (supported != null) {
			for (Camera.Size size : supported) {
				if (size.width <= width && size.height <= height) {
					params.setPreviewSize(size.width, size.height);
					Log.d("CameraView","PreviewSIze: "+size.width + "x" + size.height);
					camera.setParameters(params);
					break;
				}
			}
		}
	}
	
	
	public void bindBuffer(GLShaderProgram glsl) {

		GLES20.glEnableVertexAttribArray(glsl.positionHandle);
		vertex.position(0);
		GLES20.glVertexAttribPointer(glsl.positionHandle, 4, GLES20.GL_FLOAT, false, 0, vertex);
//		checkGlError("drawGLES20 VertexAttribPointer bg vertex");

		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureName);
		
		// Log.d("","bindBuffer" + cameraTextureName);
	
	}
	
	public void release() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		
		super.release();
	}
}
