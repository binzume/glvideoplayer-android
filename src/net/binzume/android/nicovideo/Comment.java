package net.binzume.android.nicovideo;

/**
 * コメント
 */
public class Comment {
	/**
	 * @param message メッセージ
	 * @param mail コマンド
	 * @param vpos コメントの位置 (1/100s単位)
	 */
	public Comment(String message, String mail, int vpos) {
		this.message = message;
		this.mail = mail;
		this.vpos = vpos;
	};

	public final String message;
	public final String mail;
	public final int vpos;
	public int no;
	public String userId;
	public int premium;
}
