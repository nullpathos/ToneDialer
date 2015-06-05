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

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class ToneDialerApp extends Application implements OnSharedPreferenceChangeListener {
	private final String TAG = "ToneDialerApp";
	private final boolean DEBUG = false;

	SharedPreferences prefs;
	private final int DEFAULT_MINTIME = 250;
	private final int DEFAULT_SILENCETIME = 70;
	private final int DEFAULT_INAPPVOLUME = 50;
	long minTime, silenceTime; // 0-1000ms
	int inAppVolume; // 0-100

	@Override
	public void onCreate() {
		super.onCreate();

		// Preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		minTime = (long) prefs.getInt("mintime", DEFAULT_MINTIME);
		silenceTime = (long) prefs.getInt("gap", DEFAULT_SILENCETIME);
		inAppVolume = prefs.getInt("volume", DEFAULT_INAPPVOLUME);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (DEBUG)
			Log.d(TAG, "onSharedPreferenceChanged key=" + key);
		minTime = (long) prefs.getInt("mintime", DEFAULT_MINTIME);
		silenceTime = (long) prefs.getInt("gap", DEFAULT_SILENCETIME);
		inAppVolume = prefs.getInt("volume", DEFAULT_INAPPVOLUME);
	}

}
