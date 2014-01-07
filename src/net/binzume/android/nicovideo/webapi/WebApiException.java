package net.binzume.android.nicovideo.webapi;

@SuppressWarnings("serial")
public class WebApiException extends Exception {
	public static final int UNKNOWN_ERROR = -1;
	public static final int NETWORK_ERROR = -2;
	public static final int INVALID_RESPONSE = -3;
	public static final int NOT_LOGIN = 0;
	public static final int USER_PROFILE = 1;
	public static final int BAD_REQUEST = 400;
	public static final int PAYMENT_REQUIRED = 402;
	public static final int NOT_FOUND = 404;
	public static final int ALREADY_EXISTS = 409;
	public static final int SERVICE_UNAVAILABLE = 503;

	private final int status;

	public WebApiException(int status) {
		this.status = status;
	}

	public WebApiException(int status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
}
