package net.binzume.android.nicovideo.webapi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.VideoInfo;
import net.binzume.android.nicovideo.util.HtmlUtil;
import net.binzume.android.nicovideo.util.HttpClient;
import android.util.Log;

public class VideoSearchAPI {

	public static final String SORT_VIEW = "v";
	public static final String SORT_MYLIST = "m";
	public static final String SORT_COMMENT = "r";
	public static final String SORT_NEW = "f";
	public static final String SORT_NEWCOMMENT = "n";
	public static final String SORT_LENGTH = "l";

	public static final int SEARCH_MODE_TAG = 0;
	public static final int SEARCH_MODE_KEYWORD = 1;

	public static List<VideoInfo> searchByTag(String tag, int page, String opt) throws WebApiException {

		Log.d("nicoplayer_api", "tag search ");
		HttpClient client = new HttpClient();
		String params = (page < 2) ? "" : ("&page=" + page);
		if (opt == null) {
			opt = "";
		}

		CharSequence data = client.getContent(Constants.TOP_URL + "tag/" + HtmlUtil.urlEncode(tag) + "?rss=2.0"
				+ params + opt);

		return VideoRssParser.parseList(data);
	}

	public static List<VideoInfo> searchByKeyword(NicoSession session, String keyword, int page, String opt)
			throws WebApiException {
		return search(session, SEARCH_MODE_KEYWORD, keyword, page, opt);
	}

	private static List<VideoInfo> search(NicoSession session, int mode, String keyword, int page, String opt)
			throws WebApiException {
		HttpClient client = new HttpClient();
		if (session != null) {
			client.setCookie(session.cookie);
		}
		if (page < 1) {
			page = 1;
		}
		if (opt == null) {
			opt = "&sort=n&order=d";
		}

		String modeStr = (mode == SEARCH_MODE_TAG) ? "tag/" : "search/";

		CharSequence data = client.getContent(Constants.TOP_URL + modeStr + HtmlUtil.urlEncode(keyword) + "?page="
				+ page + opt);

		ArrayList<VideoInfo> list = new ArrayList<VideoInfo>();
		long stime = System.currentTimeMillis();

		Pattern ptnVideo = Pattern.compile("<li class=\"item[^>]+>(.*?)</div>\\s*</li>", Pattern.DOTALL);
		Pattern ptnThumb = Pattern.compile("<img[^>]+data-original=\"(http:[^\"]+\\d)\"[^>]*>"); // thumb
		Pattern ptnLength = Pattern.compile("<span class=\"videoLength\">([^<]+)</span>"); // time
		Pattern ptnCount = Pattern
				.compile(
						"<li class=\"count view\">[^<]*<span[^>]*>([\\d,]+)</span></li>\\s*<li class=\"count comment\">[^<]*<span[^>]*>([\\d,]+)</span></li>\\s*<li class=\"count mylist\">[^<]*<span[^>]*><[^>]+>([\\d,]+)</",
						Pattern.DOTALL);
		Pattern ptnDate = Pattern.compile("<span class=\"time\">\\s*([^<]+?)\\s*</span");
		Pattern ptnDesc = Pattern.compile("<[\\w\\s]+class=\"vinfo_description\"[^>]+>([^<]+)", Pattern.DOTALL);
		Pattern ptnLink = Pattern.compile("<a\\s+title=\"([^\"]+)\"\\s*href=\"([^?\"]+)[^\"]*\"[^>]*>");

		Matcher matcher = ptnVideo.matcher(data);
		while (matcher.find()) {
			Matcher matcher2 = ptnLink.matcher(matcher.group(1));
			if (!matcher2.find())
				continue;

			String a[] = matcher2.group(2).split("/");
			final String vid = a[a.length - 1];

			VideoInfo v = new VideoInfo(vid, HtmlUtil.unescape(matcher2.group(1)));

			matcher2 = ptnThumb.matcher(matcher.group(1));
			if (!matcher2.find())
				continue;

			v.thumbnailUrl = HtmlUtil.unescape(matcher2.group(1));

			matcher2 = ptnDate.matcher(matcher.group(1));
			if (matcher2.find()) {
				v.firstRetrive = matcher2.group(1).replace("：", ":");
			}

			matcher2 = ptnLength.matcher(matcher.group(1));
			if (matcher2.find()) {
				v.lengthStr = matcher2.group(1);
			}

			matcher2 = ptnDesc.matcher(matcher.group(1));
			if (matcher2.find()) {
				v.description = matcher2.group(1);
			}

			matcher2 = ptnCount.matcher(matcher.group(1));
			if (matcher2.find()) {
				v.viewCount = Integer.parseInt(matcher2.group(1).replaceAll(",", ""));
				v.commentCount = Integer.parseInt(matcher2.group(2).replaceAll(",", ""));
				v.mylistCount = Integer.parseInt(matcher2.group(3).replaceAll(",", ""));
			}

			list.add(v);
		}
		Log.d("niconicoplayer", "parse time:" + (System.currentTimeMillis() - stime));

		// matcher = Pattern.compile("<p class=\"form_result\">").matcher(data);

		return list;
	}

}
