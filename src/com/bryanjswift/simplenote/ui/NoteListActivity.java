package com.bryanjswift.simplenote.ui;

import android.app.ListActivity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.widget.NotesAdapter;

/**
 * @author bryanjswift
 */
public abstract class NoteListActivity extends ListActivity {
    private static final String LOGGING_TAG = Constants.TAG + NoteListActivity.class.getSimpleName();
    protected static DisplayMetrics display = new DisplayMetrics();
    protected static int paddingHeight = -1;
    protected static int shadowHeight = -1;
    protected static int rowHeight = -1;
    /** Interface for accessing the SimpleNote database on the device */
    protected final SimpleNoteDao dao;
    public NoteListActivity() {
        this.dao = new SimpleNoteDao(this);
    }
    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (paddingHeight == -1 && shadowHeight == -1) {
            getWindowManager().getDefaultDisplay().getMetrics(display);
            paddingHeight = Math.round(getResources().getInteger(R.integer.noteListPadding) * display.density);
            // 5.33333333333 is the assumed height of the scrolling shadow at 160 dpi
            shadowHeight = Math.round(5.333333333333333333333333333f * display.density);
            rowHeight = Math.round(getResources().getInteger(R.integer.noteItemHeight) * display.density);
        }
        getWindow().setFormat(PixelFormat.RGBA_8888);
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
     * Add padding to show drop shadow below list when scrolling is disabled
     */
    protected void updateShadow() {
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
