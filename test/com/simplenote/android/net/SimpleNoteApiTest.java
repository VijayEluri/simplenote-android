package com.simplenote.android.net;

import junit.framework.TestCase;

import com.simplenote.android.Constants;
import com.simplenote.android.model.Note;

/**
 * Exercise the SimpleNoteApi class
 * @author bryanjswift
 */
public class SimpleNoteApiTest extends TestCase {
	public static final String emailAddress = "simplenote@solidstategroup.com";
	public static final String password = "simplenote1234";
	public static final String LOGGING_TAG = Constants.TAG + "SimpleNoteApiTest";

	String noteBody;

	private abstract class FailingCallback extends HttpCallback {
		@Override
		public void on200(String body) {
			fail("Should not have returned a 200 response code");
		}
		@Override
		public void on400(String body) {
			fail("Should not have returned a 400 response code");
		}
		@Override
		public void on401(String body) {
			fail("Should not have returned a 401 response code");
		}
		@Override
		public void on403(String body) {
			fail("Should not have returned a 403 response code");
		}
		@Override
		public void on404(String body) {
			fail("Should not have returned a 404 response code");
		}
		@Override
		public void on500(String body) {
			fail("Should not have returned a 500 response code");
		}
		@Override
		public void onException(String url, String data, Throwable t) {
			fail("Should not have thrown an exception: " + t.getMessage());
		}
	}
	/**
	 * Test we are able to log in successfully
	 */
	public void testSuccessfulLogin() {
		SimpleNoteApi.login(emailAddress, password, new FailingCallback() {
			@Override
			public void on200(String body) {
				assertTrue(body.length() > 0);
			}
		});
	}
	/**
	 * Test failed login gets a 401 response
	 */
	public void testFailingLogin() {
		SimpleNoteApi.login(emailAddress, "password", new FailingCallback() {
			@Override
			public void on401(String body) {
				// if we are here we should pass
			}
		});
	}
	/**
	 * Test able to retrieve notes after successful login
	 */
	public void testIndexAndNoteRetrieval() {
		String validToken = SimpleNoteApi.login(emailAddress, password, new FailingCallback() {
			@Override
			public void on200(String body) {
				assertTrue(body.length() > 0);
			}
		});
		Note[] notes = SimpleNoteApi.index(validToken, emailAddress, new FailingCallback() {
			@Override
			public void on200(String body) {
				assertTrue(body.length() > 0);
			}
		});
		assertTrue(notes.length > 0);
		Note note = SimpleNoteApi.retrieve(notes[0], validToken, emailAddress, new FailingCallback() {
			@Override
			public void on200(String body) {
				// If we are here we passed
				noteBody = body;
			}
		});
		assertEquals(note.getTitleAndBody(), noteBody);
	}
}