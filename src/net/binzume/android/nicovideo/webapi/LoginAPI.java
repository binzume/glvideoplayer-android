/**
 * ニコニコアカウント API
 * 
 * @author binzume <kawahira__binzume.net>
 */
package net.binzume.android.nicovideo.webapi;

import net.binzume.android.nicovideo.NicoSession;
import net.binzume.android.nicovideo.util.HttpClient;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.cookie.Cookie;

import android.util.Log;

public class LoginAPI {

	/**
	 * ログインする
	 * 
	 * @param mail メールアドレス
	 * @param passwd パスワード
	 * @return セッション(ログインできていない場合も値を返すのでerrorCodeチェック必須)
	 * @throws WebApiException
	 */
	public static NicoSession login(String mail, String passwd) throws WebApiException {

		if (mail == null || passwd == null || "".equals(passwd)) {
			throw new WebApiException(WebApiException.BAD_REQUEST);
		}

		HttpClient client = new HttpClient(Constants.USER_AGENT3);
		HttpClient.Params params = new HttpClient.Params();
		params.put("mail", mail);
		params.put("password", passwd);
		HttpResponse res = client.post(Constants.SECURE_URL, params, false);
		if (res == null) {
			Log.d("niconicoplayer_api", "login network error");
			if (client.getHttpStatus() == 503) {
				throw new WebApiException(WebApiException.SERVICE_UNAVAILABLE);
			} else {
				throw new WebApiException(WebApiException.NETWORK_ERROR);
			}
		}

		NicoSession session = new NicoSession();
		Log.d("niconicoplayer_api", "login redirect " + client.getLocation());
		if (client.getLocation() == null) {
			session.errorCode = NicoSession.ERROR_ACCOUNT_LOCK;
			return session;
		}

		// user_session
		for (Cookie cookie : client.getCookies()) {
			if (cookie.getName().equalsIgnoreCase("user_session")) {
				if (!cookie.getValue().equals("deleted")) {
					session.user_session = cookie.getValue();
					session.cookie = cookie;
				}
			}
		}
		if (session.user_session == null) {
			session.errorCode = NicoSession.ERROR_LOGIN_FAILED;
			return session;
		}

		Header idHeader = res.getFirstHeader("x-niconico-id");
		if (idHeader != null) {
			session.userId = Long.parseLong(idHeader.getValue());
		}

		Header authHeader = res.getFirstHeader("x-niconico-authflag");
		if (authHeader != null) {
			session.isPremium = Integer.parseInt(authHeader.getValue()) > 1 ? 1 : 0;
		}

		session.lastLogin = System.currentTimeMillis();

		// Log.d("niconicoplayer_api", "userId " + session.userId);
		// Log.d("niconicoplayer_api", "userType " + session.isPremium);

		session.errorCode = NicoSession.ERROR_NONE;
		return session;
	}

}
