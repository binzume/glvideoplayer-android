package net.binzume.android.nicovideo.webapi;

import java.util.ArrayList;
import java.util.List;

import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.util.HttpClient;

public class WatchItemAPI {
	public static final int TYPE_USER = 1;

	public static boolean add(NicoSession session, long itemType, String itemId) throws WebApiException {
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		// *
		String token = MyListAPI.getToken(client);

		HttpClient.Params params = new HttpClient.Params();
		params.put("item_type", "" + itemType);
		params.put("item_id", itemId);
		params.put("token", token);

		if (client.post(Constants.API_URL + "watchitem/add", params, false) == null) {
			return false;
		}

		return true;

	}

	public static boolean delete(NicoSession session, String itemIds) throws WebApiException {
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);
		// *
		String token = MyListAPI.getToken(client);

		HttpClient.Params params = new HttpClient.Params();
		params.put("id_list", itemIds);
		params.put("token", token);

		if (client.post(Constants.API_URL + "watchitem/delete", params, false) == null) {
			return false;
		}

		return true;
	}

	public static List<String> list(NicoSession session, String itemIds) throws WebApiException {
		HttpClient client = new HttpClient();
		client.setCookie(session.cookie);

		CharSequence data = client.getContent(Constants.API_URL + "watchitem/list");
		if (data.length() <= 0) {
			return null;
		}

		ArrayList<String> list = new ArrayList<String>();

		return list;
	}
}
