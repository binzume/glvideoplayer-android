package net.binzume.android.nicovideo;

import org.apache.http.cookie.Cookie;

public class NicoSession {
	public static final int ERROR_NONE = 0;
	public static final int ERROR_LOGIN_FAILED = 1;
	public static final int ERROR_ACCOUNT_LOCK = 4;

	public Cookie cookie;
	public String user_session; // SSID
	public long userId = -1;
	public int isPremium = -1;
	public int errorCode;
	public long lastLogin = -1;
}
