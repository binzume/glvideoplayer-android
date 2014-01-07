package net.binzume.android.glvideoplayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.os.Build;
import android.view.Surface;

public class NicoCommentPlane extends OESTexture {
	private FloatBuffer vertex;
	
	private Surface surface;
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public NicoCommentPlane() {
		

		try {
			
			textureUpdated = false;
			surfaceTexture = createSurfaceTexture();
			//surfaceTexture.attachToGLContext(textureName);
			surfaceTexture.setDefaultBufferSize(1024, 1024);
			surface = new Surface(surfaceTexture);
		} catch (RuntimeException e) {
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
	
	public void draw() {
		Canvas canvas = surface.lockCanvas(null);
		
		canvas.drawColor(0x000000);
		// Log.d("draw", "w:"+canvas.getWidth() + ",h:" + canvas.getHeight());
		
		Paint paint = new Paint();
		paint.setColor(0x22FF0F00);  // **A*GGBB
		paint.setTextSize(50);

		canvas.drawText("あいうえお", 400, 200, paint);
		canvas.drawText("うううううううううううううううううう", 200, 300, paint);
		canvas.drawText("悪霊退散悪霊退散", 100, 350, paint);
		
		surface.unlockCanvasAndPost(canvas);
		// Log.d("","draw" + textureName);
	}
	
	@Override
	public void bindBuffer(GLShaderProgram glsl) {
		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureName);


		GLES20.glEnableVertexAttribArray(glsl.positionHandle);
		vertex.position(0);
		GLES20.glVertexAttribPointer(glsl.positionHandle, 4, GLES20.GL_FLOAT, false, 0, vertex);
//		checkGlError("drawGLES20 VertexAttribPointer bg vertex");
		

		// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureName);
		
		//Log.d("","bindBuffer" + textureName);
	
	}
	
	@Override
	public void release() {
		if (surface != null) {
			surface.release();
			surface = null;
		}
		super.release();
	}

}
