package com.bryanjswift.simplenote.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.model.Note;

/**
 * Handle database access
 * @author bryanjswift
 */
public class SimpleNoteDao {
    /* Sql Column names */
    public static final String KEY = "key";
    public static final String KEY_HASH = "key_hash";
    public static final String BODY = "body";
    public static final String MODIFY = "modify";
    public static final String DELETED = "deleted";
    public static final String SYNCED = "synced";
    private static final String[] columns = new String[] { BaseColumns._ID, KEY, BODY, MODIFY, DELETED, SYNCED };
    /* Database information/names */
    private static final String DATABASE_NAME = "simplenotes_data.db";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 4;
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
        private static final String DATABASE_CREATE = (String.format(
                "create table %s (%s integer primary key autoincrement, " +
                "%s text not null, %s integer default 0, %s text not null, %s text not null, " +
                "%s boolean default 0, %s boolean default 1);",
                DATABASE_TABLE, BaseColumns._ID, KEY, KEY_HASH, BODY, MODIFY, DELETED, SYNCED));
        private static final String KEY_INDEX_CREATE = (String.format(
                "create index if not exists key_index on %s (%s);", DATABASE_TABLE, KEY));
        private static final String KEY_HASH_INDEX_CREATE = (String.format(
                "create index if not exists key_hash_index on %s (%s);", DATABASE_TABLE, KEY_HASH));

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(KEY_INDEX_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOGGING_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            switch (oldVersion) {
                case 1:
                    final String tmpTable = "tmp_notes";
                    final String oldCols = BaseColumns._ID + "," + KEY + "," + BODY + "," + MODIFY + "," + DELETED + ", NOT needs_sync";
                    final String cols = BaseColumns._ID + "," + KEY + "," + BODY + "," + MODIFY + "," + DELETED + "," + SYNCED;
                    db.execSQL(DATABASE_CREATE.replace("table " + DATABASE_TABLE, "table " + tmpTable));
                    db.execSQL("INSERT INTO " + tmpTable + " (" + cols + ") SELECT " + oldCols + " FROM " + DATABASE_TABLE + ";");
                    db.execSQL("DROP TABLE " + DATABASE_TABLE);
                    db.execSQL("ALTER TABLE tmp_notes RENAME TO " + DATABASE_TABLE);
                    db.execSQL(KEY_INDEX_CREATE);
                    break;
                case 2:
                    db.execSQL(KEY_INDEX_CREATE);
                    break;
                case 3:
                    db.execSQL("ALTER TABLE notes ADD COLUMN " + KEY_HASH + " integer default 0;");
                    db.execSQL(KEY_HASH_INDEX_CREATE);
                    Cursor cursor = db.query(DATABASE_TABLE, new String[] { BaseColumns._ID, KEY }, null, null, null, null, null);
                    CursorWrapper notes = new CursorWrapper(cursor);
                    while (cursor.moveToNext()) {
                        long id = notes.getLong(BaseColumns._ID);
                        String key = notes.getString(KEY);
                        ContentValues values = new ContentValues();
                        values.put(KEY_HASH, key.hashCode());
                        db.update(DATABASE_TABLE, values, BaseColumns._ID + " = " + id, null);
                    }
                    cursor.close();
                    break;
                default:
                    Log.i(LOGGING_TAG, "No upgrade necessary.");
                    break;
            }
        }
    }
    /**
     * Class to wrap a Cursor so values can be queried by column name
     * @author bryanjswift
     */
    private static class CursorWrapper {
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
        /* Setup values */
        ContentValues values = new ContentValues();
        values.put(KEY, note.getKey());
        values.put(KEY_HASH, note.getKey().hashCode());
        values.put(BODY, note.getBody());
        values.put(MODIFY, note.getDateModified());
        values.put(DELETED, note.isDeleted());
        values.put(SYNCED, note.isSynced());
        /* Perform query */
        return writeNote(values, note);
    }
    /**
     * Retrieve a specific Note from the database
     * @param id of the Note to retrieved
     * @return the Note if it exists, null otherwise
     */
    public Note retrieve(long id) {
        Log.i(LOGGING_TAG, String.format("Retrieving note with id '%d' from DB", id));
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Note result = null;
        Cursor cursor = null;
        try {
            db.beginTransaction();
            cursor = db.query(DATABASE_TABLE, columns, BaseColumns._ID + "=" + id, null, null, null, null);
            if (cursor.moveToFirst()) { // there is something in the Cursor so create a Note
                CursorWrapper c = new CursorWrapper(cursor);
                result = new Note(c.getLong(BaseColumns._ID), c.getString(BODY), c.getString(KEY), c.getString(MODIFY), c.getBoolean(DELETED));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
            if (cursor != null) { cursor.close(); }
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
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Note result = null;
        Cursor cursor = null;
        try {
            db.beginTransaction();
            cursor = db.query(DATABASE_TABLE, columns, KEY_HASH + " = " + key.hashCode(), null, null, null, null);
            CursorWrapper c = new CursorWrapper(cursor);
            while (cursor.moveToNext()) { // there is something in the Cursor so create a Note
                if (c.getString(KEY).equals(key)) {
                    result = new Note(c.getLong(BaseColumns._ID), c.getString(BODY), c.getString(KEY), c.getString(MODIFY), c.getBoolean(DELETED));
                    cursor.moveToLast();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
            if (cursor != null) { cursor.close(); }
        }
        return result;
    }
    /**
     * Retrieve a Note[] of all Notes in database ordered by modified date, descending
     * @return Note[] containing all undeleted notes
     */
    public Note[] retrieveAll() {
        Log.i(LOGGING_TAG, "Getting all notes from DB");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Note[] notes = new Note[0];
        try {
            db.beginTransaction();
            cursor = db.query(DATABASE_TABLE, columns, DELETED + " = 0", null, null, null, MODIFY + " DESC");
            if (cursor != null) { db.setTransactionSuccessful(); }
            db.endTransaction();
            notes = cursorToNotes(cursor);
        } finally {
            if (cursor != null) { cursor.close(); }
            db.close();
        }
        return notes;
    }
    /**
     * Retrieve all notes that need to be synchronized with SimpleNote's servers
     * @return Note[] of all unsynchronized notes
     */
    public Note[] retrieveUnsynced() {
        Log.i(LOGGING_TAG, "Getting all unsynchronized notes from DB");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Note[] notes = new Note[0];
        try {
            db.beginTransaction();
            cursor = db.query(DATABASE_TABLE, columns, // table, columns to select
                    SYNCED + " = 0", // where clause
                    null, null, null, MODIFY + " DESC"); // __, __, __, order by
            if (cursor != null) { db.setTransactionSuccessful(); }
            db.endTransaction();
            notes = cursorToNotes(cursor);
        } finally {
            if (cursor != null) { cursor.close(); }
            db.close();
        }
        return notes;
    }
    /**
     * Mark a Note for deletion in the database
     * @param note to mark for deletion
     * @return whether or not the Note was successfully updated
     */
    public boolean delete(Note note) {
        Log.i(LOGGING_TAG, String.format("Marking %s for deletion", note.getKey()));
        /* Setup values */
        ContentValues values = new ContentValues();
        values.put(DELETED, true);
        values.put(SYNCED, false);
        /* Perform query */
        return writeNote(values, note) != null;
    }
    /**
     * Mark a Note as up to date with the server in the database
     * @param note to mark synchronized
     * @return whether or not the note was successfully updated
     */
    public boolean markSynced(Note note) {
        Log.i(LOGGING_TAG, String.format("Marking %s synchronized", note.getKey()));
        final ContentValues values = new ContentValues();
        values.put(SYNCED, true);
        return writeNote(values, note) != null;
    }
    /**
     * Remove a Note from the database
     * @param note to remove from the database
     * @return whether or not the Note was successfully deleted
     */
    synchronized public boolean kill(Note note) {
        Log.w(LOGGING_TAG, String.format("Killing %s", note.getKey()));
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        /* Perform query */
        try {
            db.beginTransaction();
            int rows = db.delete(DATABASE_TABLE, BaseColumns._ID + "=" + note.getId(), null);
            success = rows == 1;
            if (success) { db.setTransactionSuccessful(); }
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }
    /**
     * Delete all Note objects from the database
     * @return true if any notes deleted, false otherwise
     */
    synchronized public boolean killAll() {
        Log.w(LOGGING_TAG, String.format("Killing all notes"));
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        /* Perform query */
        try {
            db.beginTransaction();
            int rows = db.delete(DATABASE_TABLE, null, null);
            success = rows > 0;
            if (success) { db.setTransactionSuccessful(); }
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }
    synchronized private Note writeNote(ContentValues values, Note note) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Note result = null;
        try {
            db.beginTransaction();
            if (note.getId() != Constants.DEFAULT_ID) {
                Log.i(LOGGING_TAG, String.format("Updating note with id: %d", note.getId()));
                int rows = db.update(DATABASE_TABLE, values, BaseColumns._ID + " = " + note.getId(), null);
                if (rows == 1) {
                    result = note;
                    db.setTransactionSuccessful();
                }
            } else {
                Log.i(LOGGING_TAG, "Inserting new note into DB");
                long id = db.insert(DATABASE_TABLE, null, values);
                if (id != Constants.DEFAULT_ID) {
                    result = note.setId(id);
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }
    /**
     * Turn a cursor into an array of Note objects
     * @param cursor to read data from
     * @return an array of Notes with data from provided Cursor
     */
    private static Note[] cursorToNotes(Cursor cursor) {
        final CursorWrapper c = new CursorWrapper(cursor);
        final Note[] notes = new Note[cursor.getCount()];
        while (cursor != null && cursor.moveToNext()) {
            notes[cursor.getPosition()] = new Note(c.getLong(BaseColumns._ID), c.getString(BODY), c.getString(KEY), c.getString(MODIFY), c.getBoolean(DELETED));
        }
        return notes;
    }
}
