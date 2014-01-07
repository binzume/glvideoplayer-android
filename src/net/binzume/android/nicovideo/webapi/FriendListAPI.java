package net.binzume.android.nicovideo.webapi;

import android.util.Log;
import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.util.HttpClient;

import org.json.*;

public class FriendListAPI {

	public static String getList(NicoSession session) throws WebApiException {
		if (session == null)
			throw new NotLoginException();
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		String data = client.getContent(Constants.API_URL + "friendlist/list").toString();

		try {
			JSONObject json = new JSONObject(data);
			if (!json.getString("status").equals("ok")) {
				Log.d("nicoplayer_api", "status:" + json.getString("status"));
				return null;
			}

			JSONArray jslist = json.getJSONArray("friend");
			for (int i = 0; i < jslist.length(); i++) {
				JSONObject jitem = jslist.getJSONObject(i);
				jitem.getInt("id");
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}

}
