/**
 * ニコニコ マイリスト API
 * 
 * @author binzume <kawahira__binzume.net>
 */
package net.binzume.android.nicovideo.webapi;

import net.binzume.android.nicovideo.MyList;
import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.VideoInfo;
import net.binzume.android.nicovideo.util.HtmlUtil;
import net.binzume.android.nicovideo.util.HttpClient;

import org.json.*;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyListAPI {
	private static final String TOP_URL = Constants.TOP_URL;
	private static final String API_URL = Constants.API_URL;
	private static final String MYLIST_URL = Constants.MYLIST_URL;
	public static final int DEFAULT_LIST = -1;

	/**
	 * マイリストの一覧を取得する 何も無けれ ば空のList マイリストの内容自体は取得しない
	 * 
	 * @param session セッション
	 * @return
	 * @throws NotLoginException
	 */
	public static List<MyList> getLists(NicoSession session) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		String data = client.getContent(API_URL + "mylistgroup/list").toString();

		try {
			JSONObject json = new JSONObject(data);
			if (!json.getString("status").equals("ok")) {
				Log.d("nicoplayer_api", "status:" + json.getString("status"));
				if ("NOAUTH".equals(json.getJSONObject("error").getString("code"))) {
					throw new NotLoginException();
				}
				throw new WebApiException(WebApiException.UNKNOWN_ERROR);
			}

			JSONArray jslist = json.getJSONArray("mylistgroup");
			ArrayList<MyList> list = new ArrayList<MyList>();
			for (int i = 0; i < jslist.length(); i++) {
				JSONObject jitem = jslist.getJSONObject(i);
				MyList item = new MyList(jitem.getInt("id"));
				item.userId = jitem.getInt("user_id");
				item.name = jitem.getString("name");
				item.isPublic = jitem.getInt("public");
				list.add(item);
			}

			return list;
		} catch (JSONException e) {
			throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
		}

	}

	public static void addDefault(NicoSession session, String vid) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		// *
		String token = getToken(client);

		HttpClient.Params params = new HttpClient.Params();
		params.put("item_id", vid);
		params.put("token", token);

		client.post(API_URL + "deflist/add", params, true);

		String data = client.getContent();
		try {
			JSONObject json = new JSONObject(data);
			if (!json.getString("status").equals("ok")) { // status:fail
				// Log.d("nicoplayer_api", "data:" + data);
				// Log.d("nicoplayer_api", "status:" + json.getString("status"));
				// Log.d("nicoplayer_api", "errorCode:" + json.getJSONObject("error").getString("code"));
				if (json.has("error")) {
					String errorCode = json.getJSONObject("error").getString("code");
					if ("EXIST".equals(errorCode)) {
						throw new WebApiException(WebApiException.ALREADY_EXISTS, json.getJSONObject("error").getString("description"));
					} else {
						throw new WebApiException(WebApiException.UNKNOWN_ERROR, json.getJSONObject("error").getString("description"));
					}
				}
				throw new WebApiException(WebApiException.UNKNOWN_ERROR);
			}
		} catch (JSONException e) {
			throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
		}
	}

	public static MyList get(NicoSession session, int id) throws WebApiException {
		MyList mylist = new MyList(id);
		mylist.userId = 0;
		mylist.isPublic = 0;
		return get(session, mylist);
	}

	public static MyList get(NicoSession session, MyList mylist) throws WebApiException {
		if (session == null)
			throw new NotLoginException();
		// if (mylist==null || mylist.id<0) return getDefault(session);
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		String data;

		if (mylist == null || mylist.id < 0) {
			if (mylist == null)
				mylist = new MyList(-1);
			mylist.name = "とりあえずマイリスト";
			mylist.userId = 0;
			mylist.isPublic = 0;

			data = client.getContent(API_URL + "deflist/list").toString();
		} else {
			data = client.getContent(API_URL + "mylist/list?group_id=" + mylist.id).toString();
		}
		if (client.getLocation() != null && client.getLocation().startsWith(Constants.SECURE_PROFILE_URL)) {
			throw new WebApiException(WebApiException.USER_PROFILE);
		}

		JSONArray jslist;
		try {
			JSONObject json = new JSONObject(data);
			if (!json.getString("status").equals("ok")) {
				Log.d("nicoplayer_api", "status:" + json.getString("status"));
				if ("NOAUTH".equals(json.getJSONObject("error").getString("code"))) {
					throw new NotLoginException();
				}
				throw new WebApiException(WebApiException.UNKNOWN_ERROR);
			}

			mylist.items = new ArrayList<MyList.MyListEntry>();
			jslist = json.getJSONArray("mylistitem");
		} catch (JSONException e) {
			throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
		}

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		for (int i = 0; i < jslist.length(); i++) {
			try {
				JSONObject jitem = jslist.getJSONObject(i);
				JSONObject itemdata = jitem.getJSONObject("item_data");
				MyList.MyListEntry item = new MyList.MyListEntry(itemdata.getString("video_id"), HtmlUtil.unescape(itemdata.getString("title")));
				item.type = jitem.getInt("item_type");
				item.threadId = jitem.getInt("item_id");
				item.description = jitem.getString("description");
				item.createDate = jitem.getLong("create_time") * 1000;
				item.updateDate = itemdata.getLong("update_time");
				item.thumbnailUrl = itemdata.getString("thumbnail_url");
				item.lastResBody = itemdata.getString("last_res_body");
				item.viewCount = itemdata.getInt("view_counter");
				item.mylistCount = itemdata.getInt("mylist_counter");
				item.commentCount = itemdata.getInt("num_res");
				item.deleted = itemdata.getInt("deleted");
				item.firstRetrive = fmt.format(new Date(itemdata.getLong("first_retrieve") * 1000));
				mylist.items.add(item);
				int len = itemdata.getInt("length_seconds");
				item.lengthStr = String.format("%d:%02d", len / 60, len % 60);
			} catch (Exception e) {
				Log.d("nicoplayer_api", "mylistItem json error");
			}
		}

		return mylist;
	}

	/**
	 * マイリストのアイテムを移動
	 * 
	 * @param session
	 * @param from
	 * @param threadId
	 * @param to
	 * @throws WebApiException
	 */
	public static void move(NicoSession session, MyList from, long threadId, MyList to) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		String token = getToken(client);

		if (from == null || from.id == -1) {
			HttpClient.Params params = new HttpClient.Params();
			params.put("target_group_id", "" + to.id);
			params.put("id_list[0][]", "" + threadId);
			params.put("token", token);
			client.post(API_URL + "deflist/move", params);
		} else {
			HttpClient.Params params = new HttpClient.Params();
			params.put("group_id", "" + from.id);
			params.put("target_group_id", "" + to.id);
			params.put("id_list[0][]", "" + threadId);
			params.put("token", token);
			client.post(API_URL + "mylist/move", params);
		}
		String data = client.getContent();
		JSONObject json;
		try {
			json = new JSONObject(data);
			if (!json.getString("status").equals("ok")) {
				Log.d("nicoplayer_api", "status:" + json.getString("status"));
				if (json.has("error")) {
					String errorCode = json.getJSONObject("error").getString("code");
					if ("EXIST".equals(errorCode)) {
						throw new WebApiException(WebApiException.ALREADY_EXISTS, json.getJSONObject("error").getString("description"));
					} else {
						throw new WebApiException(WebApiException.UNKNOWN_ERROR, json.getJSONObject("error").getString("description"));
					}
				}
				throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
			} else if (json.has("duplicates")) {
				// .....
				if (json.get("duplicates") instanceof JSONObject) {
					throw new WebApiException(WebApiException.ALREADY_EXISTS);
				}
			}
		} catch (JSONException e) {
			throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
		}

	}

	/**
	 * マイリスト新規作成
	 * 
	 * @param session
	 * @param mylist
	 * @throws WebApiException
	 */
	public static void create(NicoSession session, MyList mylist) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		String token = getToken(client);

		HttpClient.Params params = new HttpClient.Params();
		params.put("name", "" + mylist.name);
		params.put("description", mylist.description);
		params.put("public", "" + mylist.isPublic);
		params.put("default_sort", "1");
		params.put("icon_id", "1");
		params.put("token", token);

		client.post(API_URL + "mylistgroup/add", params);

	}

	public static boolean update(NicoSession session, MyList mylist) throws WebApiException {
		// not implemented yet...
		return false;
	}

	/**
	 * マイリスト削除
	 * 
	 * @param session
	 * @param mylist
	 * @throws WebApiException
	 */
	public static void removeList(NicoSession session, MyList mylist) throws WebApiException {
		if (session == null)
			throw new NotLoginException();

		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		String token = getToken(client);

		HttpClient.Params params = new HttpClient.Params();
		params.put("group_id", "" + mylist.id);
		params.put("token", token);

		client.post(API_URL + "mylistgroup/delete", params);
	}

	/**
	 * マイリストのアイテムを削除
	 * 
	 * @param session
	 * @param mylist
	 * @param threadId
	 * @throws WebApiException
	 */
	public static void remove(NicoSession session, MyList mylist, long threadId) throws WebApiException {
		if (session == null)
			throw new NotLoginException();
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		String token = getToken(client);

		if (mylist == null || mylist.id == -1) {
			HttpClient.Params params = new HttpClient.Params();
			params.put("id_list[0][]", "" + threadId);
			params.put("token", token);
			client.post(API_URL + "deflist/delete", params);
		} else {
			HttpClient.Params params = new HttpClient.Params();
			params.put("group_id", "" + mylist.id);
			params.put("id_list[0][]", "" + threadId);
			params.put("token", token);
			client.post(API_URL + "mylist/delete", params);
		}
		String data = client.getContent();

		JSONObject json;
		try {
			json = new JSONObject(data);
			if (!json.getString("status").equals("ok")) {
				Log.d("nicoplayer_api", "status:" + json.getString("status"));
				throw new WebApiException(WebApiException.UNKNOWN_ERROR, "status error");
			}
		} catch (JSONException e) {
			throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
		}

	}

	// マイリスト以外から使うためのpublicにした
	public static String getToken(HttpClient client) throws WebApiException {

		CharSequence data = client.getContent(MYLIST_URL);
		Matcher matcher = Pattern.compile("NicoAPI.token\\s*=\\s*\"([0-9a-f-]+)\";").matcher(data);
		if (matcher.find()) {
			// Log.d("nicoplayer_api", "getToken() : " + matcher.group(1));
			return matcher.group(1);
		}

		if (client.getLocation() != null) {
			if (client.getLocation().startsWith(Constants.SECURE_PROFILE_URL)) {
				// ユーザー情報未設定
				throw new WebApiException(WebApiException.USER_PROFILE);
			}
			if (client.getLocation().startsWith("https://secure.nicovideo.jp/secure/login_form")) {
				throw new NotLoginException();
			}
		}

		throw new WebApiException(WebApiException.INVALID_RESPONSE, "invalid response");
	}

	/**
	 * 他のユーザーのマイリストから動画を取得
	 * 
	 * @param id マイリストID
	 * @return
	 */
	public static List<VideoInfo> getVideoList(String id) throws WebApiException {
		HttpClient client = new HttpClient();

		CharSequence data = client.getContent(TOP_URL + "mylist/" + id + "?rss=2.0");

		return VideoRssParser.parseList(data);
	}

}
