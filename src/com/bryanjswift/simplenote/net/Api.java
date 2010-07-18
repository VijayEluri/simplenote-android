package com.bryanjswift.simplenote.net;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.bryanjswift.simplenote.util.Base64;

public abstract class Api {
	/**
	 * Class to represent the response from an API call
	 */
	public static class Response {
		public int status;
		public String body;
		public Map<String, List<String>> headers;
	}
	/**
	 * Sends an HTTP POST request
	 *
	 * @param url to connect to
	 * @param data to send in POST body
	 * @return Response object containing status code and response body
	 * @throws IOException when there is a problem establishing a connection
	 */
	public static Response Post(String url, String data) throws IOException {
		Response response = new Response();
		try {
			// Setup connection
			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.connect();
			// Send POST data to the server
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(data);
			out.flush();
			// Get the response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()),8192);
			StringBuilder sb = new StringBuilder();
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}
			sb.deleteCharAt(sb.length() - 1); // Remove extraneous CR/LF
			// Store response information in Response object
			response.status = conn.getResponseCode();
			response.body = sb.toString();
			response.headers = conn.getHeaderFields();
			// Clean up
			conn.disconnect();
			conn = null;
			out  = null;
			in   = null;
			sb   = null;
		} catch (FileNotFoundException fnfe) {
			// I'm not sure why but when the login fails we get a FileNotFoundException
			response.status = 401;
		}
		return response;
	}
	/**
	 * Sends an HTTP GET request
	 *
	 * @param url to connect to
	 * @return Response object containing status code and response body
	 * @throws IOException when there is a problem establishing a connection
	 */
	public static Response Get(String url) throws IOException {
		Response response = new Response();
		// Setup connection
		HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
		conn.setRequestMethod("GET");
		conn.connect();
		// Get response from the server
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()),8192);
		StringBuilder sb = new StringBuilder();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			sb.append(line + '\n');
		}
		sb.deleteCharAt(sb.length() - 1); // Remove extraneous CR/LF
		// Store response in a Response object
		response.status = conn.getResponseCode();
		response.body = sb.toString();
		response.headers = conn.getHeaderFields();
		// Clean up
		conn.disconnect();
		conn = null;
		in = null;
		sb = null;
		return response;
	}
	/**
	 * Quick way to URL encode a String
	 * @param str to encode
	 * @return URL encoded String
	 */
	public static String encode(String str) {
		return encode(str, false, true); // Don't Base64 encode by default
	}
	/**
	 * Encode a String, optionally with base64 as well as URL encoding
	 * @param str to encode
	 * @param base64Encode - whether or not to use base64 encoding
	 * @return encoded String
	 */
	public static String encode(String str, Boolean base64Encode) {
		return encode(str, base64Encode, true);
	}
	/**
	 * Encode a String, optionally with base64, optionally with URL encoding
	 * @param str to encode
	 * @param base64Encode - whether or not to use base64 encoding
	 * @param urlEncode - whether or not to use URL encoding
	 * @return encoded String
	 */
	public static String encode(String str, boolean base64Encode, boolean urlEncode) {
		if (urlEncode) {
			try {
				return base64Encode ? Base64.encodeBytes(URLEncoder.encode(str, "UTF-8").getBytes()) : URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return base64Encode ? Base64.encodeBytes(URLEncoder.encode(str).getBytes()) : URLEncoder.encode(str);
			}
		} else {
			return base64Encode ? Base64.encodeBytes(str.getBytes()) : str;
		}
	}
}
