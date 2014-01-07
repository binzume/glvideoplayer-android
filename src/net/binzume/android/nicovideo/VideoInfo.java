package net.binzume.android.nicovideo;

import java.util.List;


public class VideoInfo {
	public final String videoId;
	public String title;
	public String description;
	public String thumbnailUrl;
	public List<String> tags;

	public String firstRetrive;
	public String lengthStr;

	public int viewCount = -1;
	public int mylistCount = -1;
	public int commentCount = -1;

	public VideoInfo(String videoId, String title) {
		this.videoId = videoId;
		this.title = title;
	}

	public static VideoInfo unserialize(String str) {
		String s[] = str.split("\\*", 10);
		if ("1".equals(s[0])) {
			s = str.split("\\*", 9);
			if (s.length != 9) {
				return null;
			}
		} else if ("2".equals(s[0])) {
			if (s.length != 10) {
				return null;
			}
		}

		VideoInfo videoInfo = new VideoInfo(s[1], s[s.length - 1]);

		videoInfo.thumbnailUrl = s[3];
		videoInfo.viewCount = Integer.parseInt(s[4]);
		videoInfo.mylistCount = Integer.parseInt(s[5]);
		videoInfo.commentCount = Integer.parseInt(s[6]);
		videoInfo.firstRetrive = s[7];
		if (s.length > 9) {
			videoInfo.lengthStr = s[8];
		}
		return videoInfo;
	}

	public String toString() {
		return "2*" + videoId + "**" + thumbnailUrl + "*" + viewCount + "*" + mylistCount + "*" + commentCount + "*"
				+ firstRetrive + "*" + lengthStr + "*" + title;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VideoInfo))
			return false;
		VideoInfo other = (VideoInfo) obj;
		if (videoId == null) {
			if (other.videoId != null)
				return false;
		} else if (!videoId.equals(other.videoId))
			return false;
		return true;
	}
}
