package com.bryanjswift.simplenote.ui;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.app.UpdateNoteHandler;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.thread.SyncNotesTask;
import com.bryanjswift.simplenote.widget.NotesAdapter;

/**
 * @author bryanjswift
 */
public abstract class NoteListActivity extends ListActivity {
    private static final String LOGGING_TAG = Constants.TAG + NoteListActivity.class.getSimpleName();
    protected static final String SCROLL_POSITION = "scrollY";
    // Fields to be calculated once
    protected static DisplayMetrics display = new DisplayMetrics();
    /** Padding necessary to display the drop shadow properly */
    protected static int paddingHeight = -1;
    /** Height of the cache hint shadow */
    protected static int shadowHeight = -1;
    /** Height of individual note row */
    protected static int rowHeight = -1;
    /** Height of title bar */
    protected static int titleBarHeight = -1;
    /** Interface for accessing the SimpleNote database on the device */
    protected final SimpleNoteDao dao;
    /** Message handler which should update the UI when a message with a Note is received */
    protected final Handler updateNoteHandler;
    /** BroadcastReceiver which will receive requests to update from background sync services */
    protected final BroadcastReceiver refreshNoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOGGING_TAG, "Received broadcast to refresh notes in list");
            refreshNotes();
        }
    };
    /** BroadcastReceiver to sync notes */
    protected final BroadcastReceiver syncNoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOGGING_TAG, "Received broadcast to sync notes");
            syncNotes();
        }
    };
    public NoteListActivity() {
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
        if (paddingHeight == -1) {
            getWindowManager().getDefaultDisplay().getMetrics(display);
            paddingHeight = Math.round(getResources().getInteger(R.integer.noteListPadding) * display.density);
            // 5.33333333333 is the assumed height of the scrolling shadow at 160 dpi
            shadowHeight = Math.round(5.333333333333333333333333333f * display.density);
            titleBarHeight = Math.round(24 * display.density);
            rowHeight = Math.round(getResources().getInteger(R.integer.noteItemHeight) * display.density);
        }
        getWindow().setFormat(PixelFormat.RGBA_8888);
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
        if (state != null) {
            getListView().scrollTo(0, state.getInt(SCROLL_POSITION, 0));
		}
    }
    /**
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        FireIntent.finishIfUnauthorized(this);
        registerReceiver(refreshNoteReceiver, new IntentFilter(Constants.BROADCAST_REFRESH_NOTES));
        registerReceiver(syncNoteReceiver, new IntentFilter(Constants.BROADCAST_SYNC_NOTES));
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
        unregisterReceiver(refreshNoteReceiver);
        unregisterReceiver(syncNoteReceiver);
    }

    /**
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return ((NotesAdapter) getListAdapter()).getNotes();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
     * Edit notes when clicked
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        FireIntent.EditNote(this, id, null);
    }
    /**
     * Internal method to trigger a note refresh
     * @param note causing the refresh
     */
    protected abstract void updateNotesFor(final Note note);
    /**
     * Pull notes from database and update the NotesAdapter
     */
    protected abstract void refreshNotes();
    /**
     * Start up a note syncing thread
     */
    protected void syncNotes() {
        (new SyncNotesTask(this, updateNoteHandler)).execute();
    }
    /**
     * Add padding to show drop shadow below list when scrolling is disabled
     */
    protected void updateShadow() {
        if (isScrollable()) {
            // scrollable so hide shadow
            getListView().setPadding(0, 0, 0, 0);
        } else {
            // not scrollable so show the shadow
            int listHeight = getListAdapter().getCount() * rowHeight;
            int ph = display.heightPixels - paddingHeight - shadowHeight - titleBarHeight - listHeight;
            if (ph > paddingHeight) { ph = paddingHeight; }
            getListView().setPadding(0, 0, 0, ph);
        }
    }
    /**
     * Check the display height against the list height
     * @return whether the list height is greater than or equal to the display height
     */
    private boolean isScrollable() {
        int displayHeight;
        if (isPortrait()) {
            displayHeight = display.heightPixels - paddingHeight - shadowHeight - titleBarHeight;
        } else {
            displayHeight = display.widthPixels - paddingHeight - shadowHeight - titleBarHeight;
        }
        int listHeight = getListAdapter().getCount() * rowHeight;
        return listHeight >= displayHeight;
    }
    private boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
