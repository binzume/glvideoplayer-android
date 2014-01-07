package com.example.glvideoplayer;

/**
 * テクスチャ
 * 
 * @author kosuke_kawahira
 */
public class Texture {

	/**
	 * テクスチャ識別子
	 * 
	 * OpenGLのテクスチャ番号．
	 */
	public final int id;

	/**
	 * 元のビットマップの幅と高さ
	 */
	public final int width, height;

	public Texture(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}
}
