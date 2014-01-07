package net.binzume.android.nicovideo.webapi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.binzume.android.nicovideo.VideoInfo;
import net.binzume.android.nicovideo.util.HtmlUtil;

public class VideoRssParser {

	public static List<VideoInfo> parseList(CharSequence data) {

		// id thumb title
		Pattern ptnItem = Pattern.compile("<item[^>]*>(.*?)</item>", Pattern.DOTALL);
		Pattern ptnLink = Pattern.compile("<link[^>]*>(.*?)</link>");
		Pattern ptnThumb = Pattern.compile("<p\\s+class=\"nico-thumbnail\"><img alt=\"([^\"]*)\" src=\"([^\"]+)\"");
		Pattern ptnLength = Pattern.compile("<[\\w\\s]+class=\"nico-info-length\">([^<]+)");
		Pattern ptnDate = Pattern.compile("<[\\w\\s]+class=\"nico-info-date\">([^<]+)");
		Pattern ptnDesc = Pattern.compile("<[\\w\\s]+class=\"nico-description\">([^<]+)", Pattern.DOTALL);
		Pattern ptnCount = Pattern
				.compile("<strong class=\"nico-info-total-view\">([^>]+)</strong>.*?<strong class=\"nico-info-total-res\">([^>]+)</strong>.*?<strong class=\"nico-info-total-mylist\">([^>]+)</strong>");

		ArrayList<VideoInfo> list = new ArrayList<VideoInfo>();

		Matcher matcher = ptnItem.matcher(data);
		while (matcher.find()) {

			Matcher matcher2 = ptnLink.matcher(matcher.group(1));
			if (!matcher2.find())
				continue;

			String a[] = matcher2.group(1).split("/");
			final String vid = a[a.length - 1];

			matcher2 = ptnThumb.matcher(matcher.group(1));
			if (!matcher2.find())
				continue;

			VideoInfo v = new VideoInfo(vid, HtmlUtil.unescape(HtmlUtil.unescape(matcher2.group(1)))); // 二重にエスケープされてる
			v.thumbnailUrl = HtmlUtil.unescape(matcher2.group(2));

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

		return list;

	}

}
