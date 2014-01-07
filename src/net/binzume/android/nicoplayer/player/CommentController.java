package net.binzume.android.nicoplayer.player;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.binzume.android.nicovideo.Comment;
import net.binzume.android.nicovideo.ThreadInfo;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentController {
	private final static int COMMENT_SHOW_TIME = 4000;

	final static private int STATE_LOADING = 1;
	final static private int STATE_OK = 2;
	final static private int STATE_ERROR = -1;

	private ThreadInfo threadInfo;
	public List<CommentSlot<TextView>> sortedSlots = new LinkedList<CommentSlot<TextView>>();
	private List<CommentSlot<TextView>> displaySlots = new LinkedList<CommentSlot<TextView>>();
	private List<TextView> comentview_stock = new LinkedList<TextView>();
	private ViewGroup commentLayer;
	private float sd;
	private int width;
	private int height;
	private int loadingState = 0;
	private int filterThreshold = 0;

	public CommentController(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		filterThreshold = Integer.parseInt(sp.getString("config_comment_filter", "-5000"));
	}

	@SuppressWarnings("deprecation")
	public void setView(Activity context, ViewGroup v) {
		// 解像度にあわせてフォントサイズを
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		sd = metrics.scaledDensity;
		clearDisplaySlot();
		comentview_stock.clear();

		commentLayer = v;
		for (int i = 0; i < 30; i++) {
			TextView tv = new TextView(context);
			comentview_stock.add(tv);
			commentLayer.addView(tv, RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			tv.setText("");
			tv.setHorizontallyScrolling(true);
			// tv.setSingleLine(true);
		}

	}

	public void setComments(List<Comment> comments) {
		sortedSlots = new LinkedList<CommentSlot<TextView>>();
		if (comments == null)
			return;
		for (Comment comment : comments) {
			sortedSlots.add(new CommentSlot<TextView>(comment));
		}
		sortSlots();
	}

	public boolean isError() {
		return loadingState < 0;
	}

	public boolean isLoading() {
		return loadingState == STATE_LOADING;
	}

	public boolean isLoaded() {
		return loadingState == STATE_OK;
	}

	/**
	 * @param ti thread
	 * @param videoLength video length in seconds.
	 */
	public void load(ThreadInfo ti, int videoLength) {
		threadInfo = null;
		loadingState = STATE_LOADING;
		int leaves = videoLength / 60;
		if (leaves <= 0) {
			leaves = 99;
		}
		new CommentLoadTask(this, videoLength / 60, filterThreshold).execute(ti);
	}

	public void setThreadInfo(ThreadInfo ti) {
		if (ti == null) {
			loadingState = STATE_ERROR;
			return;
		}
		loadingState = STATE_OK;
		threadInfo = ti;
		setComments(ti.comments);
		ti.comments = null; // コピーしたのでもう使わない
	}

	public ThreadInfo getThreadInfo() {
		return threadInfo;
	}

	public void addComment(Comment c) {
		CommentSlot<TextView> item = new CommentSlot<TextView>(c);
		sortedSlots.add(item);
		sortSlots();
		item.style |= 0x100; // 仮
		if (width > 0) {
			makeCommentLayout(width, height);
		}
	}

	private void sortSlots() {
		Collections.sort(sortedSlots, new Comparator<CommentSlot<?>>() {
			public int compare(CommentSlot<?> a, CommentSlot<?> b) {
				return a.entryTime - b.entryTime;
			}
		});
	}

	private void clearDisplaySlot() {
		for (CommentSlot<TextView> slot : displaySlots) {
			slot.displayObject.setText("");
			comentview_stock.add(slot.displayObject); // return
			slot.displayObject = null;
		}
		displaySlots.clear();
	}

	private int fastMesureText(String text, int fontSize) {
		int sz = 0;

		for (int i = 0; i < text.length(); i++) {
			sz += text.charAt(i) > 255 ? 2 : 1;
		}

		return sz * fontSize / 2;
	}

	public void makeCommentLayout(int width, int height) {
		clearDisplaySlot();
		this.width = width;
		if (height > width * 3 / 4) {
			height = width * 3 / 4; // 3:4より縦を伸ばさない（仮）
		}
		this.height = height;

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//textPaint.setShadowLayer(2, 0, 0, 0);
		int lines = 11;
		int font_height = height / lines;
		if (font_height < CommentSlot.SIZE_DEFAULT * sd) {
			font_height = (int) (CommentSlot.SIZE_DEFAULT * sd);
			lines = height / font_height;
		}
		float font_scale = (float) font_height / CommentSlot.SIZE_BIG / sd;
		// コメントを並べる
		int rowslot1[] = new int[lines]; // 行スロットが空く時間
		int rowslot2[] = new int[lines]; // 行スロットが空く時間
		int rowslot3[] = new int[lines]; // ue,shita用
		int count = 0;
		for (CommentSlot<?> ci : sortedSlots) {
			count++;
			ci.renderFontSize = ci.fontsize * font_scale;
			textPaint.setTextSize(ci.renderFontSize);
			// 1000件以上あるときは時間かかるので以降を速度重視にする
			ci.width = (int) ((count < 1000 ? textPaint.measureText(ci.c.message) : fastMesureText(ci.c.message, (int) ci.renderFontSize)) * sd);
			if ((ci.style & (CommentSlot.PLACE_SHITA | CommentSlot.PLACE_UE)) != 0) {
				if (ci.width > width) {
					ci.renderFontSize = ci.renderFontSize * width / ci.width;
					textPaint.setTextSize(ci.renderFontSize);
					ci.width = (int) ((count < 1000 ? textPaint.measureText(ci.c.message) : fastMesureText(ci.c.message, (int) ci.renderFontSize)) * sd);
				}
			}
			ci.y = (int) (Math.random() * (height - 24));
			if ((ci.style & CommentSlot.PLACE_SHITA) != 0) {
				for (int j = rowslot3.length - 1; j >= 0; j--) {
					if (rowslot3[j] <= ci.entryTime) {
						rowslot3[j] = ci.entryTime + COMMENT_SHOW_TIME;
						ci.y = j * font_height;
						break;
					}
				}
			} else if ((ci.style & CommentSlot.PLACE_UE) != 0) {
				for (int j = 0; j < rowslot3.length; j++) {
					if (rowslot3[j] <= ci.entryTime) {
						rowslot3[j] = ci.entryTime + COMMENT_SHOW_TIME;
						ci.y = j * font_height;
						break;
					}
				}
			} else {
				for (int j = 0; j < rowslot1.length; j++) {
					// w = 画面からはみ出てから消えるまでの時間
					int w = (ci.width * COMMENT_SHOW_TIME / (ci.width + width));
					// int w =
					// (ci.ci.message.length()*20*COMMENT_SHOW_TIME/width);
					if (rowslot1[j] <= ci.entryTime && rowslot2[j] <= ci.entryTime + COMMENT_SHOW_TIME - w) {
						rowslot1[j] = ci.entryTime + w;
						rowslot2[j] = ci.entryTime + COMMENT_SHOW_TIME;
						ci.y = j * font_height;
						break;
					}
				}
			}
			// Log.d("nicoplayer","slot " +ci.y+" "+ ci.entryTime);
		}
	}

	public void updatePosition(int t) {

		// 外に出たコメントを消す
		for (Iterator<CommentSlot<TextView>> i = displaySlots.iterator(); i.hasNext();) {
			CommentSlot<TextView> slot = i.next();
			if (slot.entryTime > t || t >= slot.entryTime + COMMENT_SHOW_TIME) {
				slot.displayObject.setText("");
				comentview_stock.add(slot.displayObject); // return
				slot.displayObject = null;
				i.remove();
			}
		}

		// パフォーマンスに問題あるならArrayListにして二分探索する
		for (CommentSlot<TextView> slot : sortedSlots) {
			if (slot.entryTime > t)
				break;
			if (t < slot.entryTime + COMMENT_SHOW_TIME) {
				if (slot.displayObject == null) {
					if (comentview_stock.size() == 0) {
						continue;
					}
					displaySlots.add(slot);
					TextView tv = comentview_stock.get(0); // get
					tv.setTextSize(slot.renderFontSize);
					comentview_stock.remove(0);
					slot.displayObject = tv;
					tv.setText(slot.c.message);
					tv.setTextColor(slot.color);
					if (slot.width > width * 3 / 2) {
						tv.setShadowLayer(0, 0, 0, 0); // hardware accel bug?
					} else if ((slot.style & CommentSlot.STYLE_MINE) != 0) {
						tv.setShadowLayer(2, 0, 0, Color.RED);
					} else if (slot.color == Color.BLACK) {
						tv.setShadowLayer(1, 0, 0, Color.WHITE);
					} else {
						tv.setShadowLayer(1, 0, 0, Color.BLACK);
					}
				}
				int x;
				if ((slot.style & (CommentSlot.PLACE_SHITA | CommentSlot.PLACE_UE)) == 0) {
					x = width - (t - slot.entryTime) * (width + slot.width) / (COMMENT_SHOW_TIME);
				} else {
					x = (width - slot.width) / 2;
				}
				slot.displayObject.setPadding(x, slot.y, 0, 0);
			}
		}

	}

}
