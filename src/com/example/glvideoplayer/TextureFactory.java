package com.example.glvideoplayer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TextureFactory {

	/**
	 * 
	 * @param gl_
	 *            GLES1.xを使ってた時の名残．GLES2.0の場合使わない
	 * @param bitmap
	 * @return
	 */
	public static Texture createFromBitmap(GL10 gl_, Bitmap bitmap) {

		int[] textureID = new int[1];
		GLES20.glGenTextures(1, textureID, 0);
		int textureNo = textureID[0];
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textureNo);
		GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

		float bmpSize = Math.max(bitmap.getWidth(), bitmap.getHeight());
		final float texSize;
		if (bmpSize <= 256) {
			texSize = 256.0f;
		} else if (bmpSize <= 512) {
			texSize = 512.0f;
		} else {
			texSize = 1024.0f;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(texSize / bitmap.getWidth(), texSize / bitmap.getHeight());
		Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
		bmp.recycle();

		return new Texture(textureNo, bitmap.getWidth(), bitmap.getHeight());
	}

	public static Texture createFromResource(Context context, int resId) {
		return createFromBitmap(null, BitmapFactory.decodeResource(context.getResources(), resId));
	}
}
