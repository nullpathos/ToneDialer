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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import eu.nullpathos.tonedialer.R;

public class NumberPreference extends DialogPreference implements OnClickListener {
	private final String TAG = "NumberPreference";
	private final boolean DEBUG = false;

	private final int DEFAULT_VALUE = 0;
	private final int MAX_VALUE = 1000;
	private final int MIN_VALUE = 0;
	private int numberValue = DEFAULT_VALUE;
	private EditText value_view;
	private Button plusButton, minusButton;

	public NumberPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.number_pref_dialog);
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
		if (DEBUG)
			Log.d(TAG, "onBindDialogView");
		value_view = (EditText) view.findViewById(R.id.editText_value);
		value_view.setText(String.valueOf(numberValue));
		plusButton = (Button) view.findViewById(R.id.button_plus);
		plusButton.setOnClickListener(this);
		minusButton = (Button) view.findViewById(R.id.button_minus);
		minusButton.setOnClickListener(this);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (DEBUG)
			Log.d(TAG, "onDialogClosed");
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			numberValue = Integer.parseInt(value_view.getText().toString());
			if (numberValue < MIN_VALUE) {
				numberValue = MIN_VALUE;
			}
			if (numberValue > MAX_VALUE) {
				numberValue = MAX_VALUE;
			}
			persistInt(numberValue);
		} else {
			numberValue = getPersistedInt(DEFAULT_VALUE);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if (DEBUG)
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
		if (DEBUG)
			Log.d(TAG, "onGetDefaultValue");
		return a.getInteger(index, DEFAULT_VALUE);

	}

	@Override
	public void onClick(View v) {
		if (DEBUG)
			Log.d(TAG, "onClick");
		int val = Integer.parseInt(value_view.getText().toString());

		if (v.getId() == R.id.button_plus) {
			val = val + 10;
		}
		if (v.getId() == R.id.button_minus) {
			val = val - 10;
		}
		if (val < MIN_VALUE) {
			val = MIN_VALUE;
		}
		if (val > MAX_VALUE) {
			val = MAX_VALUE;
		}
		value_view.setText(String.valueOf(val));
	}

}
