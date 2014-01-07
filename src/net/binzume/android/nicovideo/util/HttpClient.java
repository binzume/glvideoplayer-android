//simple http client

package net.binzume.android.nicovideo.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.binzume.android.nicovideo.webapi.Constants;
import net.binzume.android.nicovideo.webapi.WebApiException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class HttpClient {
	protected DefaultHttpClient client;
	protected List<Cookie> cookies;
	protected StringBuilder content;
	protected String location;
	protected int httpStatus;

	public HttpClient() {
		init(Constants.USER_AGENT2);
	}

	public HttpClient(String ua) {
		init(ua);
	}

	private void init(String ua) {
		client = new DefaultHttpClient();

		client.setRedirectHandler(new DefaultRedirectHandler() {
			public URI getLocationURI(HttpResponse res, HttpContext arg1) throws ProtocolException {
				URI uri = super.getLocationURI(res, arg1);
				if (res.getFirstHeader("Location") != null) {
					location = uri.toString();// res.getFirstHeader("Location").getValue();
				}
				return uri;
			}
		});
		client.getParams().setParameter("http.useragent", ua);
	}

	public void post(String url, String data) throws WebApiException {
		try {
			post(url, new StringEntity(data, "UTF-8"), true);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new WebApiException(WebApiException.BAD_REQUEST, "param error");
		}
	}

	public void post(String url, Params params) throws WebApiException {
		post(url, params, true);
	}

	public HttpResponse post(String url, Params params, boolean needContent) throws WebApiException {
		try {
			return post(url, new UrlEncodedFormEntity(params, HTTP.UTF_8), needContent);
		} catch (UnsupportedEncodingException e) {
			throw new WebApiException(WebApiException.BAD_REQUEST, "param error");
		}

	}

	private HttpResponse post(String url, StringEntity data, boolean needContent) throws WebApiException {
		content = null;
		httpStatus = 0;
		//data.setContentType("application/x-www-form-urlencoded");
		//Log.d("niconico",data.getContentEncoding().());
		try {
			long stime = System.currentTimeMillis();
			// Log.d("HttpClient", "POST " + url);
			HttpPost request = new HttpPost(url);
			request.setEntity(data);
			HttpResponse response = client.execute(request);

			httpStatus = response.getStatusLine().getStatusCode();
			if (httpStatus != HttpStatus.SC_OK) {
				throw new WebApiException(WebApiException.NETWORK_ERROR, "HTTP status error");
			}
			if (needContent)
				read(response);
			
			Log.d("HttpClient", " load time:" + (System.currentTimeMillis() - stime));
			return response;
		} catch (Exception e) {
			throw new WebApiException(WebApiException.NETWORK_ERROR, "Network error");
		}
	}

	public CharSequence getContent(String url) throws WebApiException {
		content = null;
		try {
			long stime = System.currentTimeMillis();
			Log.d("HttpClient", "GET " + url);
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
				throw new WebApiException(WebApiException.SERVICE_UNAVAILABLE);
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new WebApiException(WebApiException.NETWORK_ERROR, "HTTP status error");
			}
			read(response);
			Log.d("HttpClient", " load time:" + (System.currentTimeMillis() - stime));
			return content;
		} catch (Exception e) {
			throw new WebApiException(WebApiException.NETWORK_ERROR, "Network error");
		}

	}

	public boolean get(String url) {
		try {
			getContent(url);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public byte[] getBytes(String url) {
		content = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}

			int len;
			byte[] readbuf = new byte[8192];
			HttpEntity ent = response.getEntity();
			InputStream is = ent.getContent();
			while ((len = is.read(readbuf)) > 0) {
				baos.write(readbuf, 0, len);
			}
			is.close();
		} catch (Exception e) {
			return null;
		}

		return baos.toByteArray();
	}

	public boolean head(String url) throws WebApiException {
		content = null;
		try {
			HttpHead request = new HttpHead(url);
			HttpResponse response;
			response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
				throw new WebApiException(WebApiException.BAD_REQUEST, "param error");
			}
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
				throw new WebApiException(WebApiException.SERVICE_UNAVAILABLE);
			}
		} catch (Exception e) {
			throw new WebApiException(WebApiException.NETWORK_ERROR, "Network error");
		}

		return true;
	}

	private void read(HttpResponse res) throws IOException {
		HttpEntity ent = res.getEntity();
		InputStream inputStream = ent.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		content = new StringBuilder();

		char buf[] = new char[4096];
		int size;
		while ((size = reader.read(buf, 0, buf.length)) >= 0) {
			content.append(String.valueOf(buf, 0, size));
		}
		inputStream.close();
	}

	public List<Cookie> getCookies() {
		List<Cookie> cookies = client.getCookieStore().getCookies();
		return cookies;
	}

	public void setCookie(Cookie cookie) {
		client.getCookieStore().addCookie(cookie);
	}

	public String toString() {
		return content.toString();
	}

	public String getContent() {
		return content.toString();
	}

	public CharSequence getCharSequence() {
		return content;
	}

	public String getLocation() {
		return location;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public static class Params extends ArrayList<NameValuePair> {
		private static final long serialVersionUID = 2525123L;

		public void put(String key, String value) {
			add(new BasicNameValuePair(key, value));
		}
	}

}
