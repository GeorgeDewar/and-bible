package edu.cmu.pocketsphinx.demo;

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PocketSphinxAndroidDemo extends Activity implements
		OnTouchListener, RecognitionListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}

	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;

	TextView which_pass;
	/**
	 * Editable text view.
	 */

	final static int ACTIVITY_CREATE = 1;

	EditText words;

	/**
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.words.setText("");

			this.which_pass.setTextColor(Color.RED);
			this.which_pass.setText("First Pass");
			start_date = new Date();
			this.listening = true;
			this.rec.start();
			break;
		case MotionEvent.ACTION_UP:
			Date end_date = new Date();
			long nmsec = end_date.getTime() - start_date.getTime();
			this.speech_dur = (float) nmsec / 1000;
			if (this.listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				this.rec_dialog = ProgressDialog.show(
						PocketSphinxAndroidDemo.this, "",
						"Computing Final Pass .. ", true);
				this.rec_dialog.setCancelable(false);
				this.listening = false;
			}
			this.rec.stop();
			break;
		default:
			;
		}
		/* Let the button handle its own state */
		return false;
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {

		CharSequence title = "George's PocketSphinx Testing Lab";
		this.setTitle(title);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button b = (Button) findViewById(R.id.Button01);
		b.setOnTouchListener(this);
		this.performance_text = (TextView) findViewById(R.id.PerformanceText);
		this.which_pass = (TextView) findViewById(R.id.WhichPass);
		this.words = (EditText) findViewById(R.id.EditText01);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(words.getWindowToken(), 0);
		
		this.rec = new RecognizerTask("hub4wsj_sc_8k", "hub4.5000.DMP",
				"hub4.5000.dic");

		this.rec_thread = new Thread(this.rec);
		this.listening = false;
		this.rec.setRecognitionListener(this);
		this.rec_thread.start();
		

	}

	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final PocketSphinxAndroidDemo that = this;
		final String hyp = b.getString("hyp");

		that.words.post(new Runnable() {
			public void run() {
				that.which_pass.setTextColor(Color.RED);
				that.which_pass.setText("First Pass");
				that.words.setText(hyp);
			}
		});
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		final String hyp = b.getString("hyp");
		final PocketSphinxAndroidDemo that = this;
		this.words.post(new Runnable() {
			public void run() {
				that.which_pass.setTextColor(Color.GREEN);
				that.which_pass.setText("Final Pass");
				that.words.setText(hyp);

				Date end_date = new Date();
				long nmsec = end_date.getTime() - that.start_date.getTime();
				float rec_dur = (float) nmsec / 1000;
				that.performance_text.setText(String.format(
						"%.2f seconds %.2f xRT", that.speech_dur, rec_dur
								/ that.speech_dur));
				Log.d(getClass().getName(), "Hiding Dialog");
				that.rec_dialog.dismiss();
			}
		});
	}

	public void onError(int err) {
		final PocketSphinxAndroidDemo that = this;
		that.words.post(new Runnable() {
			public void run() {
				that.rec_dialog.dismiss();
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.exit:
			exitApplication();
			return true;
		}

		return false;

	}

	public void exitApplication() {

		Log.i("PocketSphinxAndroidDemo", "terminated!!");
		super.onDestroy();
		this.finish();
	}
	
}