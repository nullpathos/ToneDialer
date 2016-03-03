/*
    This file is part of ToneDialer from nullpathos.eu.

    ToneDialer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ToneDialer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ToneDialer.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.nullpathos.tonedialer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener, OnTouchListener {
	static final String TAG = "ToneDialer";
	static final boolean DEBUG = false;

	private static final Uri CONTACTS_CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
	private static final Uri PHONE_NUMBER_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	private static final String DATA_CONTACT_ID = ContactsContract.Data.CONTACT_ID;
	private static final String CONTACTS_ID = ContactsContract.Contacts._ID;
	private static final String DATA_CONTACT_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
	private static final int PICK_CONTACT_REQUEST = 0;

	private Button button_tone0, button_tone1, button_tone2, button_tone3;
	private Button button_tone4, button_tone5, button_tone6, button_tone7;
	private Button button_tone8, button_tone9, button_tonestar, button_tonepound;
	// private Button button_tonea, button_toneb, button_tonec, button_toned;
	private Tone t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, ts, tp, silence;
	private Button button_contacts;
	private Button button_playseq;
	private Button button_clear;
	private EditText edittext_seqtoplay;
	private SequencePlay sequencePlay = null;
	private SoundPool soundPool;
	private long minTime, silenceTime;
	private AudioManager audioManager;
	private int inAppVolume; // 0-100
	private int outOfAppVolume; // 0-max from AudioManager
	private boolean forceSpeaker = false; // TODO: add this as a preference?
	private boolean isSpeakerOn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DEBUG) Log.d(TAG, "in onCreate");
		setContentView(R.layout.activity_main);
		button_tone0 = (Button) findViewById(R.id.button_tone0);
		button_tone0.setOnTouchListener(this);
		button_tone1 = (Button) findViewById(R.id.button_tone1);
		button_tone1.setOnTouchListener(this);
		button_tone2 = (Button) findViewById(R.id.button_tone2);
		button_tone2.setOnTouchListener(this);
		button_tone3 = (Button) findViewById(R.id.button_tone3);
		button_tone3.setOnTouchListener(this);
		button_tone4 = (Button) findViewById(R.id.button_tone4);
		button_tone4.setOnTouchListener(this);
		button_tone5 = (Button) findViewById(R.id.button_tone5);
		button_tone5.setOnTouchListener(this);
		button_tone6 = (Button) findViewById(R.id.button_tone6);
		button_tone6.setOnTouchListener(this);
		button_tone7 = (Button) findViewById(R.id.button_tone7);
		button_tone7.setOnTouchListener(this);
		button_tone8 = (Button) findViewById(R.id.button_tone8);
		button_tone8.setOnTouchListener(this);
		button_tone9 = (Button) findViewById(R.id.button_tone9);
		button_tone9.setOnTouchListener(this);
		button_tonestar = (Button) findViewById(R.id.button_tonestar);
		button_tonestar.setOnTouchListener(this);
		button_tonepound = (Button) findViewById(R.id.button_tonepound);
		button_tonepound.setOnTouchListener(this);

		button_contacts = (Button) findViewById(R.id.button_contacts);
		button_contacts.setOnClickListener(this);

		button_playseq = (Button) findViewById(R.id.button_playtones);
		button_playseq.setOnClickListener(this);

		button_clear = (Button) findViewById(R.id.button_clear);
		button_clear.setOnClickListener(this);

		edittext_seqtoplay = (EditText) findViewById(R.id.editText_seqtoplay);

		// create SoundPool object with 3 streams
		soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		if (soundPool == null) {
			if (DEBUG) Log.d(TAG, "oops, soundPool is null!");
		}

		t0 = new Tone(this, soundPool, R.raw.dtmf0);
		t1 = new Tone(this, soundPool, R.raw.dtmf1);
		t2 = new Tone(this, soundPool, R.raw.dtmf2);
		t3 = new Tone(this, soundPool, R.raw.dtmf3);
		t4 = new Tone(this, soundPool, R.raw.dtmf4);
		t5 = new Tone(this, soundPool, R.raw.dtmf5);
		t6 = new Tone(this, soundPool, R.raw.dtmf6);
		t7 = new Tone(this, soundPool, R.raw.dtmf7);
		t8 = new Tone(this, soundPool, R.raw.dtmf8);
		t9 = new Tone(this, soundPool, R.raw.dtmf9);
		ts = new Tone(this, soundPool, R.raw.dtmfstar);
		tp = new Tone(this, soundPool, R.raw.dtmfpound);
		silence = new Tone(this, soundPool, R.raw.silence400ms);

		if (DEBUG) Log.d(TAG, "getting AudioManager");
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
		if (sequencePlay != null) {
			sequencePlay.cancel(true);
		}
		stopAllNow();
		button_playseq.setText(R.string.button_playtones_text);
		if (DEBUG) Log.d(TAG, "restoring outOfAppVolume");
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, outOfAppVolume, 0);
		if (forceSpeaker) {
			if (DEBUG) Log.d(TAG, "restoring speaker setting");
			audioManager.setSpeakerphoneOn(isSpeakerOn);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume");

		minTime = ((ToneDialerApp) getApplication()).minTime;
		silenceTime = ((ToneDialerApp) getApplication()).silenceTime;
		inAppVolume = ((ToneDialerApp) getApplication()).inAppVolume;

		int realVol = inAppVolume * (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100;

		outOfAppVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (DEBUG) Log.d(TAG, "setting inAppVolume");
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, realVol, 0);

		if (forceSpeaker) {
			isSpeakerOn = audioManager.isSpeakerphoneOn();
			if (!isSpeakerOn) {
				if (DEBUG) Log.d(TAG, "forcing speaker on");
				audioManager.setSpeakerphoneOn(true);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (DEBUG) Log.d(TAG, "onClick");

		if (v.getId() == R.id.button_playtones) {
			playSequence(edittext_seqtoplay.getText().toString());
		}
		if (v.getId() == R.id.button_clear) {
			edittext_seqtoplay.setText("");
		}
		if (v.getId() == R.id.button_contacts) {
			// call contacts
			try {
				Intent intent = new Intent(Intent.ACTION_PICK, CONTACTS_CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT_REQUEST);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (DEBUG) Log.d(TAG, "onActivityResult");

		if (resultCode == Activity.RESULT_OK && requestCode == PICK_CONTACT_REQUEST) {
			ContentResolver cr = getContentResolver();

			Cursor cursor = cr.query(data.getData(), null, null, null, null);
			// if (DEBUG) Log.d(TAG, "cursor size " + cursor.getCount());

			if (cursor != null && cursor.moveToFirst()) {
				String contactId = cursor.getString(cursor.getColumnIndex(CONTACTS_ID));
				String[] projection = new String[] { DATA_CONTACT_NUMBER };
				String selection = DATA_CONTACT_ID + " = ?";
				String[] selectionParams = new String[] { contactId };
				Cursor contactCur = cr.query(PHONE_NUMBER_URI, projection, selection, selectionParams, null);
				// if (DEBUG) Log.d(TAG, "contactCur size " +
				// contactCur.getCount());

				if (contactCur != null && contactCur.moveToFirst()) {
					String contactNumber = contactCur.getString(contactCur.getColumnIndex(DATA_CONTACT_NUMBER));

					if (contactNumber != null) {
						edittext_seqtoplay.setText(contactNumber);
					}
				}
				if (contactCur != null) contactCur.close();
			}
			if (cursor != null) cursor.close();
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// if (DEBUG) Log.d(TAG, "onTouch");
		int action = event.getAction();
		final int viewId = view.getId();

		if (action == MotionEvent.ACTION_DOWN) {
			if (DEBUG) Log.d(TAG, "onTouch ACTION_DOWN");
			new Thread() {
				public void run() {
					if (DEBUG) Log.d(TAG, "play the tone");
					// stop any playing tones first - no polyphony
					stopAllNow();
					switch (viewId) {
					case R.id.button_tone0:
						t0.start();
						break;
					case R.id.button_tone1:
						t1.start();
						break;
					case R.id.button_tone2:
						t2.start();
						break;
					case R.id.button_tone3:
						t3.start();
						break;
					case R.id.button_tone4:
						t4.start();
						break;
					case R.id.button_tone5:
						t5.start();
						break;
					case R.id.button_tone6:
						t6.start();
						break;
					case R.id.button_tone7:
						t7.start();
						break;
					case R.id.button_tone8:
						t8.start();
						break;
					case R.id.button_tone9:
						t9.start();
						break;
					case R.id.button_tonestar:
						ts.start();
						break;
					case R.id.button_tonepound:
						tp.start();
						break;
					default:
						break;
					}
				}
			}.start();

			switch (viewId) {
			case R.id.button_tone0:
				edittext_seqtoplay.append("0");
				break;
			case R.id.button_tone1:
				edittext_seqtoplay.append("1");
				break;
			case R.id.button_tone2:
				edittext_seqtoplay.append("2");
				break;
			case R.id.button_tone3:
				edittext_seqtoplay.append("3");
				break;
			case R.id.button_tone4:
				edittext_seqtoplay.append("4");
				break;
			case R.id.button_tone5:
				edittext_seqtoplay.append("5");
				break;
			case R.id.button_tone6:
				edittext_seqtoplay.append("6");
				break;
			case R.id.button_tone7:
				edittext_seqtoplay.append("7");
				break;
			case R.id.button_tone8:
				edittext_seqtoplay.append("8");
				break;
			case R.id.button_tone9:
				edittext_seqtoplay.append("9");
				break;
			case R.id.button_tonestar:
				edittext_seqtoplay.append("*");
				break;
			case R.id.button_tonepound:
				edittext_seqtoplay.append("#");
				break;
			default:
				break;
			}
		}

		if (action == MotionEvent.ACTION_UP) {
			if (DEBUG) Log.d(TAG, "onTouch ACTION_UP");
			new Thread() {
				public void run() {
					if (DEBUG) Log.d(TAG, "stop the tone");
					switch (viewId) {
					case R.id.button_tone0:
						t0.stop();
						break;
					case R.id.button_tone1:
						t1.stop();
						break;
					case R.id.button_tone2:
						t2.stop();
						break;
					case R.id.button_tone3:
						t3.stop();
						break;
					case R.id.button_tone4:
						t4.stop();
						break;
					case R.id.button_tone5:
						t5.stop();
						break;
					case R.id.button_tone6:
						t6.stop();
						break;
					case R.id.button_tone7:
						t7.stop();
						break;
					case R.id.button_tone8:
						t8.stop();
						break;
					case R.id.button_tone9:
						t9.stop();
						break;
					case R.id.button_tonestar:
						ts.stop();
						break;
					case R.id.button_tonepound:
						tp.stop();
						break;
					default:
						break;
					}
				}
			}.start();
			view.performClick();
		}
		return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_preferences:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	private void playSequence(final String s) {
		if (DEBUG) Log.d(TAG, "playSequence called with string=" + s);
		if (sequencePlay != null && sequencePlay.getStatus() != AsyncTask.Status.FINISHED && !sequencePlay.isCancelled()) {
			if (DEBUG) Log.d(TAG, "cancelling sequencePlay");
			sequencePlay.cancel(true);
			stopAllNow();
			button_playseq.setText(R.string.button_playtones_text);
		} else if (s != null && s.length() != 0) {
			if (DEBUG) Log.d(TAG, "starting new sequencePlay");
			sequencePlay = new SequencePlay();
			sequencePlay.execute(s);
			button_playseq.setText(R.string.button_cancel);
		}
	}

	private void playTone(char c) throws InterruptedException {
		if (DEBUG) Log.d(TAG, "playTone called with " + c);
		switch (c) {
		case '0':
			t0.sequencePlay();
			break;
		case '1':
			t1.sequencePlay();
			break;
		case '2':
			t2.sequencePlay();
			break;
		case '3':
			t3.sequencePlay();
			break;
		case '4':
			t4.sequencePlay();
			break;
		case '5':
			t5.sequencePlay();
			break;
		case '6':
			t6.sequencePlay();
			break;
		case '7':
			t7.sequencePlay();
			break;
		case '8':
			t8.sequencePlay();
			break;
		case '9':
			t9.sequencePlay();
			break;
		case '*':
			ts.sequencePlay();
			break;
		case '#':
			tp.sequencePlay();
			break;
		default:
			break;
		}
	}

	private void stopAllNow() {
		t0.stopNow();
		t1.stopNow();
		t2.stopNow();
		t3.stopNow();
		t4.stopNow();
		t5.stopNow();
		t6.stopNow();
		t7.stopNow();
		t8.stopNow();
		t9.stopNow();
		ts.stopNow();
		tp.stopNow();
	}

	class SequencePlay extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			if (DEBUG) Log.d(TAG, "playing silence");
			try {
				// this is just to wake up the audio driver
				silence.sequencePlay();
			} catch (InterruptedException e1) {
				return null;
			}
			for (int i = 0; i < params[0].length(); i++) {
				if (this.isCancelled()) {
					return null;
				}
				try {
					playTone(params[0].charAt(i));
				} catch (InterruptedException e) {
					return null;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (DEBUG) Log.d(TAG, "in onPostExecute");
			button_playseq.setText(R.string.button_playtones_text);
		}
	}

	class Tone {
		private SoundPool sp;
		private volatile int streamId = 0;
		private int dtmf_tone;
		private volatile boolean playing = false;
		private volatile boolean timeUp = true;
		private volatile Thread minTimeThread = null;
		private float volume = 1.0f;

		public Tone(Context context, SoundPool sp, int resource_id) {
			// constructor. resource_id is the R.raw sound file
			this.sp = sp;
			this.dtmf_tone = sp.load(context, resource_id, 1);
		}

		public void sequencePlay() throws InterruptedException {
			// start the tone, wait for minTime,
			// stop the tone, wait for silenceTime
			streamId = sp.play(dtmf_tone, volume, volume, 1, 0, 1.0f);
			timeUp = false;
			playing = true;
			Thread.sleep(minTime);
			stopNow();
			Thread.sleep(silenceTime);
		}

		public void start() {
			// start the tone playing
			synchronized (this) {
				if (!playing) {
					streamId = sp.play(dtmf_tone, volume, volume, 1, 0, 1.0f);
					playing = true;
					// only one minTimeThread at a time so interrupt the current if necessary
					// (overkill as already done in stopNow)
					if (minTimeThread != null && minTimeThread.isAlive()) {
						minTimeThread.interrupt();
					}
					minTimeThread = new Thread() {
						public void run() {
							timeUp = false;
							try {
								Thread.sleep(minTime);
							} catch (InterruptedException e) {
								// do nothing
							}
							timeUp = true;
							if (DEBUG) Log.d(TAG, " time up");
						}
					};
					minTimeThread.start();
				}
			}
		}

		public synchronized void stop() {
			// stop only after minimum time is up
			// no check is made of playing here because it may be false when the tone is playing
			// this is possibly due to android implementation of volatile vs optimisation (?)
			// makes no difference as we want to avoid single tone heterphony anyway
			synchronized (this) {
				if (timeUp) {
					if (streamId != 0) {
						sp.pause(streamId);
					}
					playing = false;
				} else {
					new Thread() {
						public void run() {
							// wait for the minTime delay to expire
							try {
								if (minTimeThread != null) {
									minTimeThread.join(minTime);
								}
							} catch (InterruptedException e) {
								// do nothing
							}
							// stop the tone
							if (streamId != 0) {
								sp.pause(streamId);
							}
							playing = false;
						}
					}.start();
				}
			}
		}

		public synchronized void stopNow() {
			// stop now disregarding minimum time
			synchronized (this) {
				if (streamId != 0) {
					sp.pause(streamId);
				}
				if (minTimeThread != null && minTimeThread.isAlive()) {
					minTimeThread.interrupt();
				}
				playing = false;
				timeUp = true;
			}
		}
	}

}