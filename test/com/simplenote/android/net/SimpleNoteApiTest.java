package com.simplenote.android.net;

import com.bryanjswift.simplenote.net.Api;
import junit.framework.TestCase;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.net.HttpCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;

/**
 * Exercise the SimpleNoteApi class
 * @author bryanjswift
 */
public class SimpleNoteApiTest extends TestCase {
	public static final String LOGGING_TAG = Constants.TAG + "SimpleNoteApiTest";
    public final Api.Credentials credentials = new Api.Credentials("simplenote@bryanjswift.com", "simplenote1234", "");
    public final Api.Credentials badCredentials = new Api.Credentials("simplenote@bryanjswift.com", "bad password", "");

	String noteBody;

	private abstract class FailingCallback extends HttpCallback {
		@Override
		public void on200(Response response) {
			fail("Should not have returned a 200 response code");
		}
		@Override
		public void on400(Response response) {
			fail("Should not have returned a 400 response code");
		}
		@Override
		public void on401(Response response) {
			fail("Should not have returned a 401 response code");
		}
		@Override
		public void on403(Response response) {
			fail("Should not have returned a 403 response code");
		}
		@Override
		public void on404(Response response) {
			fail("Should not have returned a 404 response code");
		}
		@Override
		public void on500(Response response) {
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
		SimpleNoteApi.login(credentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				assertTrue(response.body.length() > 0);
			}
		});
	}
	/**
	 * Test failed login gets a 401 response
	 */
	public void testFailingLogin() {
		SimpleNoteApi.login(badCredentials, new FailingCallback() {
			@Override
			public void on401(Response response) {
				// if we are here we should pass
			}
		});
	}
	/**
	 * Test able to retrieve notes after successful login
	 */
	public void testIndexAndNoteRetrieval() {
		String token = SimpleNoteApi.login(credentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				assertTrue(response.body.length() > 0);
			}
		}).body;
        Api.Credentials authedCredentials = new Api.Credentials(credentials.email, credentials.password, token);
		Note[] notes = SimpleNoteApi.index(authedCredentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				assertTrue(response.body.length() > 0);
			}
		});
		assertTrue(notes.length > 0);
		Note note = SimpleNoteApi.retrieve(notes[0], authedCredentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				// If we are here we passed
				noteBody = response.body;
			}
		});
		assertEquals(note.getTitleAndBody(), noteBody);
	}
	/**
	 * Test we are able to update notes
	 */
	public void testNoteUpdate() {
		String token = SimpleNoteApi.login(credentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				assertTrue(response.body.length() > 0);
			}
		}).body;
        Api.Credentials authedCredentials = new Api.Credentials(credentials.email, credentials.password, token);
		Note[] notes = SimpleNoteApi.index(authedCredentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				assertTrue(response.body.length() > 0);
			}
		});
		assertTrue(notes.length > 0);
		final Note serverNote = SimpleNoteApi.retrieve(notes[0], authedCredentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				// If we are here we passed
				noteBody = response.body;
			}
		});
		assertEquals(serverNote.getTitleAndBody(), noteBody);
		final Note newNote = serverNote.setBody(serverNote.getBody() + "\n Test appended");
		SimpleNoteApi.update(newNote, authedCredentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				// passed
			}
		});
		SimpleNoteApi.update(serverNote, authedCredentials, new FailingCallback() {
			@Override
			public void on200(Response response) {
				// passed
			}
		});
	}
}
