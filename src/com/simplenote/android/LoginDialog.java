package com.simplenote.android;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.simplenote.android.APIBase.Response;

public class LoginDialog extends Activity {
	private static final String LOGGING_TAG = Constants.TAG + "LoginDialog";

	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor;

	private String authenticating;
	private String initializing;
	private String loggingIn;

	public JSONObject mUserData;
	public ProgressDialog mProgressDialog;

	private Thread mThread = null;

	public LoginDialog() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fetchStrings();

		mPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);
		mPrefsEditor = mPrefs.edit();

		setContentView(R.layout.login);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		EditText loginBox = (EditText) findViewById(R.id.email);
		EditText passwordBox = (EditText) findViewById(R.id.password);

		loginBox.setText(mPrefs.getString(Preferences.EMAIL, ""));
		passwordBox.setText(mPrefs.getString(Preferences.PASSWORD, ""));
		passwordBox.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (mThread != null) { return true; }
				mThread = new Thread(signinRunnable);
				mProgressDialog = ProgressDialog.show(LoginDialog.this, loggingIn, initializing);
				Log.d(LOGGING_TAG, "Created progressDialog: " + mProgressDialog.toString());
				Log.i(LOGGING_TAG, "Starting login thread");
				mThread.start();
				return false;
			}
			
		});
	}

	private void fetchStrings() {
		if (authenticating == null || initializing == null || loggingIn == null) {
			authenticating = getString(R.string.status_authenticating);
			initializing = getString(R.string.status_initializing);
			loggingIn = getString(R.string.status_loggingin);
		}
	}

	private void closeDialog() {
		Log.d(LOGGING_TAG, "Closing progressDialog: " + mProgressDialog.toString());
		mProgressDialog.dismiss();
		mThread = null;
	}

	@Override
	public void onPause() {
		if ( mProgressDialog != null && mProgressDialog.isShowing() ) {
			runOnUiThread(new Runnable() {
				public void run() {
					closeDialog();
				}
			});
		}
		super.onPause();
	}

	private Runnable signinRunnable = new Runnable() {
		public void run() {
			EditText loginBox = (EditText) findViewById(R.id.email);
			EditText passwordBox = (EditText) findViewById(R.id.password);
	
			String email = loginBox.getText().toString();
			String password = passwordBox.getText().toString();

			// Update UI to say we're sending the request
			runOnUiThread(new Runnable() {
				public void run() {
					mProgressDialog.setMessage(authenticating);
				}
			});

			Response authResponse = APIHelper.getLoginResponse(email,password);

			if (authResponse.statusCode == 401) { // failed auth login
				Log.i(LOGGING_TAG, "Login auth failed with API server.");
				runOnUiThread(new Runnable() {
					public void run() {
						closeDialog();
						Toast.makeText(LoginDialog.this, R.string.error_authentication, Toast.LENGTH_LONG).show();
					}
				});
			} else if (authResponse.statusCode == 200) { // successful auth login
				Log.i(LOGGING_TAG, "Login auth success with API server.");
				mPrefsEditor.putString(Preferences.EMAIL, email);
				mPrefsEditor.putString(Preferences.PASSWORD, password);
				mPrefsEditor.putString(Preferences.TOKEN, authResponse.resp.replaceAll("(\\r|\\n)", ""));
				mPrefsEditor.commit();

				// Give control back to SimpleNote Activity
				runOnUiThread(new Runnable() {
					public void run() {
						closeDialog();
						Intent intent = new Intent(LoginDialog.this, SimpleNote.class);
						startActivity(intent);
						LoginDialog.this.finish();
					}
				});
			}
		}
	};
}
