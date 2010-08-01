package com.bryanjswift.simplenote.view;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.bryanjswift.simplenote.Constants;

/**
 * Specialized focus change listener which keeps track of the View's initial value
 * and alters value on focus change based on the current value and the initial value
 * @author bryanjswift
 */
public class TextAsLabelFocusChangeListener implements OnFocusChangeListener {
	private static final String LOGGING_TAG = Constants.TAG + "TextAsLabelFocusChangeListener";
	protected final EditText field;
	protected final String initial;
	public TextAsLabelFocusChangeListener(EditText field, String initial) {
		Log.d(LOGGING_TAG, "Default value: " + initial);
		this.field = field;
		this.initial = initial;
	}
	/**
	 * When focus is gained, if the value is the default string make it empty
	 * When focus is lost, if the value is empty make it the default
	 * @param field - the whose focus is changing
	 * @param hasFocus or not the field now has focus
	 */
	public void onFocusChange(View field, boolean hasFocus) {
		if (field == null || !field.equals(this.field)) { return; }
		if (hasFocus) { onFocus(); }
		else { onBlur(); }
	}
	/**
	 * Called when focus is gained
	 */
	protected void onFocus() {
		Editable value = field.getText();
		if (value.toString().equals(initial)) {
			// does replace return a copy or just a reference to the same object?
			value.clear();
			field.setText(value, TextView.BufferType.EDITABLE);
		}
	}
	/**
	 * Called when focus is lost
	 */
	protected void onBlur() {
		Editable value = field.getText();
		if (value.toString().equals("")) {
			// does append return a copy or just a reference to the same object?
			field.setText(value.append(initial), TextView.BufferType.EDITABLE);
		}
	}
}
