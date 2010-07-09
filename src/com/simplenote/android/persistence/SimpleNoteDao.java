package com.simplenote.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.simplenote.android.Constants;
import com.simplenote.android.model.Note;

/**
 * Handle database access
 * @author bryanjswift
 */
public class SimpleNoteDao {
	/* Sql Column names */
	public static final String KEY = "key";
	public static final String TITLE = "title";
	public static final String BODY = "body";
	public static final String MODIFY = "modify";
	public static final String DELETED = "deleted";
	public static final String NEEDS_SYNC = "needs_sync";
	private static final String[] columns = new String[] { BaseColumns._ID, KEY, TITLE, BODY, MODIFY, DELETED, NEEDS_SYNC };
	/* Database information/names */
	private static final String DATABASE_NAME = "simplenotes_data.db";
	private static final String DATABASE_TABLE = "notes";
	private static final int DATABASE_VERSION = 1;
	/* Internal constants */
	private static final String LOGGING_TAG = Constants.TAG + "DAO";
	/* Internal fields */
	private final DatabaseHelper dbHelper;
	/**
	 * Construct Dao with Activity context
	 * @param context - where the Dao was created
	 */
	public SimpleNoteDao(Context context) {
		this.dbHelper = new DatabaseHelper(context);
	}
	/**
	 * Handle the creation and upgrading of the database
	 * @author bryanjswift
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * Database creation sql statement
		 */
		private static final String DATABASE_CREATE = String.format(
				"create table %s (%s integer primary key autoincrement, " +
				"%s text not null, %s text not null, %s text not null, " +
				"%s text not null, %s boolean default 0, %s boolean default 0);",
				DATABASE_TABLE, BaseColumns._ID, KEY, TITLE, BODY, MODIFY, DELETED, NEEDS_SYNC);

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(LOGGING_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			switch (oldVersion) {
				default:
					Log.i(LOGGING_TAG, "** upgrade steps complete.");
					break;
			}
		}
	}
	/**
	 * Class to wrap a Cursor so values can be queried by column name
	 * @author bryanjswift
	 */
	private class CursorWrapper {
		private final Cursor cursor;
		CursorWrapper(Cursor cursor) {
			this.cursor = cursor;
		}
		private String getString(String column) {
			return cursor.getString(cursor.getColumnIndex(column));
		}
		private long getLong(String column) {
			return cursor.getLong(cursor.getColumnIndex(column));
		}
		private boolean getBoolean(String column) {
			return cursor.getInt(cursor.getColumnIndex(column)) == 1;
		}
	}
	/**
	 * Write a Note to the database
	 * @param note to store
	 * @return the Note if it was saved successfully, null otherwise
	 */
	public Note save(Note note) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Note result = null;
		/* Setup values */
		ContentValues values = new ContentValues();
		values.put(KEY, note.getKey());
		values.put(TITLE, note.getTitle());
		values.put(BODY, note.getBody());
		values.put(MODIFY, note.getDateModified());
		values.put(DELETED, note.getDeleted());
		values.put(NEEDS_SYNC, true);
		/* Perform query */
		try {
			db.beginTransaction();
			if (note.getId() < 0) { // If no id set then must be creating a new note
				Log.i(LOGGING_TAG, "Inserting new note into DB");
				long id = db.insert(DATABASE_TABLE, null, values);
				if (id > -1) {
					result = note.setId(id);
				}
			} else { // id exists, updating existing note
				Log.i(LOGGING_TAG, String.format("Updating note with id: %d", note.getId()));
				int rows = db.update(DATABASE_TABLE, values, BaseColumns._ID + "=" + note.getId(), null);
				if (rows == 1) {
					result = note;
				}
			}
			if (note != null) {
				db.setTransactionSuccessful();
			}
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return result;
	}
	/**
	 * Retrieve a specific Note from the database
	 * @param id of the Note to retrieved
	 * @return the Note if it exists, null otherwise
	 */
	public Note retrieve(long id) {
		Log.i(LOGGING_TAG, String.format("Retrieving note with id '%d' from DB", id));
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Note result = null;
		try {
			db.beginTransaction();
			Cursor cursor = db.query(DATABASE_TABLE, columns, BaseColumns._ID + "=" + id, null, null, null, null);
			if (cursor.moveToFirst()) { // there is something in the Cursor so create a Note
				CursorWrapper c = new CursorWrapper(cursor);
				result = new Note(c.getLong(BaseColumns._ID), c.getString(BODY), c.getString(KEY), c.getString(MODIFY), c.getBoolean(DELETED));
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return result;
	}
	/**
	 * Retrieve a specific Note from the database
	 * @param key of the Note to retrieved
	 * @return the Note if it exists, null otherwise
	 */
	public Note retrieveByKey(String key) {
		Log.i(LOGGING_TAG, String.format("Retrieving note with key '%s' from DB", key));
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Note result = null;
		try {
			db.beginTransaction();
			Cursor cursor = db.query(DATABASE_TABLE, columns, KEY + " LIKE '" + key + "'", null, null, null, null);
			if (cursor.moveToFirst()) { // there is something in the Cursor so create a Note
				CursorWrapper c = new CursorWrapper(cursor);
				result = new Note(c.getLong(BaseColumns._ID), c.getString(BODY), c.getString(KEY), c.getString(MODIFY), c.getBoolean(DELETED));
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return result;
	}
	/**
	 * Retrieve a Cursor of all Notes in database ordered by modified date, descending
	 * @return Cursor containing all notes
	 */
	public Cursor retrieveAll() {
		Log.i(LOGGING_TAG, "Getting all notes from DB");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = null;
		try {
			db.beginTransaction();
			result = db.query(DATABASE_TABLE, columns, DELETED + " = 0", null, null, null, MODIFY + " DESC");
			db.setTransactionSuccessful();
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return result;
	}
	/**
	 * Mark a Note for deletion in the database
	 * @param note to mark for deletion
	 * @return whether or not the Note was successfully updated
	 */
	public boolean delete(Note note) {
		Log.i(LOGGING_TAG, String.format("Marking %s for deletion", note.getKey()));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean success = false;
		/* Setup values */
		ContentValues args = new ContentValues();
		args.put(DELETED, true);
		/* Perform query */
		try {
			db.beginTransaction();
			int rows = db.update(DATABASE_TABLE, args, BaseColumns._ID + "=" + note.getId(), null);
			success = rows == 1;
			if (success) { db.setTransactionSuccessful(); }
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return success;
	}
	/**
	 * Remove a Note from the database
	 * @param note to remove from the database
	 * @return whether or not the Note was successfully deleted
	 */
	protected boolean kill(Note note) {
		Log.w(LOGGING_TAG, String.format("Killing %s", note.getKey()));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean success = false;
		/* Perform query */
		try {
			db.beginTransaction();
			int rows = db.delete(DATABASE_TABLE, BaseColumns._ID + "=" + note.getId(), null);
			success = rows == 1;
			if (success) { db.setTransactionSuccessful(); }
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return success;
	}
	/**
	 * Delete all Note objects from the database
	 * @return true if any notes deleted, false otherwise
	 */
	protected boolean killAll() {
		Log.w(LOGGING_TAG, String.format("Killing all notes"));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean success = false;
		/* Perform query */
		try {
			db.beginTransaction();
			int rows = db.delete(DATABASE_TABLE, null, null);
			success = rows > 0;
			if (success) { db.setTransactionSuccessful(); }
			db.endTransaction();
		} finally {
			// I think the dbs should be closed but I'm getting "Invalid statement in fillWindow()" errors
			//db.close();
		}
		return success;
	}
}
