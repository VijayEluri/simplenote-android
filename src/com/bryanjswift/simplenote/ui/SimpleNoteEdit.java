package com.bryanjswift.simplenote.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.view.ScrollWrappableEditText;
import com.bryanjswift.simplenote.widget.NotesAdapter;

import java.util.Date;

/**
 * Handle the note editing
 * @author bryanjswift
 */
public class SimpleNoteEdit extends Activity {
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteEdit";
	// Final variables
	private final SimpleNoteDao dao;
	// Mutable instance variables
	private long mNoteId = 0L;
	private String mOriginalBody = "";
	private boolean mActivityStateSaved = false;
	private boolean mNoteSaved = false;
    private boolean mKeyboardOpen = false;
    private final View.OnTouchListener trashTouch = new View.OnTouchListener() {
        /**
         * Handle special events when touching the trash button
         * @param view being touched
         * @param motionEvent information about touch event
         * @return whether or not the event was handled (it wasn't)
         */
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            View titleRow = findViewById(R.id.note_title_row);
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (isInsideView(view, motionEvent)) {
                        Log.d(LOGGING_TAG, "ACTION_UP MotionEvent inside view - letting onClick handle deleting note");
                    } else {
                        titleRow.setPressed(false);
                    }
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_CANCEL:
                    titleRow.setPressed(false);
                    break;
                case MotionEvent.ACTION_DOWN:
                    titleRow.setPressed(true);
                    break;
            }
            return false;
        }

        /**
         * Check the evt occurred within the bounds of view
         * @param view to check motion event coordinates against
         * @param evt to test against view bounds
         * @return true if (view.getTop() > evt.getY() < view.getBottom()) && (view.getLeft() > evt.getX() < view.getRight())
         */
        private boolean isInsideView(View view, MotionEvent evt) {
            return evt.getRawY() > view.getTop() && evt.getRawY() < view.getBottom()
                    && evt.getRawX() > view.getLeft() && evt.getRawX() < view.getRight();
        }
    };
    private final View.OnClickListener trashClick = new View.OnClickListener() {
        /**
         * Perform deletion of the note
         * @param view being clicked
         */
        @Override
        public void onClick(View view) {
            Log.d(LOGGING_TAG, "OnClick firing for trash icon");
            delete();
        }
    };
    private final View.OnTouchListener scrollTouch = new View.OnTouchListener() {
        /**
         * Try to open the keyboard
         * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
         */
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d(LOGGING_TAG, "touching the ScrollView");
            openKeyboard(findViewById(R.id.note_body));
            return false;
        }
    };
	/**
	 * Default constructor to setup final fields
	 */
	public SimpleNoteEdit() {
		this.dao = new SimpleNoteDao(this);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOGGING_TAG, "Running creating new SimpleNoteEdit Activity");
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.edit_note);
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			mNoteId = extras.getLong(BaseColumns._ID);
			mOriginalBody = extras.getString(SimpleNoteDao.BODY);
		} else {
			mNoteId = savedInstanceState.getLong(BaseColumns._ID);
		}
		final Note dbNote = dao.retrieve(mNoteId);
		if (savedInstanceState == null && mOriginalBody == null) {
			mOriginalBody = dbNote.getBody();
		} else if (savedInstanceState != null && mOriginalBody == null) {
			mOriginalBody = savedInstanceState.getString(SimpleNoteDao.BODY);
		}
		final String title;
        final ScrollWrappableEditText noteBody = ((ScrollWrappableEditText) findViewById(R.id.note_body));
        final TextView noteTitle = ((TextView) findViewById(R.id.note_title));
		if (dbNote != null) {
			title = dbNote.getTitle();
			noteBody.setText(dbNote.getBody());
		} else {
			title = getString(R.string.new_note);
		}
		noteTitle.setText(NotesAdapter.ellipsizeTitle(this, title));
        noteBody.setOnChangeListener(new ScrollWrappableEditText.OnChangeListener() {
            @Override
            public void onChange(View v, String oldText, String newText) {
                noteTitle.setText(NotesAdapter.ellipsizeTitle(SimpleNoteEdit.this, Note.extractTitle(newText)));
            }
        });
        final ImageButton trash = (ImageButton) findViewById(R.id.note_delete);
        trash.setOnClickListener(trashClick);
        trash.setOnTouchListener(trashTouch);
        findViewById(R.id.note_body_scroll).setOnTouchListener(scrollTouch);
	}

    /**
     * @see android.app.Activity#onPostResume()
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mNoteId == Constants.DEFAULT_ID && !hasHardwareKeyboard()) {
            openKeyboard(findViewById(R.id.note_body));
        }
    }
    /**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOGGING_TAG, "Resuming SimpleNoteEdit");
		mActivityStateSaved = false;
	}
	/**
	 * Called before on pause, save data here so SimpleEditNote can be relaunched
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LOGGING_TAG, "Saving instance state");
		mActivityStateSaved = true;
		// Make sure the note id is set in the saved state
		outState.putLong(BaseColumns._ID, mNoteId);
		outState.putString(SimpleNoteDao.BODY, mOriginalBody);
		outState.putInt(Constants.REQUEST_KEY, Constants.REQUEST_EDIT);
	}
	/**
	 * When user leaves this view save the note and set a result
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGGING_TAG, "Firing onPause and handling note saving if needed");
		// if text is unchanged send a CANCELLED result, otherwise save and send an OK result
		if (needsSave() && !mActivityStateSaved) {
			saveAndFinish();
		} else {
			Intent intent = getIntent();
			setResult(RESULT_CANCELED, intent);
		}
	}
	/**
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			handled = handleBackPressed();
		}
		if (!handled) {
			handled = super.onKeyDown(keyCode, event);
		}
		return handled;
	}
	/**
	 * The logic to handle the press of the back button
	 * @return whether or not the event was handled
	 */
	private boolean handleBackPressed() {
		Log.d(LOGGING_TAG, "Back button pressed");
		boolean handled = false;
		if (needsSave()) {
			// save finishes the Activity with an OK result
			saveAndFinish();
			handled = true;
		}
		return handled;
	}
	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_edit, menu);
		return true;
	}
	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				saveAndFinish(); // save returns ok
				return true;
			case R.id.menu_delete:
				delete(); // delete returns ok if there was a note to delete, cancelled otherwise
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	/**
	 * Checks if the body of the note has been updated compared to what was in the DB
	 * when this Activity was created
	 * @return whether or note the note body has changed
	 */
	private boolean needsSave() {
		final String body = ((EditText) findViewById(R.id.note_body)).getText().toString();
		return !(mNoteSaved || mOriginalBody.equals(body));
	}
    /**
     * Saves the note with data from the view
     * @return the note as it is now saved in the DB
     */
    private Note save() {
        final String body = ((EditText) findViewById(R.id.note_body)).getText().toString();
        final String now = Constants.serverDateFormat.format(new Date());
        final Note dbNote = dao.retrieve(mNoteId);
        final Note note;
        if (!(dbNote == null || dbNote.getKey().equals(Constants.DEFAULT_KEY))) {
            note = dao.save(dbNote.setBody(body).setDateModified(now).setSynced(false));
            Log.d(LOGGING_TAG, String.format("Saved the note '%d' with updated values", note.getId()));
        } else {
            note = dao.save(new Note(body, now));
            Log.d(LOGGING_TAG, String.format("Created the note '%d'", note.getId()));
        }
        mNoteId = note.getId();
        mNoteSaved = true;
        return note;
    }
	/**
	 * Saves the note with data from the view and finishes this Activity with an OK result
	 */
	private void saveAndFinish() {
		final Intent intent = getIntent();
		// get the note as it is from the db, set new fields values and save it
		final Note note = save();
		intent.putExtra(Note.class.getName(), note);
		setResult(RESULT_OK, intent);
		finish();
	}
	/**
	 * Deletes the note if it exists in the db otherwise cancel
	 */
	private void delete() {
		final Note dbNote = dao.retrieve(mNoteId);
		final Intent intent = getIntent();
		if (dbNote != null) {
			Log.d(LOGGING_TAG, "Note exists, marking it as deleted and finishing successfully");
			dao.delete(dbNote);
			// Have to re-retrieve because deleting doesn't update the note passed to delete
			intent.putExtra(Note.class.getName(), dao.retrieve(mNoteId));
			setResult(RESULT_OK, intent);
		} else {
			Log.d(LOGGING_TAG, "Note doesn't exist, cancelling");
			setResult(RESULT_CANCELED, intent);
		}
		mNoteSaved = true;
		finish();
	}
    /**
     * Checks for the presence of any hardware keyboard
     * @return whether or not a hardware keyboard exists
     */
    private boolean hasHardwareKeyboard() {
        return getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
    }
    /**
     * Request to open or show the soft keyboard
     * @param view to show for?
     */
    private void openKeyboard(final View view) {
        if (!mKeyboardOpen) {
            Log.d(LOGGING_TAG, "Trying to open keyboard");
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
        mKeyboardOpen = true;
    }
}
