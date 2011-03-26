package com.bryanjswift.swiftnote.net;

import android.util.Log;
import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.util.Base64;
import com.bryanjswift.swiftnote.util.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Api {
    private static final String LOGGING_TAG = Constants.TAG + "Api";
	/**
	 * Class to represent the response from an API call
	 */
	public static class Response {
		public final int status;
		public final String body;
		public final Map<String, List<String>> headers;
        public Response(final int status, final String body, final Map<String, List<String>> headers) {
            this.status = status;
            this.body = body;
            this.headers = headers;
        }
        public Response(final int status) {
            this(status, null, null);
        }
	}
	/**
	 * Sends an HTTP POST request
	 *
	 * @param url to connect to
	 * @param data to send in POST body
	 * @return Response object containing status code and response body
     * @throws IOException never?
	 */
    public static Api.Response Post(final String url, final String data) throws IOException {
        final HttpClient client = new DefaultHttpClient();
        Api.Response apiResponse = new Api.Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        try {
            final URI uri = new URI(url);
            final HttpPost post = new HttpPost(uri);
            post.setEntity(new StringEntity(data));
            final HttpResponse response = client.execute(post);
            final HttpEntity entity = response.getEntity();
            final int status = response.getStatusLine().getStatusCode();
            Log.i(LOGGING_TAG, "API (POST) call to " + uri.toString() + " returned with " + status + " status");
            apiResponse = new Api.Response(status, IOUtils.slurp(entity.getContent()), extractHeaders(response));
        } catch (URISyntaxException urise) {
            Log.e(LOGGING_TAG, "Couldn't create URI", urise);
        } catch (UnsupportedEncodingException uee) {
            Log.e(LOGGING_TAG, "Encountered unsupported encoding", uee);
        } catch (ClientProtocolException cpe) {
            Log.e(LOGGING_TAG, "Wrong protocol", cpe);
        } catch (IOException ioe) {
            Log.e(LOGGING_TAG, "Something bad happened", ioe);
        }
        return apiResponse;
    }
	/**
	 * Sends an HTTP GET request
	 *
	 * @param url to connect to
	 * @return Response object containing status code and response body
     * @throws IOException never?
	 */
    public static Api.Response Get(final String url) throws IOException {
        final HttpClient client = new DefaultHttpClient();
        Api.Response apiResponse = new Api.Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        try {
            final URI uri = new URI(url);
            final HttpGet get = new HttpGet(uri);
            final HttpResponse response = client.execute(get);
            final HttpEntity entity = response.getEntity();
            final int status = response.getStatusLine().getStatusCode();
            Log.i(LOGGING_TAG, "API (GET) call to " + uri.toString() + " returned with " + status + " status");
            final String body = status == HttpStatus.SC_OK ? IOUtils.slurp(entity.getContent()) : null;
            apiResponse = new Api.Response(status, body, extractHeaders(response));
        } catch (URISyntaxException urise) {
            Log.e(LOGGING_TAG, "Couldn't create URI", urise);
        } catch (UnsupportedEncodingException uee) {
            Log.e(LOGGING_TAG, "Encountered unsupported encoding", uee);
        } catch (ClientProtocolException cpe) {
            Log.e(LOGGING_TAG, "Wrong protocol", cpe);
        } catch (IOException ioe) {
            Log.e(LOGGING_TAG, "Something bad happened", ioe);
        }
        return apiResponse;
    }

    /**
     * Create a map of header names to header values from an HttpResponse
     * @param response to create header map from
     * @return map of header name to value list
     */
    public static Map<String, List<String>> extractHeaders(final HttpResponse response) {
        final HeaderIterator headersIterator = response.headerIterator();
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        while (headersIterator.hasNext()) {
            final Header header = headersIterator.nextHeader();
            final String name = header.getName();
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<String>();
            }
            values.add(header.getValue());
            headers.put(name, values);
        }
        return headers;
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
    /**
     * Convenient holder for credential information
     */
    public static class Credentials {
        public final String email;
        public final String password;
        public final String auth;
        public Credentials(String email, String password, String auth) {
            this.email = email;
            this.password = password;
            this.auth = auth;
        }
        public boolean hasAuth() {
            return !(email == null || email.equals("")) && !(auth == null || auth.equals(""));
        }
        public boolean hasCreds() {
            return !(email == null || email.equals("")) && !(password == null || password.equals(""));
        }
    }
}
