package com.simplenote.android;

import java.util.Calendar;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class SimpleNote extends ListActivity {
  private static final int ACTIVITY_CREATE=0;
  private static final int ACTIVITY_EDIT=1;
  private static final int ACTIVITY_LOGIN=2;

  private static final int INSERT_ID = Menu.FIRST;
  private static final int DELETE_ID = Menu.FIRST + 1;
  private static final int LOGIN_ID  = Menu.FIRST + 2;
  private static final int PREFERENCES_ID  = Menu.FIRST + 3;
  
  private NotesDbAdapter mDbHelper;
  private SharedPreferences mPrefs;
  private SharedPreferences.Editor mPrefsEditor;
  private String mUserEmail;
  private String mUserPassword;
  private String mUserToken;
  public JSONObject mUserData;
  public ProgressDialog mProgressDialog;
  private Thread mThread;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      mPrefs = getSharedPreferences( Constants.PREFS_NAME, 0);
      mPrefsEditor = mPrefs.edit();
      mUserEmail = mPrefs.getString("email", "");
      mUserToken = mPrefs.getString("token", null);
      
      Intent intent = new Intent(this, AlarmReceiver.class);
      intent.putExtra("email", mPrefs.getString("email", ""));
      intent.putExtra("password", mPrefs.getString("password", ""));
      PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      
      // Set up the AlarmManager service
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
      //am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60 * 1000, pendingIntent);
		
      if ( mUserToken == null ) {	// Get login credentials
    	  Intent intent1 = new Intent( SimpleNote.this, LoginDialog.class );
    	  startActivity( intent1 );
    	  SimpleNote.this.finish();
      } else {						// User is "logged in"
	      setContentView(R.layout.notes_list);
	      mDbHelper = new NotesDbAdapter(this);
	      mDbHelper.open();
	      fillData();
	      registerForContextMenu(getListView());

	      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

          Toast.makeText(getBaseContext(),
                  "Back:" + preferences.getAll(),
                  Toast.LENGTH_LONG).show();

      }
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
      SimpleCursorAdapter notes = 
          new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
      setListAdapter(notes);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      menu.add(0, INSERT_ID, 0, R.string.menu_insert);
      menu.add(0, LOGIN_ID, 0, R.string.menu_login);
      menu.add(0, PREFERENCES_ID, 0, R.string.menu_preferences);
      return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
      switch(item.getItemId()) {
          case INSERT_ID:
              Toast.makeText(getBaseContext(),
                      "Back:" + mPrefs.getAll(),
                      Toast.LENGTH_LONG).show();

              createNote();
              return true;
          case LOGIN_ID:
        	  loginUser();
        	  return true;
          case PREFERENCES_ID:
              Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
		      startActivity(settingsActivity);
        	  return true;
      }

      return super.onMenuItemSelected(featureId, item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
          ContextMenuInfo menuInfo) {
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
	  Intent i = new Intent(this, LoginDialog.class);
	  startActivity(i);
	  SimpleNote.this.finish();
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