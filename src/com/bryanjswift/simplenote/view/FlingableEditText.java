package com.bryanjswift.simplenote.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Scroller;

import com.bryanjswift.simplenote.Constants;

/**
 * @author bryanjswift
 */
public class FlingableEditText extends EditText {
    private static final String LOGGING_TAG = Constants.TAG + FlingableEditText.class.getSimpleName();
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    /**
     * Handles scrolling
     */
    private final Scroller scroller;
    private final int minimumVelocity;
    private final int maximumVelocity;
    private boolean mIsBeingDragged = false;
    private float mLastMotionY;
    public FlingableEditText(Context context) {
        super(context);
        scroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        Log.d(LOGGING_TAG, "maximum velocity: " + maximumVelocity + " ;; minimum velocity: " + minimumVelocity);
    }
    public FlingableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        Log.d(LOGGING_TAG, "maximum velocity: " + maximumVelocity + " ;; minimum velocity: " + minimumVelocity);
    }
    public FlingableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        Log.d(LOGGING_TAG, "maximum velocity: " + maximumVelocity + " ;; minimum velocity: " + minimumVelocity);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction();
        final float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                // Remember where the motion event started
                mLastMotionY = y;
                mIsBeingDragged = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    final int deltaY = (int) (mLastMotionY - y);
                    Log.d(LOGGING_TAG, "ScrollY: " + getScrollY() + " ;; DeltaY: " + deltaY);
                    mLastMotionY = y;
                    scroller.startScroll(getScrollX(), getScrollY(), 0, deltaY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity();
                    Log.d(LOGGING_TAG, "computed velocity: " + initialVelocity);
                    if (Math.abs(initialVelocity) > minimumVelocity) {
                        fling(-initialVelocity);
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityY) {
        int height = getHeight() - getPaddingBottom() - getPaddingTop();
        int bottom = getHeight();
        scroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0, Math.max(0, bottom - height));
        invalidate();
    }
}
