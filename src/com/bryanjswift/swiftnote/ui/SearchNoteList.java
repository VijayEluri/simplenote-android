package com.bryanjswift.swiftnote.ui;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.model.Note;
import com.bryanjswift.swiftnote.persistence.SimpleNoteDao;
import com.bryanjswift.swiftnote.widget.NotesAdapter;

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
        final Object data = getLastNonConfigurationInstance();
        if (Intent.ACTION_SEARCH.equals(searchIntent.getAction())) {
            String query = searchIntent.getStringExtra(SearchManager.QUERY);
            mQuery = query;
            ((TextView) findViewById(R.id.no_results)).setText(String.format(getString(R.string.no_results), query));
            final Note[] results;
            if (data == null) {
                results = search(query);
            } else {
                results = (Note[]) data;
            }
            setListAdapter(new NotesAdapter(this, results));
            updateShadow();
            setTitle(String.format(getString(R.string.results_title), query));
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
    protected void refreshNotes() {
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
