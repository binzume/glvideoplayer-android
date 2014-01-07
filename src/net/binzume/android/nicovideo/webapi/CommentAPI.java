/**
 * コメント
 * 
 * @author binzume <kawahira__binzume.net>
 */

package net.binzume.android.nicovideo.webapi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.binzume.android.nicovideo.Comment;
import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.ThreadInfo;
import net.binzume.android.nicovideo.util.HtmlUtil;
import net.binzume.android.nicovideo.util.HttpClient;
import android.util.Log;

public class CommentAPI {

	private static final int MAX_COMMENT = 5000;

	public static ThreadInfo get(NicoSession session, ThreadInfo thread, int leaves, int threshold) throws NotLoginException {
		if (session == null)
			throw new NotLoginException();
		if (thread == null)
			return null;

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		int commentsByLeaf = 100;
		if (commentsByLeaf * leaves > MAX_COMMENT) {
			commentsByLeaf = MAX_COMMENT / leaves + 1;
		}
		String leaveParams = "0-" + leaves + ":" + commentsByLeaf;

		String threadKey = null;
		String force184 = "0";
		if (thread.needsKey) {
			if (!client.get("http://flapi.nicovideo.jp/api/getthreadkey?thread=" + thread.threadId)) {
				return null;
			}
			String tistr = client.getContent();
			HashMap<String, String> info = HtmlUtil.flashVarsToMap(tistr);
			force184 = info.get("force_184");
			threadKey = info.get("threadkey");
		}
		final String postdata;
		long userId = session.userId;
		if (threadKey != null && threadKey.length() > 0) {
			postdata = "<packet><thread thread=\"" + thread.threadId + "\" version=\"20090904\" user_id=\"" + userId + "\" threadkey=\"" + threadKey
					+ "\" force_184=\"" + force184 + "\" scores=\"1\"/><thread_leaves thread=\"" + thread.threadId + "\" user_id=\"" + userId
					+ "\" threadkey=\"" + threadKey + "\" force_184=\"" + force184 + "\" scores=\"1\">" + leaveParams + "</thread_leaves></packet>";
		} else {
			postdata = "<packet><thread thread=\"" + thread.threadId + "\" version=\"20090904\" user_id=\"" + userId
					+ "\" scores=\"1\"/><thread_leaves thread=\"" + thread.threadId + "\" user_id=\"" + userId + "\" scores=\"1\">" + leaveParams
					+ "</thread_leaves><thread thread=\"" + thread.threadId + "\" version=\"20061206\" res_from=\"-1000\" fork=\"1\"/></packet>";
		}
		// Log.d("nicoplayer_api", "get comments: " + postdata);

		try {
			client.post(thread.messageServerUrl, postdata);
		} catch (Exception e) {
			return null;
		}
		CharSequence data = client.getCharSequence();

		thread.force184 = "1".equals(force184);

		// fixme...
		{
			Pattern pattern = Pattern.compile("<thread[^>]*? last_res=\"(\\d+)\"");
			Matcher matcher = pattern.matcher(data);
			if (matcher.find()) {
				thread.lastRes = Integer.parseInt(matcher.group(1));
			}
		}
		{
			Pattern pattern = Pattern.compile("<thread[^>]*? ticket=\"(\\w+)\"");
			Matcher matcher = pattern.matcher(data);
			if (matcher.find()) {
				thread.ticket = matcher.group(1);
			}
		}

		LinkedList<Comment> comments = new LinkedList<Comment>();
		try {
			Pattern pattern = Pattern.compile("<chat([^>]*)>([^<]+)</chat>");
			Pattern ptnNo = Pattern.compile("no=\"(\\d+)\"");
			Pattern ptnVpos = Pattern.compile("vpos=\"(\\d+)\"");
			Pattern ptnMail = Pattern.compile("mail=\"([^\"]*)\"");
			Pattern ptnUser = Pattern.compile("user_id=\"([^\"]*)\"");
			Pattern ptnScore = Pattern.compile("score=\"(-?\\d+)\"");

			Matcher matcher = pattern.matcher(data);
			while (matcher.find()) {

				Matcher matcher2 = ptnMail.matcher(matcher.group(1));
				String mail = matcher2.find() ? matcher2.group(1) : "";

				matcher2 = ptnUser.matcher(matcher.group(1));
				String commentUserId = matcher2.find() ? matcher2.group(1) : null;
				
				matcher2 = ptnVpos.matcher(matcher.group(1));
				int vpos = matcher2.find() ? Integer.parseInt(matcher2.group(1)) : 0;

				matcher2 = ptnNo.matcher(matcher.group(1));
				int no = matcher2.find() ? Integer.parseInt(matcher2.group(1)) : 0;
	
				boolean fork = (matcher.group(1).indexOf("fork=\"1\"") >= 0);
				if (!fork && threshold < 0) {
					// comment filter
					// Log.d("CommentAPI", matcher.group(1));
					matcher2 = ptnScore.matcher(matcher.group(1));
					if (matcher2.find() && Integer.parseInt(matcher2.group(1)) < threshold) {
						// Log.d("CommentAPI","filter: " + HtmlUtil.unescape(matcher.group(3)) + " " +  Integer.parseInt(matcher2.group(1)));
						continue;
					}
				}
				String msg = HtmlUtil.unescape(matcher.group(2));
				if (fork && (msg.startsWith("＠") || msg.startsWith("@") || msg.startsWith("/"))) {
					Log.d("nicoplayer_api", "skip: " + matcher.group(0));
					continue;
				}
				
				Comment c = new Comment(msg, mail, vpos);
				c.userId = commentUserId;
				c.no = no;
				comments.add(c);
			}
			Log.d("nicoplayer_api", "comments: " + comments.size());
		} catch (OutOfMemoryError e) {
			return null;
		}
		thread.comments = comments;
		return thread;
	}

	public static boolean postComment(NicoSession session, ThreadInfo thread, Comment comment) throws WebApiException {
		if (session == null)
			throw new NotLoginException();
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		String postkey = getPostKey(client, thread.lastRes / 100, thread.threadId);
		Log.d("nicoplayer_api", "postkey: " + postkey);

		String postdata;
		String maildata = (thread.force184 ? "" : "184 ") + HtmlUtil.escape(comment.mail);
		long userId = session.userId;
		postdata = "<chat thread=\"" + thread.threadId + "\" vpos=\"" + comment.vpos + "\" mail=\"" + maildata + "\" ticket=\"" + thread.ticket
				+ "\" user_id=\"" + userId + "\" postkey=\"" + postkey + "\" premium=\"" + session.isPremium + "\">" + HtmlUtil.escape(comment.message)
				+ "</chat>";

		client.post(thread.messageServerUrl, postdata);

		CharSequence data = client.getCharSequence();
		Pattern pattern = Pattern.compile("<chat_result[^>]*? status=\"0\"");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			return true;
		}
		Log.d("nicoplayer_api", "request:" + postdata);
		Log.d("nicoplayer_api", "response:" + data);

		return false;
	}

	private static String getPostKey(HttpClient client, int bn, long threadId) throws WebApiException {
		String res = client.getContent("http://flapi.nicovideo.jp/api/getpostkey/?block_no=" + bn + "&yugi=&thread=" + threadId).toString();

		HashMap<String, String> resmap = HtmlUtil.flashVarsToMap(res);
		return resmap.get("postkey");
	}

}
