package com.simplenote.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.simplenote.android.Constants;

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
	 * Start the LoginDialog Activity expecting a result
	 * @param context Activity for which the intent is firing
	 */
	public static void SigninDialog(final Activity context) {
		Intent i = new Intent(context, LoginDialog.class);
		context.startActivityForResult(i, Constants.REQUEST_LOGIN);
	}
}
