package net.binzume.android.nicovideo;

import java.util.List;


public class ChannelCategoryInfo {
	public String id;
	public String name;
	public String description;

	public List<ChannelInfo> channels;

	public ChannelCategoryInfo() {
	}

	public ChannelCategoryInfo(String str) {
		String s[] = str.split("\t", 4);
		if (!"0".equals(s[0]) || s.length != 4)
			return;

		id = s[1];
		name = s[2];
		description = s[3];

	}

	public String toString() {
		return "0\t" + id + "\t" + name + "\t" + "description";
	}

}
