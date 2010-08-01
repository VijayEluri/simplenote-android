package com.bryanjswift.simplenote.ui;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.app.UpdateNoteHandler;
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
	/** Interface for accessing the SimpleNote database on the device */
	private final SimpleNoteDao dao;
	/** Message handler which should update the UI when a message with a Note is received */
	private final Handler updateNoteHandler;
	/** BroadcastReceiver which will receive requests to update from background sync services */
	private final BroadcastReceiver updateNoteReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOGGING_TAG, "Received broadcast to refresh notes in list");
			refreshNotes();
		}
	};
    private static DisplayMetrics display = new DisplayMetrics();
    private static int paddingHeight = -1;
    private static int shadowHeight = -1;
    private static int rowHeight = -1;
	/**
	 * Create a dao to store using this as the context
	 */
	public SimpleNoteList() {
		super();
		this.dao = new SimpleNoteDao(this);
		this.updateNoteHandler = new UpdateNoteHandler(this, true);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		int scrollY = 0;
        if (paddingHeight == -1 && shadowHeight == -1) {
            getWindowManager().getDefaultDisplay().getMetrics(display);
            paddingHeight = Math.round(getResources().getInteger(R.integer.noteListPadding) * display.density);
            // 5.33333333333 is the assumed height of the scrolling shadow at 160 dpi
            shadowHeight = Math.round(5.333333333333333333333333333f * display.density);
            rowHeight = Math.round(getResources().getInteger(R.integer.noteItemHeight) * display.density);
        }
		if (savedState != null && savedState.getInt(Constants.REQUEST_KEY) == Constants.REQUEST_EDIT) {
			Log.d(LOGGING_TAG, "Resuming note editing from a saved state");
			FireIntent.EditNote(this, savedState.getLong(BaseColumns._ID), savedState.getString(SimpleNoteDao.BODY));
		} else if (savedState != null) {
			scrollY = savedState.getInt(SCROLL_POSITION, 0);
		}
		Log.d(LOGGING_TAG, "Firing up the note list");
		getWindow().setFormat(PixelFormat.RGBA_8888);
		// Now get notes and create a note adapter and set it to display
        Note[] notes = dao.retrieveAll();
		setListAdapter(new NotesAdapter(this, notes));
        // Set content view based on Notes currently in the database
        setContentView(R.layout.notes_list);
		// Make sure onCreateContextMenu is called for long press of notes
		registerForContextMenu(getListView());
		// check the token exists first and if not authenticate with existing username/password
		Preferences.Credentials credentials = Preferences.getLoginPreferences(this);
		if (!credentials.auth.equals("")) {
			// sync notes in a background thread
			syncNotes();
		} else {
			(new LoginWithCredentials(this, credentials)).start();
		}
        // bind click listener to new note button
        findViewById(R.id.note_add).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FireIntent.EditNote(SimpleNoteList.this, Constants.DEFAULT_ID, "");
            }
        });
		// restore the scroll position
		getListView().scrollTo(0, scrollY);
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
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(updateNoteReceiver, new IntentFilter(Constants.BROADCAST_UPDATE_NOTES));
		refreshNotes();
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
			case Constants.REQUEST_LOGIN: handleSigninResult(resultCode); break;
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
				syncNotes();
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
		final Note note = adapter.getItem(info.position);
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
					updateNotesFor(dao.retrieve(note.getId()));
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
	 * Start up a note syncing thread
     */
	private void syncNotes() {
		final AndroidSimpleNoteApi api = new AndroidSimpleNoteApi(this, updateNoteHandler);
		final Thread sync = new SyncNotesThread(api);
		sync.start();
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity start
	 * @param resultCode how the LoginDialog Activity finished
     */
	private void handleSigninResult(final int resultCode) {
		// Assume this only gets called when LoginDialog completed successfully
		if (resultCode == RESULT_OK) {
			syncNotes();
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
				// Note modified, list refreshed by onResume
				// Send updated note to the server
				final Preferences.Credentials credentials = Preferences.getLoginPreferences(this);
				final String email = credentials.email;
				final String auth = credentials.auth;
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
	private void updateNotesFor(final Note note) {
		// Note modified, refresh the list
		final Message message = Message.obtain(updateNoteHandler, Constants.MESSAGE_UPDATE_NOTE);
		message.setData(new Bundle());
		final Bundle data = message.getData();
		data.putSerializable(Note.class.getName(), note);
		message.sendToTarget();
	}
	/**
	 * Pull notes from database and update the NotesAdapter
	 */
	private void refreshNotes() {
        Log.d(LOGGING_TAG, "Refreshing notes from DB");
        final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
        adapter.setNotes(dao.retrieveAll());
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
                updateShadow();
			}
		});
	}

    /**
     * Add padding to show drop shadow below list when scrolling is disabled
     */
    private void updateShadow() {
        if (isScrollable()) {
            // scrollable so hide shadow
            getListView().setPadding(0, 0, 0, 0);
        } else {
            // not scrollable so show the shadow
            getListView().setPadding(0, 0, 0, paddingHeight);
        }
    }

    /**
     * Check the display height against the list height
     * @return whether the list height is greater than or equal to the display height
     */
    private boolean isScrollable() {
        int displayHeight = display.heightPixels - paddingHeight - shadowHeight;
        int listHeight = getListAdapter().getCount() * rowHeight;
        Log.d(LOGGING_TAG, String.format("DisplayHeight: %d :: ListHeight: %d", displayHeight, listHeight));
        return listHeight >= displayHeight;
    }
}
