package com.bryanjswift.swiftnote.net;

import com.bryanjswift.swiftnote.net.Api.Response;

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
	 * @param response contents of the response
	 */
	public void on200(Response response) { }
	/**
	 * Bad Request
	 * @param response contents of the response
	 */
	public void on400(Response response) { }
	/**
	 * Unauthorized
	 * @param response contents of the response
	 */
	public void on401(Response response) { }
	/**
	 * Forbidden
	 * @param response contents of the response
	 */
	public void on403(Response response) { }
	/**
	 * Not Found
	 * @param response contents of the response
	 */
	public void on404(Response response) { }
	/**
	 * Server error
	 * @param response contents of the response
	 */
	public void on500(Response response) { }
	/**
	 * Called when the request is finished no matter what the response was
	 * @param response contents of the response
	 */
	public void onComplete(Response response) { }
	/**
	 * Called when the request is finished and the status code was anything indicating an error
	 * @param response contents of the response
	 */
	public void onError(Response response) { }
	/**
	 * Called when an exception is thrown, this isn't really an HTTP case
	 * @param url requested that caused the exception
	 * @param data sent with the request
	 * @param t - the exception thrown
	 */
	public void onException(String url, String data, Throwable t) { }
}
