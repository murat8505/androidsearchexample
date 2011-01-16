package com.example.search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class SuggestionProvider extends ContentProvider{

    private RecordsDbHelper mDbHelper;
    
    public static String AUTHORITY = "com.example.search.SuggestionProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/records");
    
    //MIME типы для getType()
    public static final String RECORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                                                  "/vnd.example.search";
    public static final String RECORD_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                                                  "/vnd.example.search";

    //Для матчера разных URI
    private static final int SEARCH_RECORDS = 0;
    private static final int GET_RECORD = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final UriMatcher sURIMatcher = makeUriMatcher();
               
	@Override
	public boolean onCreate() {
		mDbHelper = new RecordsDbHelper(getContext());
		return true;
	}

	/**
	 * Обрабатывает запросы от Search Manager'a.
	 * Когда запрашивается конкретный элемент, то требуется только URI.
	 * Когда запрашивается поиск по всей таблице, то первый элемент параметра selectionArgs содержит строку запроса.
	 * Остальные параметры не нужны.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		//Используем UriMatcher, чтобы узнать какой тип запроса получен. Далее формируем соответствующий запрос к БД
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case SEARCH_RECORDS:
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case GET_RECORD:
                return getRecord(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
	}
	
    private Cursor getSuggestions(String query) {
      query = query.toLowerCase();
      String[] columns = new String[] {
              BaseColumns._ID,
              RecordsDbHelper.KEY_DATA,
              SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
      return mDbHelper.getRecordMatches(query, columns);
    }
    
    private Cursor search(String query) {
      query = query.toLowerCase();
      String[] columns = new String[] {
          BaseColumns._ID,
          RecordsDbHelper.KEY_DATA};

      return mDbHelper.getRecordMatches(query, columns);
    }
    
    private Cursor getRecord(Uri uri) {
      String rowId = uri.getLastPathSegment();
      String[] columns = new String[] {
          RecordsDbHelper.KEY_DATA};

      return mDbHelper.getRecord(rowId, columns);
    }
    
    /**
     * Вспомогательный метод
     * нужен для сопоставления разным URI конкретных значений 
     */
    private static UriMatcher makeUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        // Для записей
        matcher.addURI(AUTHORITY, "records", SEARCH_RECORDS);
        matcher.addURI(AUTHORITY, "records/#", GET_RECORD);
        // Для подсказок
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }
	
    //Требуемые методы (наследуются от класса ContentProvider)
	@Override
	public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_RECORDS:
                return RECORDS_MIME_TYPE;
            case SEARCH_SUGGEST:
            	return SearchManager.SUGGEST_MIME_TYPE;
            case GET_RECORD:
                return RECORD_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }	
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
	
}