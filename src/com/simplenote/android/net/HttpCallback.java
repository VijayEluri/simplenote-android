package com.simplenote.android.net;

import java.util.List;
import java.util.Map;

/**
 * Class to represent what should happen for different HTTP status codes
 * @author bryanjswift
 */
public abstract class HttpCallback {
	/** Create a static empty callback object */
	public static final HttpCallback EMPTY;
	static {
		EMPTY = new HttpCallback() { };
	}
	/**
	 * Called when the response contained a success status code
	 * @param body contents of the response
	 */
	public void on200(String body) { }
	/**
	 * Bad Request
	 * @param body contents of the response
	 */
	public void on400(String body) { }
	/**
	 * Unauthorized
	 * @param body contents of the response
	 */
	public void on401(String body) { }
	/**
	 * Forbidden
	 * @param body contents of the response
	 */
	public void on403(String body) { }
	/**
	 * Not Found
	 * @param body contents of the response
	 */
	public void on404(String body) { }
	/**
	 * Server error
	 * @param body contents of the response
	 */
	public void on500(String body) { }
	/**
	 * Called when the request is finished no matter what the response was
	 * @param body contents of the response
	 */
	public void onComplete(String body) { }
	/**
	 * Called when the request is finished and the status code was anything indicating an error
	 * @param status code causing the error
	 * @param body contents of the response
	 * @param headers included in the response
	 */
	public void onError(int status, String body, Map<String, List<String>> headers) { }
	/**
	 * Called when an exception is thrown, this isn't really an HTTP case
	 * @param url requested that caused the exception
	 * @param data sent with the request
	 * @param t - the exception thrown
	 */
	public void onException(String url, String data, Throwable t) { }
}
