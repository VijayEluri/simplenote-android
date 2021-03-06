package com.bryanjswift.swiftnote.preference;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.persistence.SwiftNoteDao;
import com.bryanjswift.swiftnote.ui.FireIntent;

/**
 * Custom preference to clear all account information
 * @author bryanjswift
 */
public class ClearAccountPreference extends DialogPreference {
    private final Context context;
    private final SwiftNoteDao dao;
    /**
     * Create a ClearAccountPreference instance
     * @param context for the preference widget
     * @param attrs attributes for the preference widget
     */
    public ClearAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.dao = new SwiftNoteDao(context);
    }
    /**
     * @see android.preference.DialogPreference#onClick()
     */
    @Override
    protected void onClick() {
        super.onClick();
    }
    /**
     * @see android.preference.DialogPreference#onDialogClosed(boolean)
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            final Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.remove(Preferences.EMAIL);
            editor.remove(Preferences.PASSWORD);
            editor.remove(Preferences.TOKEN);
            editor.commit();
            // TODO: If this should work without a SimpleNote account then don't kill all
            dao.killAll();
            FireIntent.Splash(context);
        }
    }

}
