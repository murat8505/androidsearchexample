package com.example.search;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Main extends ListActivity {
	
	private EditText text;
	private Button add;
	private RecordsDbHelper mDbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mDbHelper = new RecordsDbHelper(this);
		
		Intent intent = getIntent();
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			//Берем строку запроса из экстры
			String query = intent.getStringExtra(SearchManager.QUERY);
			//Выполняем поиск
			showResults(query);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())){
			//Создаем Intent для открытия RecordActivity
            Intent recordIntent = new Intent(this, RecordActivity.class);
            recordIntent.setData(intent.getData());
            startActivity(recordIntent);
            finish();
		}
		
		add = (Button) findViewById(R.id.add);
		text = (EditText) findViewById(R.id.text);
		add.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String data = text.getText().toString();
				if (!data.equals("")) {
					saveTask(data);
					text.setText("");
				}
			}
		});
		
	}

	private void saveTask(String data) {
		mDbHelper.createRecord(data);
	}
	
    private void showResults(String query) {

    	//Запрашиваем у контент-провайдера курсор на записи 
        Cursor cursor = managedQuery(SuggestionProvider.CONTENT_URI, null, null,
                                new String[] {query}, null);
        if (cursor == null) {
            Toast.makeText(this, "There are no results", Toast.LENGTH_SHORT).show();
        } else {
        	//Обновляем адаптер
            String[] from = new String[] { RecordsDbHelper.KEY_DATA };
            int[] to = new int[] { R.id.text1 };
            SimpleCursorAdapter records = new SimpleCursorAdapter(this, R.layout.record, cursor, from, to);
            getListView().setAdapter(records);            
        }        
    }
	

	//Создаем меню для вызова поиска (интерфейс в res/menu/main_menu.xml)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;	
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.search_record:
        	onSearchRequested();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}