/**
 * 
 */
package com.simplenote.android.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simplenote.android.Constants;
import com.simplenote.android.R;
import com.simplenote.android.model.Note;

/**
 * Specialized ListAdapter for converting Note objects into views based on the notes_row layout
 * @author bryanjswift
 */
public class NotesAdapter extends BaseAdapter {
	private static final String LOGGING_TAG = Constants.TAG + "NotesAdapter";
	// Immutable Adapter state
	private final LayoutInflater inflater;
	// Mutable Adapter state
	private Note[] notes;
	/**
	 * Default constructor for converting Note objects to ListView rows
	 * @param context where the notes are being viewed
	 * @param notes to use as data
	 */
	public NotesAdapter(Context context, Note[] notes) {
		this.inflater = LayoutInflater.from(context);
		this.notes = notes;
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
	public Object getItem(int position) {
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
		final LinearLayout row;
		final Note note = notes[position];
		Log.d(LOGGING_TAG, String.format("Getting view for '%d' with title '%s'",note.getId(), note.getTitle()));
		if (convertView == null) {
			row = (LinearLayout) inflater.inflate(R.layout.notes_row, parent, false);
		} else {
			row = (LinearLayout) convertView;
		}
		final String modified;
		if (note.getModified() == null) {
			modified = note.getDateModified();
		} else {
			modified = Constants.displayDateFormat.format(note.getModified());
		}
		((TextView) row.findViewById(R.id.text_title)).setText(note.getTitle());
		((TextView) row.findViewById(R.id.text_date)).setText(modified);
		return row;
	}
	/**
	 * @return the notes
	 */
	public final Note[] getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public final void setNotes(Note[] notes) {
		this.notes = notes;
	}
}
