package com.bryanjswift.simplenote.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.BaseColumns;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;

/**
 * Collection of methods to fire Intents
 * @author bryanjswift
 */
public class FireIntent {
	/**
	 * Start the SimpleNoteList Activity
	 * @param context in which the intent is firing
	 */
	public static void SimpleNoteList(final Context context) {
		Intent i = new Intent(context, SimpleNoteList.class);
		context.startActivity(i);
	}
	/**
	 * Starts the SimpleNoteEdit Activity
	 * @param context in which the intent is firing
	 * @param id of the note to edit
	 * @param originalBody of the note as retrieved from saved state
	 */
	public static void EditNote(final Activity context, final long id, final String originalBody) {
		Intent intent = new Intent(context, SimpleNoteEdit.class);
		intent.putExtra(BaseColumns._ID, id);
		if (originalBody != null) {
			intent.putExtra(SimpleNoteDao.BODY, originalBody);
		}
		context.startActivityForResult(intent, Constants.REQUEST_EDIT);
	}
	/**
	 * Start the LoginDialog Activity expecting a result
	 * @param context Activity for which the intent is firing
	 */
	public static void SigninDialog(final Activity context) {
		Intent i = new Intent(context, LoginDialog.class);
		context.startActivityForResult(i, Constants.REQUEST_LOGIN);
	}
	/**
	 * Start the Preferences Activity
	 * @param context Activity for which the intent is firing
	 */
	public static void Preferences(final Activity context) {
		Intent settings = new Intent(context, Preferences.class);
		context.startActivity(settings);
	}
}
