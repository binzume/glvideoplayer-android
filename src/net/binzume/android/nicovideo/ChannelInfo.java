package net.binzume.android.nicovideo;

public class ChannelInfo {
	public String id; // example: ch639
	public String name;
	public String updateStr;

	public ChannelInfo() {
	}

	public ChannelInfo(String str) {
		String s[] = str.split("\t", 3);
		if (!"0".equals(s[0]) || s.length != 3)
			return;

		id = s[1];
		name = s[2];
	}

	public String getIconUrl() {
		return "http://icon.nimg.jp/channel/" + id + ".jpg";
	}

	public String getVideoListUrl() {
		return "http://ch.nicovideo.jp/video/" + id;
	}

	public String getTopUrl() {
		return "http://ch.nicovideo.jp/channel/" + id;
	}

	public String toString() {
		return "0\t" + id + "\t" + name;
	}

}
