package net.binzume.android.nicovideo.webapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.VideoFileInfo;
import net.binzume.android.nicovideo.VideoInfo;
import net.binzume.android.nicovideo.util.HtmlUtil;
import net.binzume.android.nicovideo.util.HttpClient;

import org.apache.http.cookie.Cookie;

import android.util.Log;

public class VideoAPI {

	public static VideoFileInfo getFlv(NicoSession session, String vid, int eco, boolean watch, boolean mp4Only) throws WebApiException {
		if (session == null || session.cookie == null)
			throw new NotLoginException();

		String ecoParam = eco > 0 ? "?eco=" + eco : "?";
		if (mp4Only) {
			ecoParam += "&device=ipad";
		} else {
			ecoParam += "&device=android";
		}

		HttpClient client = new HttpClient(Constants.USER_AGENT3);
		client.setCookie(session.cookie);

		if (!vid.startsWith("sm")) {
			// watchにアクセスした履歴が無いと動画にアクセスできない＆soXXX形式を数値に直す
			if (client.head(Constants.WATCH_URL + vid + ecoParam)) {
				if (client.getLocation() != null && client.getLocation().startsWith(Constants.SECURE_PROFILE_URL)) {
					// user profile not registered!
					client.head("http://sp.nicovideo.jp/watch/" + vid + ecoParam + "&watch_harmful=1");
				}
				String location = client.getLocation();
				// Log.d("NicoNicoPlayer", "location:" + location);

				if (location != null && location.indexOf("/watch/") != -1) {
					String l[] = location.substring(location.indexOf("/watch/") + 1).split("[/\\?]");
					if (l.length >= 2) {
						vid = l[1];
						// Log.d("NicoNicoPlayer", "location:" + location + " -> " + vid);
						client.head(Constants.WATCH_URL + vid + ecoParam);
					}
				}
			}
		}

		String data = client.getContent(Constants.FLAPI_URL + "getflv/" + vid + ecoParam).toString();
		if (data.length() <= 0) {
			return null;
		}
		//Log.d("NicoNicoPlayer", "vid=" + vid + "  getflv=" + data);

		HashMap<String, String> info = HtmlUtil.flashVarsToMap(data);

		if ("1".equals(info.get("closed"))) {
			throw new NotLoginException();
		}

		VideoFileInfo vi = new VideoFileInfo(info);
		vi.videoId = vid;


		if (!watch) {
			return vi;
		}

		if (vid.startsWith("sm") && vi.url != null) {
			// watchにアクセスした履歴が無いと動画にアクセスできない
			// Log.d("NicoNicoPlayer"," " + WATCH_URL + vid + ecoParam +
			// "&watch_harmful=1");
			client.head(Constants.WATCH_URL + vid + ecoParam + "&watch_harmful=1");
			Log.d("NicoNicoPlayer", "watch/" + vid);
			if (client.getLocation() != null && client.getLocation().startsWith(Constants.SECURE_PROFILE_URL)) {
				// user profile not registered!
				Log.d("vi", "user profile not registered!");
				client.head("http://sp.nicovideo.jp/watch/" + vid + ecoParam + "&watch_harmful=1");
			}
		}

		List<Cookie> cookies = client.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase("nicohistory")) {
				vi.cookie = "nicohistory=" + cookie.getValue();
			}
		}

		if (vi.cookie == null && vi.url != null) {
			if (client.getLocation() != null && client.getLocation().startsWith(Constants.SECURE_PROFILE_URL)) {
				Log.d("vi", "user profile not registered!!");
				throw new WebApiException(WebApiException.USER_PROFILE);
			}
		}

		// 有料動画チェック
		if (!vid.startsWith("sm") && vi.cookie == null) {
			VideoInfo vinfo = getVideoInfo(session, vid, true);
			int chid = ChannelAPI.getPPV(session, vinfo.videoId);
			if (chid > 0) {
				vi.errorCode = "ppv_video";
				vi.url = "http://sp.ch.nicovideo.jp/ch" + chid + "/video/" + vinfo.videoId; // ひどい
				return vi;
			}
		}

		Log.d("NicoNicoPlayer", "ok _vid " + vid + "  " + vi.cookie);
		return vi;
	}

	public static VideoInfo getVideoInfo(NicoSession session, String vid) {
		return getVideoInfo(session, vid, false);
	}

	public static VideoInfo getVideoInfo(NicoSession session, String vid, boolean overwriteVideoId) {
		HttpClient client = new HttpClient();
		if (session != null && session.cookie != null) {
			client.setCookie(session.cookie);
		}

		if (!client.get(Constants.API_URL + "getthumbinfo/" + vid)) {
			return null;
		}

		CharSequence data = client.getCharSequence();
		if (data.length() <= 0) {
			return null;
		}

		List<String> tags = new ArrayList<String>();
		// タグ
		Matcher matcher = Pattern.compile("<tags[^>]*domain=\"jp\">(.+?)</tags>", Pattern.DOTALL).matcher(data);
		if (matcher.find()) {
			// for (CharSequence s : HtmlUtil.get(matcher.group(1), "tag")) {
			// thumbInfo.tags.add(s);
			// }
			Pattern pattern1 = Pattern.compile("<tag[^>]*>([^<]+)</tag>");
			Matcher matcher1 = pattern1.matcher(matcher.group(1));
			while (matcher1.find()) {
				tags.add(HtmlUtil.unescape(HtmlUtil.unescape(matcher1.group(1))));
			}
		}

		// とりあえず入れておく
		matcher = Pattern.compile("<(\\w+)[^>]*>([^<]*)</\\1>", Pattern.DOTALL).matcher(data);
		HashMap<String, String> info = new HashMap<String, String>();
		while (matcher.find()) {
			// データが二重にエスケープされてる…
			info.put(matcher.group(1), HtmlUtil.unescape(HtmlUtil.unescape(matcher.group(2))));
		}
		if (overwriteVideoId && info.get("video_id") != null) {
			vid = info.get("video_id");
		}
		VideoInfo thumbInfo = new VideoInfo(vid, info.get("title"));
		thumbInfo.tags = tags;

		try {
			thumbInfo.viewCount = Integer.parseInt(info.get("view_counter"));
			thumbInfo.commentCount = Integer.parseInt(info.get("comment_num"));
			thumbInfo.mylistCount = Integer.parseInt(info.get("mylist_counter"));
		} catch (NumberFormatException e) {

		}
		thumbInfo.thumbnailUrl = info.get("thumbnail_url");
		thumbInfo.firstRetrive = info.get("first_retrieve");
		thumbInfo.lengthStr = info.get("length");
		thumbInfo.description = info.get("description");

		return thumbInfo;
	}

	/**
	 * ランキングを取得
	 * 
	 * @param type 種類
	 * @param term 期間
	 * @param category カテゴリ
	 * @return
	 */
	public static List<VideoInfo> getRanking(String type, String term, String category) {
		HttpClient client = new HttpClient();

		if (!client.get(Constants.TOP_URL + "ranking/" + type + "/" + term + "/" + category + "?rss=2.0")) {
			return null;
		}

		CharSequence data = client.getCharSequence();
		if (data.length() <= 0) {
			return null;
		}

		return VideoRssParser.parseList(data);
	}

}
