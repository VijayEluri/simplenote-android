package com.simplenote.android.model;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.simplenote.android.Constants;

/**
 * Represents a note from the SimpleNote servers
 * @author bryanjswift
 */
public class Note implements Serializable{
	/** Generated serialVersionUID */
	private static final long serialVersionUID = -6783055981397528617L;
	// Private attributes
	private final long id;
	private final String titleAndBody;
	private final String key;
	private final String dateModified;
	private final boolean deleted;
	private final boolean synced;
	// Memoizable fields
	private String title;
	private Date modified;

	/**
	 * Constructor to create a Note from id, body, key, dateModified, deleted and needsSync
	 *
	 * Must parse the title from the body
	 * @param id of the note
	 * @param titleAndBody - title of the note followed by the body of the note, separated by one or more new lines
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 * @param deleted - whether or not the note is marked for deletion on the server
	 * @param synced - whether or not the not is in sync with the server
	 */
	public Note(final long id, String titleAndBody, final String key, final String dateModified, final boolean deleted, final boolean synced) {
		this.id = id;
		this.titleAndBody = titleAndBody;
		this.key = key;
		this.dateModified = dateModified;
		this.deleted = deleted;
		this.synced = synced;
	}
	/**
	 * Constructor to create a Note from id, body, key, dateModified and deleted
	 * @param id of the note
	 * @param titleAndBody - title of the note followed by the body of the note, separated by one or more new lines
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 * @param deleted - whether or not the note is marked for deletion on the server
	 */
	public Note(final long id, String titleAndBody, final String key, final String dateModified, final boolean deleted) {
		this(id, titleAndBody, key, dateModified, deleted, false);
	}
	/**
	 * Constructor for making a new Note without an existing id or key
	 * @param titleAndBody - title of the note followed by the body of the note, separated by one or more new lines
	 * @param dateModified - the last modification was made on this date (server or local)
	 */
	public Note(final String titleAndBody, final String dateModified) {
		this(Constants.DEFAULT_ID, titleAndBody, Constants.DEFAULT_KEY, dateModified, false);
	}
	/**
	 * Create a Note from body, key and dateModified
	 *
	 * Must parse the title from the body
	 * @param id of the note
	 * @param titleAndBody - title of the note followed by the body of the note, separated by one or more new lines
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 */
	public Note(final long id, final String titleAndBody, final String key, final String dateModified) {
		this(id, titleAndBody, key, dateModified, false);
	}
	/**
	 * Create a placeholder index note
	 * @param id of the note
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 * @param deleted - whether or not the note is marked for deletion on the server
	 */
	public Note(final long id, final String key, final String dateModified, final boolean deleted) {
		this(id, null, key, dateModified, deleted);
	}
	/**
	 * Create a Note from a JSONObject
	 * JSONObjects will not have body fields/values
	 * @param object representing a bare bones note
	 * @throws JSONException if a mapping doesn't exist or a coercion can't be made
	 */
	public Note(final JSONObject object) throws JSONException {
		this(Constants.DEFAULT_ID, "", object.getString("key"), object.getString("modify"), object.getBoolean("deleted"));
	}
	/**
	 * @return id
	 */
	public final long getId() {
		return id;
	}
	/**
	 * Invokes private constructor to create a new Note
	 * @param id of the new Note object
	 * @return a new Note with id updated
	 */
	public final Note setId(final long id) {
		return new Note(id, this.titleAndBody, this.key, this.dateModified, this.deleted);
	}
	/**
	 * @return title
	 */
	public final String getTitle() {
		if (title == null) {
			final int idxNewline = titleAndBody.indexOf("\n");
			if (idxNewline != -1) {
				title = titleAndBody.substring(0,idxNewline);
			} else {
				title = titleAndBody;
			}
		}
		return title;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param title of the new Note object
	 * @return a new Note with title updated
	 */
	public final Note setTitle(final String title) {
		final int idxNewline = this.titleAndBody.indexOf("\n");
		final String titleAndBody;
		if (idxNewline > 0) {
			titleAndBody = title + this.titleAndBody.substring(idxNewline);
		} else {
			titleAndBody = title + this.titleAndBody;
		}
		return new Note(this.id, titleAndBody, this.key, this.dateModified, this.deleted);
	}
	/**
	 * @return body
	 */
	public final String getBody() {
		return titleAndBody;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param body of the new Note object
	 * @return a new Note with body updated
	 */
	public final Note setBody(final String body) {
		return new Note(this.id, body, this.key, this.dateModified, this.deleted);
	}
	/**
	 * @return key
	 */
	public final String getKey() {
		return key;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param key of the new Note object
	 * @return a new Note with key updated
	 */
	public final Note setKey(final String key) {
		return new Note(this.id, this.titleAndBody, key, this.dateModified, this.deleted);
	}
	/**
	 * @return dateModified
	 */
	public final String getDateModified() {
		return dateModified;
	}
	/**
	 * Get a Date object from the dateModified String
	 * @return Date parsed from dateModified
	 */
	public final Date getModified() {
		if (modified == null) {
			try {
				modified = Constants.serverDateFormat.parse(dateModified);
			} catch (ParseException e) {
				modified = null;
			}
		}
		return modified;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param dateModified of the new Note object
	 * @return a new Note with dateModified updated
	 */
	public final Note setDateModified(final String dateModified) {
		return new Note(this.id, this.titleAndBody, this.key, dateModified, this.deleted);
	}
	/**
	 * @return deleted
	 */
	public final boolean isDeleted() {
		return deleted;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param deleted of the new Note object
	 * @return a new Note with deleted updated
	 */
	public final Note setDeleted(final boolean deleted) {
		return new Note(this.id, this.titleAndBody, this.key, this.dateModified, deleted);
	}
	/**
	 * Combine title and body fields
	 * @return combined title and body fields
	 */
	public final String getTitleAndBody() {
		return getBody();
	}
	/**
	 * Create a new Note with updated title and body fields
	 * @param titleAndBody combined title and body data to update the note with
	 * @return a new Note with title and body updated
	 */
	public final Note setTitleAndBody(String titleAndBody) {
		return new Note(this.id, titleAndBody, this.key, this.dateModified, this.deleted);
	}
	/**
	 * @return synced
	 */
	public final boolean isSynced() {
		return synced;
	}
	/**
	 * Create a new Note with an updated synced field
	 * @param synced if the new Note is synced or not
	 * @return a new Note with synced updated
	 */
	public final Note setSynced(final boolean synced) {
		return new Note(this.id, this.titleAndBody, this.key, this.dateModified, this.deleted, synced);
	}
}
