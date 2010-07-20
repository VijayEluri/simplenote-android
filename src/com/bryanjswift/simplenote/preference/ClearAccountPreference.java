package com.bryanjswift.simplenote.preference;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.ui.FireIntent;

/**
 * Custom preference to clear all account information
 * @author bryanjswift
 */
public class ClearAccountPreference extends DialogPreference {
	private final Context context;
	private final SimpleNoteDao dao;
	/**
	 * Create a ClearAccountPreference instance
	 * @param context
	 * @param attrs
	 */
	public ClearAccountPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.dao = new SimpleNoteDao(context);
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
