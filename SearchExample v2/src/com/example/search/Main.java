package com.example.search;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

public class Main extends ListActivity {
	private EditText text;
	private Button add;
	private RecordsDbHelper mDbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Создаем экземпляр БД
		mDbHelper = new RecordsDbHelper(this);
		//Открываем БД для записи
		mDbHelper.open();
        //Получаем Intent
		Intent intent = getIntent();
		//Проверяем тип Intent
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			//Берем строку запроса из экстры
			String query = intent.getStringExtra(SearchManager.QUERY);
			//Создаем экземпляр SearchRecentSuggestions
	        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
	        //Сохраняем запрос
	        suggestions.saveRecentQuery(query, null);			
			//Выполняем поиск
			showResults(query);
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
		//Ищем совпадения
		Cursor cursor = mDbHelper.fetchRecordsByQuery(query);
		startManagingCursor(cursor);
		String[] from = new String[] { RecordsDbHelper.KEY_DATA };
		int[] to = new int[] { R.id.text1 };

		SimpleCursorAdapter records = new SimpleCursorAdapter(this,
				R.layout.record, cursor, from, to);
		//Обновляем адаптер
		setListAdapter(records);
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
        	//Вызываем поиск
        	onSearchRequested();
        	return true;
        case R.id.clear_recent_suggestions:
        	//Очищаем историю
        	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
        	        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
        	suggestions.clearHistory();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}