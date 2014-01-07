package net.binzume.android.nicoplayer.player;

import android.os.AsyncTask;
import android.util.Log;
import net.binzume.android.nicoplayer.player.CommentController;
import net.binzume.android.nicovideo.ThreadInfo;
import net.binzume.android.nicovideo.webapi.*;

public class CommentLoadTask extends AsyncTask<ThreadInfo, Integer, ThreadInfo> {
	private CommentController activity;
	private final int leaves;
	private final int threshold;

	public CommentLoadTask(CommentController activity, int leaves, int threshold) {
		this.activity = activity;
		this.leaves = leaves;
		this.threshold = threshold;
		Log.d("nicoplayer", "thread");
	}

	@Override
	protected ThreadInfo doInBackground(ThreadInfo... params) {
		try {
			return CommentAPI.get(null, params[0], leaves, threshold);
		} catch (NotLoginException e) {
			// 無視
		} catch (OutOfMemoryError e) {
			// TODO: コメント数調整
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(ThreadInfo result) {
		Log.d("nicoplayer", "thread loaded");
		activity.setThreadInfo(result);
	}

}
