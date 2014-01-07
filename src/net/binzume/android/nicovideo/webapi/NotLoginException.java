package net.binzume.android.nicovideo.webapi;

@SuppressWarnings("serial")
public class NotLoginException extends WebApiException {

	public NotLoginException() {
		super(WebApiException.NOT_LOGIN);
	}
}
