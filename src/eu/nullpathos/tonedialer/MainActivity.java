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
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import eu.nullpathos.tonedialer.R;

public class MainActivity extends Activity implements OnClickListener, OnTouchListener {
	static final String TAG = "ToneDialer";
	static final boolean DEBUG = false;

	private Button button_tone0, button_tone1, button_tone2, button_tone3;
	private Button button_tone4, button_tone5, button_tone6, button_tone7;
	private Button button_tone8, button_tone9, button_tonestar, button_tonepound;
//	private Button button_tonea, button_toneb, button_tonec, button_toned;
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
	private boolean forceSpeaker = false; // TODO: add this as a preference
	private boolean isSpeakerOn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG)
			Log.d(TAG, "in onCreate");
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

		soundPool = new SoundPool(9, AudioManager.STREAM_MUSIC, 0);
		if (soundPool == null) {
			if (DEBUG)
				Log.d(TAG, "oops, soundPool is null!");
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

		if (DEBUG)
			Log.d(TAG, "getting AudioManager");
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG)
			Log.d(TAG, "onPause");
		if (sequencePlay != null) {
			sequencePlay.cancel(true);
		}
		stopAllNow();
		button_playseq.setText(R.string.button_playtones_text);
		if (DEBUG)
			Log.d(TAG, "restoring volume");
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, outOfAppVolume, 0);
		if (forceSpeaker) {
			if (DEBUG)
				Log.d(TAG, "restoring speaker setting");
			audioManager.setSpeakerphoneOn(isSpeakerOn);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG)
			Log.d(TAG, "onResume");

		minTime = ((ToneDialerApp) getApplication()).minTime;
		silenceTime = ((ToneDialerApp) getApplication()).silenceTime;
		inAppVolume = ((ToneDialerApp) getApplication()).inAppVolume;

		int realVol = inAppVolume * (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100;

		outOfAppVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, realVol, 0);

		if (forceSpeaker) {
			isSpeakerOn = audioManager.isSpeakerphoneOn();
			if (!isSpeakerOn) {
				if (DEBUG)
					Log.d(TAG, "forcing speaker on");
				audioManager.setSpeakerphoneOn(true);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG)
			Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.d(TAG, "onDestroy");
		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (DEBUG)
			Log.d(TAG, "onClick");

		if (v.getId() == R.id.button_playtones) {
			playSequence(edittext_seqtoplay.getText().toString());
		}
		if (v.getId() == R.id.button_contacts) {
			// call contacts
			startActivityForResult(new Intent(this, ContactsActivity.class), 0);
		}
		if (v.getId() == R.id.button_clear) {
			edittext_seqtoplay.setText("");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			String s = data.getStringExtra(ContactsActivity.RESULT);
			edittext_seqtoplay.setText(s);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// if (DEBUG) Log.d(TAG, "onTouch");

		int action = event.getAction();
		final int v_id = v.getId();

		if (action == MotionEvent.ACTION_DOWN) {
			new Thread() {
				public void run() {
					if (DEBUG)
						Log.d(TAG, "here is where we play a tone");
					stopAllNow();
					switch (v_id) {
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

			switch (v_id) {
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
			new Thread() {
				public void run() {
					if (DEBUG)
						Log.d(TAG, "here is where we stop a playing tone");
					switch (v_id) {
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
		if (DEBUG)
			Log.d(TAG, "playSequence called with string=" + s);
		if (sequencePlay != null && sequencePlay.getStatus() != AsyncTask.Status.FINISHED && !sequencePlay.isCancelled()) {
			if (DEBUG)
				Log.d(TAG, "cancelling sequencePlay");
			sequencePlay.cancel(true);
			stopAllNow();
			button_playseq.setText(R.string.button_playtones_text);
		} else if (s != null && s.length() != 0) {
			if (DEBUG)
				Log.d(TAG, "starting new sequencePlay");
			sequencePlay = new SequencePlay();
			sequencePlay.execute(s);
			button_playseq.setText(R.string.button_cancel);
		}
	}

	private void playTone(char c) throws InterruptedException {
		if (DEBUG)
			Log.d(TAG, "playTone called with " + c);
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
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (DEBUG)
				Log.d(TAG, "in onPostExecute");
			button_playseq.setText(R.string.button_playtones_text);

		}

		@Override
		protected Void doInBackground(String... params) {
			if (DEBUG)
				Log.d(TAG, "playing silence");
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

	}

	class Tone {
		private SoundPool sp;
		private int streamId = 0;
		private int dtmf_tone;
		private boolean playing = false;
		private boolean timeUp = true;
		// private long minTime = 250; // (ms)
		// private long silenceTime = 70; // (ms)
		private Thread minTimeThread = null;
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
			timeUp = true;
			stopNow();
			Thread.sleep(silenceTime);
		}

		public void start() {
			// start the tone playing
			streamId = sp.play(dtmf_tone, volume, volume, 1, 0, 1.0f);
			minTimeThread = new Thread() {
				// this is the min time thread
				public void run() {
					playing = true;
					timeUp = false;
					try {
						Thread.sleep(minTime);
					} catch (InterruptedException e) {
						if (DEBUG)
							Log.d(TAG, "got interrupted!");
					}
					timeUp = true;
					if (DEBUG)
						Log.d(TAG, "time up");
				}
			};
			minTimeThread.start();
		}

		public void stop() {
			// stop only after minimum time is up
			if (playing) {
				if (timeUp) {
					if (streamId != 0) {
						sp.pause(streamId);
					}
					playing = false;
				} else {
					new Thread() {
						public void run() {
							try {
								if (minTimeThread != null) {
									minTimeThread.join(minTime);
								}
							} catch (InterruptedException e) {
								// do nothing
							}
							if (streamId != 0) {
								sp.pause(streamId);
							}
							playing = false;
						}
					}.start();
				}
			}
		}

		public void stopNow() {
			// stop now disregarding minimum time
			if (playing) {
				if (streamId != 0) {
					sp.pause(streamId);
				}
				playing = false;
				timeUp = true;
				if (minTimeThread != null) {
					minTimeThread.interrupt();
				}
			}
		}
	}

}