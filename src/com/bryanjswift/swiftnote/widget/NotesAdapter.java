package com.bryanjswift.swiftnote.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.model.Note;

/**
 * Specialized ListAdapter for converting Note objects into views based on the notes_row layout
 * @author bryanjswift
 */
public class NotesAdapter extends BaseAdapter {
	private static final String LOGGING_TAG = Constants.TAG + "NotesAdapter";
	// Immutable Adapter state
    private final Context context;
	private final LayoutInflater inflater;
	private final DateFormat displayDateFormat;
	// Mutable Adapter state
	private Note[] notes;
	/**
	 * Default constructor for converting Note objects to ListView rows
	 * @param context where the notes are being viewed
	 * @param notes to use as data
	 */
	public NotesAdapter(Context context, Note[] notes) {
        this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.notes = notes;
		this.displayDateFormat = new SimpleDateFormat(context.getString(R.string.display_date_format));
	}
	/**
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return notes.length;
	}
	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Note getItem(int position) {
		return notes[position];
	}
	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return notes[position].getId();
	}
	/**
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		final RelativeLayout row;
		final Note note = notes[position];
		Log.d(LOGGING_TAG, String.format("Getting view for '%d' with title '%s'",note.getId(), note.getTitle()));
		if (convertView == null) {
			row = (RelativeLayout) inflater.inflate(R.layout.notes_row, parent, false);
		} else {
			row = (RelativeLayout) convertView;
		}
		final String modified;
		if (note.getModified() == null) {
			modified = note.getDateModified();
		} else {
			modified = displayDateFormat.format(note.getModified());
		}
		((TextView) row.findViewById(R.id.note_title)).setText(ellipsizeTitle(context, note.getTitle()));
		((TextView) row.findViewById(R.id.note_date)).setText(modified);
		return row;
	}
	/**
	 * @param notes the notes to set
	 */
	public final void setNotes(Note[] notes) {
		this.notes = notes;
	}

    /**
     * @return Notes stored for this ListAdapter
     */
    public final Note[] getNotes() {
        return this.notes;
    }
    /**
     * Trim the title of a note using resources from the given context
     * @param context from which to retrieve resources
     * @param t - title (or string) to truncate/ellipsize
     * @return truncated title if it is longer than the defined max characters
     */
    public static String ellipsizeTitle(final Context context, String t) {
        String title = t;
        int maxChars = context.getResources().getInteger(R.integer.titleMaxChars);
        if (title.length() > maxChars) {
            title = title.substring(0, maxChars - 1) + "\u2026";
        }
        return title;
    }
}
