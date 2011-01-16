package com.example.search;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

public class RecordsDbHelper {

	//Единственный столбец в таблице - данные
	public static final String KEY_DATA = SearchManager.SUGGEST_COLUMN_TEXT_1;

	private static final String TAG = "RecordsDbHelper";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "datas";
	private static final String DATABASE_TABLE = "records";
	private static final int DATABASE_VERSION = 2;
	
	//Сценарий создания БД
	private static final String DATABASE_CREATE =
		"CREATE VIRTUAL TABLE " + DATABASE_TABLE +
        " USING fts3 (" + KEY_DATA + ");";	

	private static final HashMap<String,String> mColumnMap = buildColumnMap();
	
	/**
	 * Возвращает курсор, указывающий на запись с rowId
	 * @param rowId id возвращаемой записи
	 * @param columns возвращаемые столбцы записи; если null, то все 
	 * @return курсор, указывающий на определенную запись, null - если не запись не найдена
	 */
    public Cursor getRecord(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);
    }
    
    /**
     * Возвращает курсор, указывающий на все записи, совпадающие с запросом
     * @param query текст поискового запроса
     * @param columns возвращаемые столбцы записи; если null, то все 
     * @return курсор, указывающий на записи, совпадающие с запросом, null - если не записи не найдена
     */
    public Cursor getRecordMatches(String query, String[] columns) {
        String selection = KEY_DATA + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }
    
	/**
	 * Создает отображение всевозможных запрашиваемых столбцов.
	 * Будет установлено как проекция в SQLiteQueryBuilder.
	 * Нужно для того, чтобы назначить для каждой записи уникальные значения SUGGEST_COLUMN_INTENT_DATA_ID
	 * которые используются для получения конкретной записи по URI.
	 */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_DATA, KEY_DATA);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        return map;
    }

    /**
     * 
     * @param selection оператор выборки
     * @param selectionArgs аргументы, заменяющие "?" в запросе к БД
     * @param columns возвращаемые столбцы записи
     * @return курсор, указывающий на все записи, совпадающие с поисковым запросом
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* SQLiteBuilder предоставляет возможность создания отображения для всех
         * необходимых столбцов БД, что позволяет не сообщать контент-провайдеру
         * настоящие имена столбцов.
         */

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(DATABASE_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDbHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }    
    	
    /**
     *Создает/открывает БД
     */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS records");
			onCreate(db);
		}
	}

    public RecordsDbHelper(Context context) {
        mDbHelper = new DatabaseHelper(context);
    }

	/**
	 * Добавляет запись в таблицу
	 * @param data данные, сохраняемые в таблицу
	 * @return id записи, или -1, если добавление не удалось
	 */
	public long createRecord(String data) {
		mDb = mDbHelper.getWritableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DATA, data);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
}