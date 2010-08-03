package com.bryanjswift.simplenote.ui;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.widget.NotesAdapter;

/**
 * @author bryanjswift
 */
public class SearchNoteList extends NoteListActivity {
    private static final String LOGGING_TAG = Constants.TAG + SearchNoteList.class.getSimpleName();
    // Immutable fields
    private final SimpleNoteDao dao;
    // Mutable fields
    private String mQuery;
    /**
     * Default constructor to populate necessary final fields
     */
    public SearchNoteList() {
        this.dao = new SimpleNoteDao(this);
    }
    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);
        Intent searchIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(searchIntent.getAction())) {
            String query = searchIntent.getStringExtra(SearchManager.QUERY);
            mQuery = query;
            ((TextView) findViewById(R.id.no_results)).setText(String.format(getString(R.string.no_results), query));
            Note[] results = search(query);
            setListAdapter(new NotesAdapter(this, results));
            updateShadow();
        }
    }

    /**
     * Triggers an update to the list of notes
     * @param note causing the refresh
     */
    protected void updateNotesFor(final Note note) {
        if (mQuery != null) {
            refreshNotes();
        }
    }
    /**
     * Perform the search and return an array of Notes
     * @param query string to use when searching note data
     * @return Note[] containing only notes that match query
     */
    private Note[] search(String query) {
        query = query.toLowerCase();
        final Note[] all = dao.retrieveAll();
        ArrayList<Note> results = new ArrayList<Note>();
        for (Note note : all) {
            if (note.getBody().toLowerCase().indexOf(query) != -1) {
                results.add(note);
            }
        }
        return results.toArray(new Note[results.size()]);
    }
    /**
     * Pull notes from database and update the NotesAdapter
     */
    private void refreshNotes() {
        Log.d(LOGGING_TAG, "Re-running search");
        final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
        adapter.setNotes(search(mQuery));
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
                updateShadow();
            }
        });
    }
}
