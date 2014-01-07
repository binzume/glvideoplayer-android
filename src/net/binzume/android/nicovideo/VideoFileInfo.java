package net.binzume.android.nicovideo;

import java.util.HashMap;

public class VideoFileInfo {
	public static final int MEDIA_UNKNOWN = -1;
	public static final int MEDIA_FLV = 0;
	public static final int MEDIA_MP4 = 1;
	public static final int MEDIA_SWF = 2;

	public static final int MEDIA_QUALITY_UNKNOWN = 0;
	public static final int MEDIA_QUALITY_LOW = 1;
	public static final int MEDIA_QUALITY_MID = 2;
	public static final int MEDIA_QUALITY_ORG = 4;

	public String videoId;
	public String url;
	public int length;
	public String cookie;
	public String errorCode;
	public ThreadInfo thread;
	public long optionThreadId;

	public VideoFileInfo() {

	}

	// from flvInfo
	public VideoFileInfo(HashMap<String, String> info) {
		try {
			length = Integer.parseInt(info.get("l"));
		} catch (NumberFormatException e) {
		}
		url = info.get("url");
		if (info.get("optional_thread_id") != null) {
			optionThreadId = Integer.parseInt(info.get("optional_thread_id"));
		}

		try {
			long threadId = Long.parseLong(info.get("thread_id"));
			thread = new ThreadInfo(threadId);
			thread.needsKey = "1".equals(info.get("needs_key"));
			thread.messageServerUrl = info.get("ms");
		} catch (NumberFormatException e) {
		}
		errorCode = info.get("error");
	}

	public int getMediaType() {
		if (url == null)
			return MEDIA_UNKNOWN;
		if (url.contains("?v="))
			return MEDIA_FLV;
		if (url.contains("?m="))
			return MEDIA_MP4;
		if (url.contains("?s="))
			return MEDIA_SWF;
		return MEDIA_UNKNOWN;
	}

	public String getThumbnailUrl() {
		if (url == null) {
			return null;
		}

		return url.replaceFirst(".*\\?.=(\\d+).*", "http://tn-skr.smilevideo.jp/smile?i=$1");
	}

	public int getMediaQuality() {
		if (url == null)
			return MEDIA_QUALITY_UNKNOWN;
		if (url.endsWith("low")) {
			return MEDIA_QUALITY_LOW;
		}
		if (url.endsWith("mid")) {
			return MEDIA_QUALITY_MID;
		}
		return MEDIA_QUALITY_ORG;
	}
}
