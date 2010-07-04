package com.simplenote.android;

import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SimpleNote extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int LOGIN_ID  = Menu.FIRST + 2;
	private static final int PREFERENCES_ID = Menu.FIRST + 3;
	private static final int CLEAR_TOKEN_ID = Menu.FIRST + 4;

	private NotesDbAdapter mDbHelper;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor;
	private String mUserToken;
	public JSONObject mUserData;
	public ProgressDialog mProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);
		mPrefsEditor = mPrefs.edit();
		mUserToken = mPrefs.getString("token", null);

		setContentView(R.layout.notes_list);
		if (mUserToken == null) { // Get login credentials
			loginUser();
		} else { // User is "logged in"
			mDbHelper = new NotesDbAdapter(this);
			fillData();
			registerForContextMenu(getListView());
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onStop() {
		if ( mDbHelper != null ) { mDbHelper.close(); }
		super.onStop();
	}

	private void fillData() {
		mDbHelper.open();
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		startManagingCursor(notesCursor);

		// Create an array to specify the fields we want to display in the list
		String[] from = new String[]{
			NotesDbAdapter.KEY_TITLE,
			NotesDbAdapter.KEY_DATESTAMP
		};

		// and an array of the fields we want to bind those fields to
		int[] to = new int[]{
			R.id.text_title,
			R.id.text_date
		};

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
		setListAdapter(notes);

		mDbHelper.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO: Move the menu buttons to /res/menu/menu.xml
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		menu.add(0, LOGIN_ID, 0, R.string.menu_login);
		menu.add(0, PREFERENCES_ID, 0, R.string.menu_preferences);
		menu.add(0, CLEAR_TOKEN_ID, 0, R.string.menu_clear_credentials);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case INSERT_ID:
			createNote();
			return true;
		case LOGIN_ID:
			loginUser();
			return true;
		case PREFERENCES_ID:
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);
			return true;
		case CLEAR_TOKEN_ID:
			Log.i(Constants.TAG, "Clearing saved credentials");
			mPrefsEditor.putString("email", null);
			mPrefsEditor.putString("password", null);
			mPrefsEditor.putString("token", null);
			mPrefsEditor.commit();
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deleteNote(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createNote() {
		Intent i = new Intent(this, NoteEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	private void loginUser() {
		Intent i = new Intent(SimpleNote.this, LoginDialog.class);
		startActivity(i);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, NoteEdit.class);
		i.putExtra(NotesDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
}