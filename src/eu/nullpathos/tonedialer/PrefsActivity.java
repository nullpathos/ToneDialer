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

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import eu.nullpathos.tonedialer.R;

public class PrefsActivity extends PreferenceActivity {
	private final String TAG = "PrefsActivity";
	private final boolean DEBUG = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG)
			Log.d(TAG, "onCreate");
		addPreferencesFromResource(R.xml.prefs);
	}

}
