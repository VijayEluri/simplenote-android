package com.bryanjswift.simplenote.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.ScrollView;

import com.bryanjswift.simplenote.Constants;

/**
 * Custom EditText to allow the ScrollView to handle displaying the proper rectangle by killing requests for
 * different rectangles
 * @author bryanjswift
 */
public class ScrollWrappableEditText extends EditText {
    private static final String LOGGING_TAG = Constants.TAG + ScrollWrappableEditText.class.getSimpleName();
    /**
     * Default constructor
     * @param context to which this View is being added
     */
    public ScrollWrappableEditText(Context context) {
        super(context);
    }
    /**
     * Default constructor
     * @param context to which this View is being added
     * @param attrs used when creating the View
     */
    public ScrollWrappableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /**
     * Default constructor
     * @param context to which this View is being added
     * @param attrs used when creating the View
     * @param defStyle id of style definition to add to attrs
     */
    public ScrollWrappableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    /**
     * Don't request this view move if it is wrapped in a ScrollView
     * @see android.view.View#requestRectangleOnScreen(android.graphics.Rect, boolean)
     */
    @Override
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        // Always return true, the ScrollView around this will handle the proper rectangle being on screen
        if (getParent() instanceof ScrollView) {
            Log.d(LOGGING_TAG, "Wrapped in ScrollView tell call request is complete");
            ((ScrollView) getParent()).scrollBy(rectangle.left - getLeft(), rectangle.top - getTop());
            return true;
        } else {
            Log.d(LOGGING_TAG, "Not wrapped by ScrollView, handle normally");
            return super.requestRectangleOnScreen(rectangle, immediate);
        }
    }
}
