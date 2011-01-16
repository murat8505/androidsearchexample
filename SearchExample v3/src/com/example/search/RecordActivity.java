package com.example.search;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class RecordActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_activity);

		//ѕолучаем URI с данными из Intent и запрашиваем данные через контент-провайдер
		Uri uri = getIntent().getData();
        Cursor cursor = managedQuery(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
        	//”станавливаем данные в текстовое поле
            cursor.moveToFirst();

            TextView record = (TextView) findViewById(R.id.record_header);
            int rIndex = cursor.getColumnIndexOrThrow(RecordsDbHelper.KEY_DATA);

            record.setText(cursor.getString(rIndex));
        }
    }
	
	//—оздаем меню дл€ вызова диалога поиска из этого активити
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_record:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }

}