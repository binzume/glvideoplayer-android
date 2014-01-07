package net.binzume.android.nicoplayer.player;

import net.binzume.android.nicovideo.Comment;
import android.graphics.Color;

public class CommentSlot<T> {
	final static public int PLACE_NAKA = 0;
	final static public int PLACE_SHITA = 2;
	final static public int PLACE_UE = 4;
	final static public int STYLE_MINE = 0x100;
	final static public int SIZE_DEFAULT = 20;
	final static public int SIZE_BIG = 24;
	final static public int SIZE_SMALL = 12;
	public final Comment c;
	public final int entryTime;
	public final int color;
	public final int fontsize;
	public int style;
	public int y;
	public int width;
	public float renderFontSize;

	public T displayObject;

	public CommentSlot(Comment comment) {
		c = comment;

		int color = Color.WHITE;
		style = CommentSlot.PLACE_NAKA;
		int fontsize = CommentSlot.SIZE_DEFAULT;

		String command[] = comment.mail.split("\\s");
		for (int j = 0; j < command.length; j++) {
			if (command[j].equals("red"))
				color = Color.RED;
			else if (command[j].equals("blue"))
				color = Color.BLUE;
			else if (command[j].equals("green"))
				color = Color.GREEN;
			else if (command[j].equals("purple"))
				color = Color.rgb(0x9b, 0x1d, 0x68);
			else if (command[j].equals("cyan"))
				color = Color.CYAN;
			else if (command[j].equals("yellow"))
				color = Color.YELLOW;
			else if (command[j].equals("white"))
				color = Color.WHITE;
			else if (command[j].equals("black"))
				color = Color.BLACK;
			else if (command[j].equals("pink"))
				color = Color.rgb(0xf1, 0x8d, 0x99);
			else if (command[j].equals("orange"))
				color = Color.rgb(0xe9, 0x79, 0x16);
			else if (command[j].equals("shita"))
				style = CommentSlot.PLACE_SHITA;
			else if (command[j].equals("ue"))
				style = CommentSlot.PLACE_UE;
			else if (command[j].equals("small"))
				fontsize = CommentSlot.SIZE_SMALL;
			else if (command[j].equals("big"))
				fontsize = CommentSlot.SIZE_BIG;
		}
		
		this.fontsize = fontsize;
		this.color = color;

		if ((style & (CommentSlot.PLACE_UE | CommentSlot.PLACE_SHITA)) != 0) {
			entryTime = comment.vpos * 10;
			if (comment.message.length() > 30)
				fontsize = CommentSlot.SIZE_SMALL;
		} else {
			entryTime = comment.vpos * 10 - 1000;
		}

	}

}
