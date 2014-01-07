package net.binzume.android.nicovideo;

import java.util.List;

/**
 * メッセージサーバースレッド
 * 
 * @author Kousuke Kawahira
 */
public class ThreadInfo {
	public final long threadId;
	public String messageServerUrl;
	public boolean force184;
	public String ticket;
	public int lastRes;
	public boolean needsKey;
	public List<Comment> comments;
	
	public ThreadInfo(long threadId) {
		this.threadId = threadId;
	}
}
