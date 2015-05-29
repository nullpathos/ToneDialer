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

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import eu.nullpathos.tonedialer.R;

public class VolumePreference extends DialogPreference implements OnSeekBarChangeListener {
	private final String TAG = "NumberPreference";
	private final int DEFAULT_VALUE = 50;
	private int numberValue = DEFAULT_VALUE;
	private SeekBar volSeekBar;

	public VolumePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.volume_adjust);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		// view is your layout expanded and added to the dialog
		// find and hang on to your views here, add click listeners etc
		// basically things you would do in onCreate
		super.onBindDialogView(view);
		Log.d(TAG, "onBindDialogView");
		volSeekBar = (SeekBar) view.findViewById(R.id.vol_seekbar);
		volSeekBar.setOnSeekBarChangeListener(this);
		volSeekBar.setProgress(numberValue);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		Log.d(TAG, "onDialogClosed");
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			persistInt(numberValue);
		} else {
			numberValue = getPersistedInt(DEFAULT_VALUE);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		Log.d(TAG, "onSetInitialValue");
		if (restorePersistedValue) {
			// Restore existing state
			numberValue = this.getPersistedInt(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			numberValue = (Integer) defaultValue;
			persistInt(numberValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		Log.d(TAG, "onGetDefaultValue");
		return a.getInteger(index, DEFAULT_VALUE);

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		Log.d(TAG, "onProgressChanged progress="+progress);
		numberValue = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
