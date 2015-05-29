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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class ContactsActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "ContactsActivity";
	static final String[] FROM = { ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER };
	static final int[] TO = { android.R.id.text1, android.R.id.text2 };
	static final String RESULT = "phone_number";
	private Cursor cursor;
	private ContentResolver cr;
	private ListView list;
	private SimpleCursorAdapter adapter;
	
	// TODO: use the android contacts app instead

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.contacts);
		
		cr = getContentResolver();

		cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER }, null, null, null);

		list = (ListView) findViewById(R.id.listView_contacts);
		list.setOnItemClickListener(this);
		adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursor, FROM, TO);
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

				if (view.getId() != android.R.id.text1)
					return false;

				String contactId = cursor.getString(columnIndex);
				Cursor tempCursor = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[] { ContactsContract.Contacts._ID,
						ContactsContract.Contacts.DISPLAY_NAME }, ContactsContract.Contacts._ID + " = ?", new String[] { contactId }, null);
				tempCursor.moveToFirst();
				String contactName = tempCursor.getString(tempCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				((TextView) view).setText(contactName);
				tempCursor.close();

				return true;
			}

		});
		list.setAdapter(adapter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (cursor != null) {
			cursor.close();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String phoneNumber = ((TextView) view.findViewById(android.R.id.text2)).getText().toString();
//		Toast.makeText(getApplicationContext(), phoneNumber, Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK, new Intent().putExtra(RESULT, phoneNumber));
		finish();
	}

}
