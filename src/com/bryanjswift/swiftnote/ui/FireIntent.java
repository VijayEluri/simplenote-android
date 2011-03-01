package com.bryanjswift.swiftnote.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.BaseColumns;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.persistence.SwiftNoteDao;

/**
 * Collection of methods to fire Intents
 * @author bryanjswift
 */
public class FireIntent {
    /**
     * Start the SwiftNoteList Activity
     * @param context in which the intent is firing
     */
    public static void SimpleNoteList(final Context context) {
        Intent i = new Intent(context, SwiftNoteList.class);
        context.startActivity(i);
    }
    /**
     * Starts the SwiftNoteEdit Activity
     * @param context in which the intent is firing
     * @param id of the note to edit
     * @param originalBody of the note as retrieved from saved state
     */
    public static void EditNote(final Activity context, final long id, final String originalBody) {
        Intent intent = new Intent(context, SwiftNoteEdit.class);
        intent.putExtra(BaseColumns._ID, id);
        if (originalBody != null) {
            intent.putExtra(SwiftNoteDao.BODY, originalBody);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        context.startActivityForResult(intent, Constants.REQUEST_EDIT);
    }
    /**
     * Start the Preferences Activity
     * @param context Activity for which the intent is firing
     */
    public static void Preferences(final Activity context) {
        Intent settings = new Intent(context, Preferences.class);
        context.startActivity(settings);
    }
    /**
     * Start the Splash Activity clearing the Activity stack
     * @param context Context for which the intent is firing
     */
    public static void Splash(final Context context) {
        final Intent splash = (new Intent(context, SwiftNoteSplash.class));
        context.startActivity(splash);
    }
    /**
     * Finish an activity if the credentials retrieved for Activity don't cut it
     * @param activity to test and kill (if necessary)
     */
    public static void finishIfUnauthorized(Activity activity) {
        final Api.Credentials credentials = Preferences.getLoginPreferences(activity);
        if (!credentials.hasAuth() && !credentials.hasCreds()) {
            activity.finish();
        }
    }
}
