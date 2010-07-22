package com.bryanjswift.simplenote.ui;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.app.Notifications;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.AndroidSimpleNoteApi;
import com.bryanjswift.simplenote.net.ServerCreateCallback;
import com.bryanjswift.simplenote.net.ServerSaveCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.thread.LoginWithCredentials;
import com.bryanjswift.simplenote.thread.SyncNotesThread;
import com.bryanjswift.simplenote.widget.NotesAdapter;

/**
 * 'Main' Activity to List notes
 * @author bryanjswift
 */
public class SimpleNoteList extends ListActivity {
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteList";
	private static final String SCROLL_POSITION = "scrollY";
	/** Intent Action to update notes via BroadcastReceiver */
	public static final String UPDATE = SimpleNoteList.class.getName() + ".UPDATE_NOTES";
	/** Interface for accessing the SimpleNote database on the device */
	private final SimpleNoteDao dao;
	/**
	 * Create a dao to store using this as the context
	 */
	public SimpleNoteList() {
		super();
		this.dao = new SimpleNoteDao(this);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		int scrollY = 0;
		if (savedState != null && savedState.getInt(Constants.REQUEST_KEY) == Constants.REQUEST_EDIT) {
			Log.d(LOGGING_TAG, "Resuming note editing from a saved state");
			FireIntent.EditNote(this, savedState.getLong(BaseColumns._ID), savedState.getString(SimpleNoteDao.BODY));
		} else if (savedState != null) {
			scrollY = savedState.getInt(SCROLL_POSITION, 0);
		}
		Log.d(LOGGING_TAG, "Firing up the note list");
		// Set content view based on Notes currently in the database
		setContentView(R.layout.notes_list);
		Note[] notes = dao.retrieveAll();
		// Now create a note adapter and set it to display
		ListAdapter notesAdapter = new NotesAdapter(this, notes);
		setListAdapter(notesAdapter);
		// Make sure onCreateContextMenu is called for long press of notes
		registerForContextMenu(getListView());
		// check the token exists first and if not authenticate with existing username/password
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		if (credentials.containsKey(Preferences.TOKEN)) {
			// sync notes in a background thread
			syncNotes(credentials.get(Preferences.EMAIL), credentials.get(Preferences.TOKEN));
		} else {
			(new LoginWithCredentials(this, credentials)).start();
		}
		// restore the scroll position
		findViewById(android.R.id.list).scrollTo(0, scrollY);
	}
	/**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(updateNoteReceiver, new IntentFilter(SimpleNoteList.UPDATE));
		refreshNotes();
	}
	/**
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(updateNoteReceiver);
	}
	/**
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(LOGGING_TAG, String.format("onActivityResult firing with resultCode: %d", resultCode));
		switch (requestCode) {
			case Constants.REQUEST_LOGIN: handleSigninResult(resultCode, data); break;
			case Constants.REQUEST_EDIT: handleNoteEditResult(resultCode, data); break;
		}
	}
	/**
	 * Edit notes when clicked
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		FireIntent.EditNote(this, id, null);
	}
	/**
	 * If SimpleNoteEdit saved state then retrieve it and go back to editing
	 * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		if (state != null && state.getInt(Constants.REQUEST_KEY) == Constants.REQUEST_EDIT) {
			Log.d(LOGGING_TAG, "Resuming edit note from a saved state");
			FireIntent.EditNote(this, state.getLong(BaseColumns._ID), state.getString(SimpleNoteDao.BODY));
		}
	}
	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_list, menu);
		return true;
	}
	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
				syncNotes(credentials.get(Preferences.EMAIL), credentials.get(Preferences.TOKEN));
				return true;
			case R.id.menu_preferences:
				FireIntent.Preferences(this);
				return true;
			case R.id.menu_add:
				FireIntent.EditNote(this, Constants.DEFAULT_ID, "");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	/**
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SCROLL_POSITION, findViewById(android.R.id.list).getScrollY());
	}
	/**
	 * Create a menu with delete and edit as options
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_list_item, menu);
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
		final Note note = (Note) adapter.getItem(info.position);
		menu.setHeaderTitle(note.getTitle());
	}
	/**
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
		switch (item.getItemId()) {
			case R.id.menu_delete_note:
				final Note note = dao.retrieve(adapter.getItemId(info.position));
				if (dao.delete(note)) {
					// Have to re-retrieve because deleting doesn't update the note passed to delete
					updateNotesFor(dao.retrieve(note.getId()), true);
				}
				return true;
			case R.id.menu_edit:
				FireIntent.EditNote(this, adapter.getItemId(info.position), null);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	/**
	 * BroadcastReceiver which will receive requests to update from background sync services
	 */
	public BroadcastReceiver updateNoteReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOGGING_TAG, "Received broadcast to refresh notes in list");
			refreshNotes();
		}
	};
	/**
	 * Message handler which should update the UI when a message with a Note is received
	 */
	private Handler updateNoteHandler = new Handler() {
		/**
		 * Messages are received from SyncNotesThread when a Note is updated or retrieved from the server
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(final Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constants.MESSAGE_UPDATE_NOTE: handleUpdateNote(msg); break;
				case Constants.MESSAGE_UPDATE_FINISHED: handleUpdateFinished(msg); break;
				case Constants.MESSAGE_UPDATE_STARTED: handleUpdateStarted(msg); break;
				default: break;
			}
		}
		/**
		 * Handle the update note message
		 * @param msg with Note information
		 */
		private void handleUpdateNote(Message msg) {
			// update the UI with the new note
			final Note note = (Note) msg.getData().getSerializable(Note.class.getName());
			if (note.isDeleted()) {
				final HashMap<String,String> credentials = Preferences.getLoginPreferences(SimpleNoteList.this);
				final String email = credentials.get(Preferences.EMAIL);
				final String auth = credentials.get(Preferences.TOKEN);
				if (!note.getKey().equals(Constants.DEFAULT_KEY) && note.isDeleted()) {
					Log.d(LOGGING_TAG, "Deleting note on the server");
					SimpleNoteApi.delete(note, auth, email, new ServerSaveCallback(SimpleNoteList.this, note));
				}
			}
			// Only refresh if told to.. should only be told to if it's an update from this Activity
			if (msg.getData().getBoolean(Constants.DATA_REFRESH_NOTES)) {
				refreshNotes();
			}
		}
		/**
		 * Handle the update started message
		 * @param msg with any relevant information
		 */
		private void handleUpdateStarted(final Message msg) {
			Notifications.Syncing(SimpleNoteList.this);
		}
		/**
		 * Handle the update finished message
		 * @param msg with any relevant information
		 */
		private void handleUpdateFinished(final Message msg) {
			Notifications.CancelSyncing(SimpleNoteList.this);
		}
	};
	/**
	 * Start up a note syncing thread
	 * @param email account identifier for notes
	 * @param auth token used for access after login API call
	 */
	private void syncNotes(String email, String auth) {
		final AndroidSimpleNoteApi api = new AndroidSimpleNoteApi(this, updateNoteHandler);
		final Thread sync = new SyncNotesThread(api);
		sync.start();
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity start
	 * @param resultCode how the LoginDialog Activity finished
	 * @param data the intent that started the LoginDialog Activity
	 */
	private void handleSigninResult(final int resultCode, final Intent data) {
		// Assume this only gets called when LoginDialog completed successfully
		if (resultCode == RESULT_OK) {
			final Bundle extras = data.getExtras();
			syncNotes(extras.getString(Preferences.EMAIL), extras.getString(Preferences.TOKEN));
		}
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity
	 * @param resultCode how the SimpleNoteEdit Activity finished
	 * @param data the intent that started the SimpleNoteEdit Activity
	 */
	private void handleNoteEditResult(final int resultCode, final Intent data) {
		switch (resultCode) {
			case RESULT_OK:
				final Note note = (Note) data.getExtras().getSerializable(Note.class.getName());
				// Note modified, refresh the list
				updateNotesFor(note, false);
				// Send updated note to the server
				final HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
				final String email = credentials.get(Preferences.EMAIL);
				final String auth = credentials.get(Preferences.TOKEN);
				if (!note.getKey().equals(Constants.DEFAULT_KEY) && note.isDeleted()) {
					Log.d(LOGGING_TAG, "Deleting note on the server");
					SimpleNoteApi.delete(note, auth, email, new ServerSaveCallback(this, note));
				} else if (note.getKey().equals(Constants.DEFAULT_KEY)) {
					Log.d(LOGGING_TAG, "Creating a new note on the server");
					SimpleNoteApi.create(note, auth, email, new ServerCreateCallback(this, note));
				} else {
					Log.d(LOGGING_TAG, String.format("Sending note '%s' to SimpleNoteApi", note.getKey()));
					SimpleNoteApi.update(note, auth, email, new ServerSaveCallback(this, note));
				}
				break;
			case RESULT_CANCELED:
				// not modified
				// Since the note wasn't changed nothing should need to be done here
				break;
		}
	}
	/**
	 * Internal method to trigger a note refresh
	 * @param note causing the refresh
	 */
	private void updateNotesFor(Note note, boolean refresh) {
		// Note modified, refresh the list
		final Message message = Message.obtain(updateNoteHandler, Constants.MESSAGE_UPDATE_NOTE);
		message.setData(new Bundle());
		final Bundle data = message.getData();
		data.putSerializable(Note.class.getName(), note);
		data.putBoolean(Constants.DATA_REFRESH_NOTES, refresh);
		message.sendToTarget();
	}
	/**
	 * Pull notes from database and update the NotesAdapter
	 */
	private void refreshNotes() {
		runOnUiThread(new Runnable() {
			public void run() {
				final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
				adapter.setNotes(dao.retrieveAll());
				adapter.notifyDataSetChanged();
			}
		});
	}
}
