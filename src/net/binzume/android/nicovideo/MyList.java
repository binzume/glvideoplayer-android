package net.binzume.android.nicovideo;

import java.util.List;

public class MyList {
	public final int id;
	public int userId;
	public String name;
	public String description;
	public int createDate;
	public int updateDate;
	public int isPublic;
	public List<MyListEntry> items;

	public MyList(int id) {
		this.id = id;
	}

	public static class MyListEntry extends VideoInfo {
		public MyListEntry(String videoId, String title) {
			super(videoId, title);
		}

		public int threadId;
		public int type;
		public int deleted;
		public String lastResBody;

		public long createDate;
		public long updateDate;
	}
}
