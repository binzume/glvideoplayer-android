package net.binzume.android.nicovideo.webapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.*;

import net.binzume.android.nicovideo.ChannelCategoryInfo;
import net.binzume.android.nicovideo.ChannelInfo;
import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.VideoInfo;
import net.binzume.android.nicovideo.util.HtmlUtil;
import net.binzume.android.nicovideo.util.HttpClient;
import android.util.Log;

public class ChannelAPI {
	private static final String LIST_URL = "http://ch.nicovideo.jp/chlist";
	private static final String VIDEO_URL = "http://ch.nicovideo.jp/video/";
	private static final String CH_API_URL = "http://ch.nicovideo.jp/api/";
	private static final String CH_SP_API = "http://sp.ch.nicovideo.jp/api/";

	public static List<ChannelCategoryInfo> getAllChannelList() {
		HttpClient client = new HttpClient();

		if (!client.get(LIST_URL)) {
			return null;
		}

		CharSequence data = client.getCharSequence();

		ArrayList<ChannelCategoryInfo> list = new ArrayList<ChannelCategoryInfo>();

		long stime = System.currentTimeMillis();

		Log.d("nicoplayer_api", "category");
		Pattern pattern_channel_list = Pattern.compile("<dt class=\"cfix\">.*?cc_menu_id=(\\d+)\">([^<]+)</a>.*?<dd[^>]*>(.*?)</dd>", Pattern.DOTALL);
		Pattern pattern_channel = Pattern.compile("<a href=\"/(ch\\d+)\"\\s*title=\"([^\"]+)\"[^>]*>(.*?)</a>", Pattern.DOTALL);
		Pattern ptnUpdate = Pattern.compile("<time><var>([^<]+)</var>", Pattern.DOTALL);
		Matcher matcher = pattern_channel_list.matcher(data);
		HashSet<String> chset = new HashSet<String>();
		while (matcher.find()) {
			ChannelCategoryInfo category = null;
			for (ChannelCategoryInfo c : list) {
				if (c.name.equals(matcher.group(2))) {
					category = c;
				}
			}
			if (category == null) {
				category = new ChannelCategoryInfo();
				category.id = matcher.group(1);
				category.name = matcher.group(2);
				category.description = matcher.group(2);
				category.channels = new ArrayList<ChannelInfo>();
				list.add(category);
				//Log.d("nicoplayer_api", "notfound" + matcher.group(1));
				//continue;
			}
			//Log.d("nicoplayer_api", "found" + matcher.group(1));

			Matcher matcher2 = pattern_channel.matcher(matcher.group(3));
			while (matcher2.find()) {
				ChannelInfo channel = new ChannelInfo();
				channel.id = matcher2.group(1);
				channel.name = HtmlUtil.unescape(matcher2.group(2));

				if (chset.contains(channel.id)) {
					continue;
				}
				chset.add(channel.id);

				Matcher matcher3 = ptnUpdate.matcher(matcher2.group(3));
				if (matcher3.find()) {
					channel.updateStr = matcher3.group(1);
				}

				category.channels.add(channel);
				//Log.d("nicoplayer_api", "channel " + channel.id);
			}
		}

		Log.d("nicoplayer_api", "parse time:" + (System.currentTimeMillis() - stime));
		return list;
	}

	public static List<ChannelInfo> getBookmarkList(NicoSession session, int page) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		if (page < 1) {
			page = 1;
		}

		CharSequence data = client.getContent(CH_SP_API + "bookmark_list/?sort=u&order=d&page=" + page);
		if (data.length() <= 0) {
			throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
		}

		ArrayList<ChannelInfo> list = new ArrayList<ChannelInfo>();

		Pattern ptnChannel = Pattern.compile("<a\\s+href=\"/(ch\\d+)\"[^>]*>(.+?)</a>", Pattern.DOTALL);
		Pattern ptnTitle = Pattern.compile("<img\\s+[^>]*data-original=\"([^\"]+)\"\\s+alt=\"([^\"]+)\"", Pattern.DOTALL);
		Pattern ptnUpdate = Pattern.compile("<dl class=\"status date\"><dt>[^<]*</dt><dd>([^<]+)</dd>", Pattern.DOTALL);
		Matcher matcher = ptnChannel.matcher(data);
		while (matcher.find()) {

			ChannelInfo channel = new ChannelInfo();
			channel.id = matcher.group(1);

			Matcher matcher2 = ptnTitle.matcher(matcher.group(2));
			if (matcher2.find()) {
				channel.name = HtmlUtil.unescape(matcher2.group(2));

				Matcher matcher3 = ptnUpdate.matcher(matcher.group(2));
				if (matcher3.find()) {
					channel.updateStr = matcher3.group(1);
				}

				list.add(channel);
			}

		}

		return list;
	}

	public static List<VideoInfo> getVideoList(String chId, int page) throws WebApiException {
		HttpClient client = new HttpClient();
		if (page < 1) {
			page = 1;
		}

		CharSequence data = client.getContent(VIDEO_URL + chId + "/video?rss=2.0&page=" + page);

		List<VideoInfo> list = VideoRssParser.parseList(data);

		if (list.size() >= 20) { // pagesize=20 ?? FIXME
			list.add(null);
		}

		return list;
	}

	public static boolean addBookmark(NicoSession session, String chId) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		// パラメータ取得
		CharSequence data = client.getContent(VIDEO_URL + chId);
		Pattern pattern_bookmark_param = Pattern.compile("<a [^>]*?api_add[^>]*?params=\"([^\"]*)\"", Pattern.DOTALL);
		Matcher matcher = pattern_bookmark_param.matcher(data);
		if (!matcher.find()) {
			return false;
		}

		Log.d("nicoplayer_api", " ch bookmark params:" + matcher.group(1));
		client.getContent(CH_API_URL + "addbookmark?" + matcher.group(1));

		return true;
	}

	public static boolean deleteBookmark(NicoSession session, String chId) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		// パラメータ取得
		CharSequence data = client.getContent(VIDEO_URL + chId);
		Pattern pattern_bookmark_param = Pattern.compile("<a [^>]*?api_add[^>]*?params=\"([^\"]*)\"", Pattern.DOTALL);
		Matcher matcher = pattern_bookmark_param.matcher(data);
		if (!matcher.find()) {
			return false;
		}

		Log.d("nicoplayer_api", " ch bookmark params:" + matcher.group(1));
		client.getContent(CH_API_URL + "deletebookmark?" + matcher.group(1));

		return true;
	}

	public static int getPPV(NicoSession session, String vid) throws WebApiException {
		//

		if (session == null || session.cookie == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		HttpClient.Params params = new HttpClient.Params();
		params.put("ppv_id", vid);

		client.post("https://secure.ch.nicovideo.jp/api/ppv.price.info/", params);
		String data = client.getContent();
		//Log.d("nicoplayer_api", " ch bookmark params:" + data);

		// とりあえず入れておく
		Matcher matcher = Pattern.compile("<(\\w+)[^>]*>([^<]*)</\\1>", Pattern.DOTALL).matcher(data);
		HashMap<String, String> info = new HashMap<String, String>();
		while (matcher.find()) {
			// データが二重にエスケープされてる…
			info.put(matcher.group(1), HtmlUtil.unescape(matcher.group(2)));
		}

		if ("100".equals(info.get("code"))) {
			throw new NotLoginException();
		}

		if (info.get("code") != null) {
			return -Integer.parseInt(info.get("code"));
		}
		
		if (info.get("channel_id") != null) {
			return Integer.parseInt(info.get("channel_id"));
		}

		return -1;

	}
}
