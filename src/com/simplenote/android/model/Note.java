package com.simplenote.android.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a note from the SimpleNote servers
 * @author bryanjswift
 */
public class Note {
	private final String title;
	private final String body;
	private final String key;
	private final String dateModified;
	private final boolean deleted;
	private int newlines = 0;

	/**
	 * Private constructor taking all fields
	 * @param title of the note
	 * @param body of the note
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 * @param newlines - number of new lines separating title and body
	 */
	private Note(final String title, final String body, final String key, final String dateModified,
			final boolean deleted, final int newlines) {
		this.title = title;
		this.body = body;
		this.key = key;
		this.dateModified = dateModified;
		this.deleted = deleted;
		this.newlines = newlines;
	}
	/**
	 * Create a Note from all four pieces of information
	 *
	 * Defaults to two new lines between title and body text
	 * @param title of the note (really just the first line of body)
	 * @param body of the note
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 */
	public Note(final String title, final String body, final String key, final String dateModified) {
		this(title,body,key,dateModified,false,2);
	}
	/**
	 * Create a Note from body, key and dateModified
	 *
	 * Must parse the title from the body
	 * @param titleAndBody - title of the note followed by the body of the note, separated by one or more new lines
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 */
	public Note(final String titleAndBody, final String key, final String dateModified) {
		this(titleAndBody,key,dateModified,false);
	}
	/**
	 * Private Constructor to create a Note from body, key, dateModified, and deleted
	 *
	 * Must parse the title from the body
	 * @param titleAndBody - title of the note followed by the body of the note, separated by one or more new lines
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 * @param deleted - whether or not the note is marked for deletion on the server
	 */
	private Note(String titleAndBody, final String key, final String dateModified, final boolean deleted) {
		int idxNewline = titleAndBody.indexOf("\n");
		if (idxNewline != -1) {
			this.title = titleAndBody.substring(0,idxNewline);
			titleAndBody = titleAndBody.substring(idxNewline);
			idxNewline = titleAndBody.indexOf("\n");
			while (idxNewline == 0) {
				titleAndBody = titleAndBody.substring(1);
				idxNewline = titleAndBody.indexOf("\n");
				newlines++;
			}
			this.body = titleAndBody;
		} else {
			this.title = titleAndBody;
			this.body = "";
		}
		this.key = key;
		this.dateModified = dateModified;
		this.deleted = deleted;
	}
	/**
	 * Create a placeholder index note
	 * @param key from SimpleNote servers for this note
	 * @param dateModified - the last modification was made on this date (server or local)
	 * @param deleted - whether or not the note is marked for deletion on the server
	 */
	public Note(final String key, final String dateModified, final boolean deleted) {
		this(null,null,key,dateModified,deleted,0);
	}
	/**
	 * Create a Note from a JSONObject
	 * @param object representing a bare bones note
	 * @throws JSONException if a mapping doesn't exist or a coercion can't be made
	 */
	public Note(final JSONObject object) throws JSONException {
		this("","",object.getString("key"),object.getString("modify"),object.getBoolean("deleted"),0);
	}
	/**
	 * @return title
	 */
	public final String getTitle() {
		return title;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param title of the new Note object
	 * @return a new Note with title updated
	 */
	public final Note setTitle(final String title) {
		return new Note(title, this.body, this.key, this.dateModified, this.deleted, this.newlines);
	}
	/**
	 * @return body
	 */
	public final String getBody() {
		return body;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param body of the new Note object
	 * @return a new Note with body updated
	 */
	public final Note setBody(final String body) {
		return new Note(this.title, body, this.key, this.dateModified, this.deleted, this.newlines);
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
		return new Note(this.title, this.body, key, this.dateModified, this.deleted, this.newlines);
	}
	/**
	 * @return dateModified
	 */
	public final String getDateModified() {
		return dateModified;
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param dateModified of the new Note object
	 * @return a new Note with dateModified updated
	 */
	public final Note setDateModified(final String dateModified) {
		return new Note(this.title, this.body, this.key, dateModified, this.deleted, this.newlines);
	}
	/**
	 * Invokes private constructor to create a new note
	 * @param deleted of the new Note object
	 * @return a new Note with deleted updated
	 */
	public final Note setDeleted(final boolean deleted) {
		return new Note(this.title, this.body, this.key, this.dateModified, deleted, this.newlines);
	}
	/**
	 * Combind title and body fields with the correct number of newlines
	 * @return combined title and body fields
	 */
	public final String getTitleAndBody() {
		StringBuilder builder = new StringBuilder(title);
		for (int i = 0; i < newlines; i++) {
			builder.append("\n");
		}
		builder.append(body);
		return builder.toString();
	}
}
