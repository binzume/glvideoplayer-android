package net.binzume.android.glvideoplayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * 普通っぽいシェーダ
 * 
 * colorの値
 * 
 * @author kosuke_kawahira
 */
public class GLShaderProgram {

	public int program;

	// ハンドル
	public int modelMatrixHandle; // モデルビュー行列
	public int texMatrixHandle;// テクスチャ行列
	public int positionHandle; // 頂点
	public int colorHandle; // 色
	public int uvHandle; // UV
	public int texHandle; // テクスチャ
	public int texExtHandle; // テクスチャ

	public float[] projectionMatrix = new float[16];
	public float[] modelViewProjectionMatrix = new float[16];

	public GLShaderProgram(Context context, int vResId, int fResId) {
		makeProgram(loadString(context, vResId), loadString(context, fResId));
	}

	private String loadString(Context context, int resId) {
		try {
			Resources r = context.getResources();
			BufferedReader reader = new BufferedReader(new InputStreamReader(r.openRawResource(resId), "UTF-8"));
			StringBuffer buf = new StringBuffer();
			String str;
			while ((str = reader.readLine()) != null) {
				buf.append(str);
				buf.append("\n");
			}
			return buf.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// プログラムの生成
	private void makeProgram(String vs, String fs) {
		// シェーダーオブジェクトの生成
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vs);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fs);

		// プログラムオブジェクトの生成
		program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);

		Log.d("", GLES20.glGetShaderInfoLog(vertexShader));
		Log.d("", GLES20.glGetShaderInfoLog(fragmentShader));

		// ハンドル取得
		modelMatrixHandle = GLES20.glGetUniformLocation(program, "u_MMatrix");
		texMatrixHandle = GLES20.glGetUniformLocation(program, "u_TexMatrix");
		positionHandle = GLES20.glGetAttribLocation(program, "a_Position");
		uvHandle = GLES20.glGetAttribLocation(program, "a_UV");
		texHandle = GLES20.glGetUniformLocation(program, "u_Texture");
		texExtHandle = GLES20.glGetUniformLocation(program, "u_TextureEXT");
		colorHandle = GLES20.glGetUniformLocation(program, "u_Color");

		GLES20.glUseProgram(program);
	}

	// シェーダー読み込み
	private static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}

	// GL10 compat
	public void glTexCoordPointer(int size, int type, int stride, FloatBuffer pointer) {
		GLES20.glVertexAttribPointer(uvHandle, size, type, false, stride, pointer);
	}

	public void glColor4f(float r, float g, float b, float a) {
		GLES20.glEnableVertexAttribArray(colorHandle);
		GLES20.glUniform4f(colorHandle, r, g, b, a);
	}

	public void glVertexPointer(int size, int type, int stride, FloatBuffer pointer) {
		GLES20.glVertexAttribPointer(positionHandle, size, type, false, stride, pointer);
	}

	public void glEnableClientState(int array) {
		GLES20.glEnableVertexAttribArray(array == GL10.GL_TEXTURE_COORD_ARRAY ? uvHandle : positionHandle);
	}

	public void setProjectionMatrix(float[] mat) {
		System.arraycopy(mat, 0, projectionMatrix, 0, 16);
		System.arraycopy(mat, 0, modelViewProjectionMatrix, 0, 16);
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, mat, 0);
	}

	public void scale(float scale) {
		Matrix.scaleM(modelViewProjectionMatrix, 0, scale, scale, 1.0f);
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
	}

	public void scale(float scaleX, float scaleY) {
		Matrix.scaleM(modelViewProjectionMatrix, 0, scaleX, scaleY, 1.0f);
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
	}

	public void rotate(float a) {
		Matrix.rotateM(modelViewProjectionMatrix, 0, a, 0, 0, 1.0f);
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
	}

	public void translate(float x, float y) {
		Matrix.translateM(modelViewProjectionMatrix, 0, x, y, 0);
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
	}

	public void resetMatrix() {
		System.arraycopy(projectionMatrix, 0, modelViewProjectionMatrix, 0, 16);
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
	}

}
