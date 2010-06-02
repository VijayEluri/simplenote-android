package com.simplenote.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class APIBase {
	
	public static class Response {
		public int statusCode;
		public String resp;
	}
	
	/**
	 * Sends an HTTP POST request
	 * 
	 * @param url
	 * @param postData
	 * @return Response object containing status code and response body
	 */
	public static Response HTTPPost( String url, String postData ) {
		Response response = new Response();
		try {
			// Setup connection
			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod( "POST" );
			conn.setDoOutput( true );
			conn.connect();
			
			// Send POST data to the server
			OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
			out.write( postData );
			out.flush();
			
			// Get the response from the server
			BufferedReader in = new BufferedReader (new InputStreamReader( conn.getInputStream() ));
			StringBuilder sb = new StringBuilder();
			
			for ( String line = in.readLine(); line != null; line = in.readLine() ) {
				sb.append(line + '\n');
			}
			
			// Store response information in Response object
			try {
				response.statusCode = conn.getResponseCode();
			} catch (IOException e) {
				response.statusCode = 401;
			}
			response.resp = sb.toString();
			
			// Clean up
			conn.disconnect();
			conn = null;
			out  = null;
			in   = null;
			sb   = null;
			
		} catch (IOException e) {
			response.statusCode = 401;
		}
		return response;
	}
	
	/**
	 * Sends an HTTP GET request
	 * 
	 * @param url
	 * @return Response object containing status  code and response body
	 */
	public static Response HTTPGet( String url ) {
		Response response = new Response();
		try {
			// Setup connection
			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			// Get response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}

			// Store response in a Response object
			try {
				response.statusCode = conn.getResponseCode();
			} catch (IOException e) {
				response.statusCode = 401;
			}
			response.resp = sb.toString();

			// Clean up
			conn.disconnect();
			conn = null;
			in = null;
			sb = null;
		} catch (IOException e) {
			response.statusCode = 401;
		}
		return response;
	}
	
	public static String encode( String str ) {
		return encode( str, false ); // Don't Base64 encode by default
	}
	
	public static String encode( String str, Boolean base64Encode ) {
		try {
			return base64Encode ? Base64.encodeBytes( URLEncoder.encode( str, "UTF-8" ).getBytes() ) : URLEncoder.encode( str, "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			return base64Encode ? Base64.encodeBytes( URLEncoder.encode( str ).getBytes() ) : URLEncoder.encode( str );
		}
	}

}
