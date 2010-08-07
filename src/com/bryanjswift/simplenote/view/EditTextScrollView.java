package com.bryanjswift.simplenote.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;

import com.bryanjswift.simplenote.Constants;

/**
 * @author bryanjswift
 */
public class EditTextScrollView extends ScrollView {
    private static final String LOGGING_TAG = Constants.TAG + EditTextScrollView.class.getSimpleName();
    public EditTextScrollView(Context context) {
        super(context);
    }
    public EditTextScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EditTextScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof EditText)) {
            throw new IllegalStateException("EditTextScrollView can host only EditText children");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof EditText)) {
            throw new IllegalStateException("EditTextScrollView can host only EditText children");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof EditText)) {
            throw new IllegalStateException("EditTextScrollView can host only EditText children");
        }
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (!(child instanceof EditText)) {
            throw new IllegalStateException("EditTextScrollView can host only EditText children");
        }
        super.addView(child, params);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(LOGGING_TAG, "intercepting touch event");
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            EditText child = getChildAt(0);
            child.dispatchTouchEvent(ev);
            child.invalidate();
            child.setSelection(150);
            invalidate();
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public EditText getChildAt(int index) {
        return (EditText) super.getChildAt(index);
    }
}
