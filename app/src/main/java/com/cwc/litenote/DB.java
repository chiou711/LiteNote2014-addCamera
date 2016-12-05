package com.cwc.litenote;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


// tab information: in naming rule, tab_table_id is same as table number 


public class DB   
{

    private static Context mContext = null;
    private static DatabaseHelper mDbHelper ;
    private static SQLiteDatabase mDb;
    private static final String DB_NAME = "notes.db";
    private static int DB_VERSION = 1;
    private static String DB_TABLE_NAME_PREFIX = "notesTable";
    private static String DB_TABLE_NAME;
    private static String mTableNum;
    
    public static final String KEY_NOTE_ID = "_id"; //do not rename _id for using CursorAdapter 
    public static final String KEY_NOTE_TITLE = "note_title";
    public static final String KEY_NOTE_BODY = "note_body";
    public static final String KEY_NOTE_MARKING = "note_marking";
    public static final String KEY_NOTE_PICTURE = "note_picture";
    public static final String KEY_NOTE_CREATED = "note_created";
    
    public static final String KEY_TAB_ID = "tab_id"; //can rename _id for using BaseAdapter
    public static final String KEY_TAB_NAME = "tab_name";
    public static final String KEY_TAB_TABLE_ID = "tab_table_id";
    public static final String KEY_TAB_STYLE = "tab_style";
    public static final String KEY_TAB_CREATED = "tab_created";
    
	private static int DEFAULT_TAB_COUNT = 5;//10; //first time
	

    /** Constructor */
    public DB(Context context) {
        DB.mContext = context;
    }
   
    public DB open() throws SQLException 
    {
        mDbHelper = new DatabaseHelper(mContext); 
        
        // will call DatabaseHelper.onCreate()first time when database is not created yet
        mDb = mDbHelper.getWritableDatabase();
        return this;  
    }

    public void close() {
        mDbHelper.close(); 
    }
    
    
    private static class DatabaseHelper extends SQLiteOpenHelper
    {  
        public DatabaseHelper(Context context) 
        {  
            super(context, DB_NAME , null, DB_VERSION);
        }

        @Override
        //Called when the database is created ONLY for the first time.
        public void onCreate(SQLiteDatabase db)
        {   
        	// table for Tab
        	DB_TABLE_NAME = "TAB_INFO";
        	String DB_CREATE;
            DB_CREATE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME + "(" + 
			            		KEY_TAB_ID + " INTEGER PRIMARY KEY," +
			            		KEY_TAB_NAME + " TEXT," +
			            		KEY_TAB_TABLE_ID + " INTEGER," +
			            		KEY_TAB_STYLE + " INTEGER," +
			            		KEY_TAB_CREATED + " INTEGER);";
            db.execSQL(DB_CREATE);  
            
        	// tables for notes
        	for(int i=1;i<=DEFAULT_TAB_COUNT;i++)
        	{
	        	DB_TABLE_NAME = DB_TABLE_NAME_PREFIX.concat(String.valueOf(i));
	            DB_CREATE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME + "(" + 
				            		KEY_NOTE_ID + " INTEGER PRIMARY KEY," +
				            		KEY_NOTE_TITLE + " TEXT," +
				            		KEY_NOTE_PICTURE + " TEXT," +
				    				KEY_NOTE_BODY + " TEXT," + 
				    				KEY_NOTE_MARKING + " INTEGER," +
				    				KEY_NOTE_CREATED + " INTEGER);";
	            db.execSQL(DB_CREATE);         
        	}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        { 
//            db.execSQL("DROP DATABASE IF EXISTS "+DATABASE_TABLE); 
//            System.out.println("DB / _onUpgrade / drop DB / DATABASE_NAME = " + DB_NAME);
     	    onCreate(db);
        }
        
        @Override
        public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion)
        { 
//            db.execSQL("DROP DATABASE IF EXISTS "+DATABASE_TABLE); 
//            System.out.println("DB / _onDowngrade / drop DB / DATABASE_NAME = " + DB_NAME);
     	    onCreate(db);
        }
    }
    
    // table columns: for note
    String[] strCols = new String[] {
          KEY_NOTE_ID,
          KEY_NOTE_TITLE,
          KEY_NOTE_PICTURE,
          KEY_NOTE_BODY,
          KEY_NOTE_MARKING,
          KEY_NOTE_CREATED
      };
    
    // table columns: for tab
    String[] strColsTab = new String[] {
            KEY_TAB_ID,
            KEY_TAB_NAME,
            KEY_TAB_TABLE_ID,
            KEY_TAB_STYLE,
            KEY_TAB_CREATED
        };
    
    //set table number
    public static void setTableNumber(String tableNum)
    {
    	mTableNum = tableNum;
        DB_TABLE_NAME = DB_TABLE_NAME_PREFIX.concat(mTableNum);
    	System.out.println("DB / _setTableNumber mTableNum=" + mTableNum);
    }  
    
    //get table number
    public static String getTableNumber()
    {
    	return mTableNum;
    }  
    
    //get table name
    public static String getTableName()
    {
    	return DB_TABLE_NAME;
    }  
    
    //get table name
    public static String getCurrentTabName()
    {
    	String tabName = null;
    	for(int i=0;i< getAllTabCount(); i++ )
    	{
    		if( Integer.valueOf(getTableNumber()) == getTabTableId(i))
    		{
    			tabName = getTabName(i);
    		}
    	}
    	return tabName;
    } 

    // select all
    public Cursor getAll() {
    	///
//    	Cursor cursor = null;
//    	try
//    	{
//    		cursor = mDb.query(DB_TABLE_NAME, 
//                strCols,
//                null, 
//                null, 
//                null, 
//                null, 
//                null  
//                );  
//    	}
//    	catch(Exception e)
//    	{
//    		while(cursor == null)
//    		{
//	        	int tblNum = Integer.valueOf(mTableNum);
//            	System.out.println("table number:" + tblNum + " is not found");
//            	System.out.println(DB_TABLE_NAME + " is not found");
//
//            	///
////        		mDb.dropTable(iTabTableId);
////        		mDb.deleteTabInfo("TAB_INFO",TabId);            	
//            	
//            	///
//	        	tblNum--;
//	        	mTableNum = String.valueOf(tblNum);
//	            DB_TABLE_NAME = DB_TABLE_NAME_PREFIX.concat(mTableNum);
//	            try
//	            {
//	            	cursor = mDb.query(DB_TABLE_NAME, 
//		                    strCols,
//		                    null, 
//		                    null, 
//		                    null, 
//		                    null, 
//		                    null  
//		                    ); 
//	            }
//	            catch(Exception ex)
//	            {
//	            	cursor = null; // still not found
//	            }
//    		}
//    	}
//		
//    	if(null == cursor)
//			System.out.println(DB_TABLE_NAME + " is not found");
//		
//    	return cursor; 
    	///
    	
        return mDb.query(DB_TABLE_NAME, 
             strCols,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    	
    }   
    
    // select all
    public Cursor getAllTab() {
        return mDb.query("TAB_INFO", 
             strColsTab,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    // insert tab
    public long insertTab(String DB_table,String tabName,long tabTableId,int tabStyle) 
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_TAB_NAME, tabName);
        args.put(KEY_TAB_TABLE_ID, tabTableId);
        args.put(KEY_TAB_STYLE, tabStyle);
        args.put(KEY_TAB_CREATED, now.getTime());
        return mDb.insert(DB_table, null, args);  
    }    
    
    // delete tab
    public long deleteTabInfo(String DB_table,int tabId) 
    { 
        return mDb.delete(DB_table, KEY_TAB_ID + "='" + tabId +"'", null);  
    }
    
    // add an entry
    //noteCreateTime: 0 will update time
    public long insert(String noteTitle,String notePicture, String noteBody, Long noteCreateTime) { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_NOTE_TITLE, noteTitle);   
        args.put(KEY_NOTE_PICTURE, notePicture);
        args.put(KEY_NOTE_BODY, noteBody);
        if(noteCreateTime == 0)
        	args.put(KEY_NOTE_CREATED, now.getTime());
        else
        	args.put(KEY_NOTE_CREATED, noteCreateTime);
        	
        args.put(KEY_NOTE_MARKING,0);
        return mDb.insert(DB_TABLE_NAME, null, args);  
    }
    
    public boolean delete(long rowId) {  
        return mDb.delete(DB_TABLE_NAME, KEY_NOTE_ID + "=" + rowId, null) > 0;
    }
    
    
    //query single tab info
    public Cursor get(String DB_table, long tabId) throws SQLException 
    {  
        Cursor mCursor = mDb.query(true,
        							DB_table,
					                new String[] {KEY_TAB_ID,
        										  KEY_TAB_NAME,
        										  KEY_TAB_TABLE_ID,
        										  KEY_TAB_STYLE,
        										  KEY_TAB_CREATED},
					                KEY_TAB_ID + "=" + tabId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    
    //query single entry
    public Cursor get(long rowId) throws SQLException 
    {  
        Cursor mCursor = mDb.query(true,
					                DB_TABLE_NAME,
					                new String[] {KEY_NOTE_ID,
				  								  KEY_NOTE_TITLE,
				  								  KEY_NOTE_PICTURE,
        										  KEY_NOTE_BODY,
        										  KEY_NOTE_MARKING,
        										  KEY_NOTE_CREATED},
					                KEY_NOTE_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    //update new table of tab info
    public boolean updateTabInfo(long tabId, String tabName, long table_id, int tabStyle) { 
        ContentValues args = new ContentValues();
        Date now = new Date(); 
        args.put(KEY_TAB_NAME, tabName);
        args.put(KEY_TAB_TABLE_ID, table_id);
        args.put(KEY_TAB_STYLE, tabStyle);
        args.put(KEY_TAB_CREATED, now.getTime());
        DB_TABLE_NAME = "TAB_INFO";
        return mDb.update(DB_TABLE_NAME, args, KEY_TAB_ID + "=" + tabId, null) > 0;
    }
    
    //update
    //argTime:  0 for Don't update time
    public boolean updateNote(long rowId, String noteTitle, String notePicture, String noteBody, long marking, long argTime) { 
        ContentValues args = new ContentValues();
        args.put(KEY_NOTE_TITLE, noteTitle);
        args.put(KEY_NOTE_PICTURE, notePicture);
        args.put(KEY_NOTE_BODY, noteBody);
        args.put(KEY_NOTE_MARKING, marking);
        
        if(argTime == 0)
        	args.put(KEY_NOTE_CREATED, mNotesCursor.getLong(mNotesCursor.getColumnIndex(KEY_NOTE_CREATED)));
        else
        	args.put(KEY_NOTE_CREATED, argTime);

//      return mDb.update(DB_TABLE_NAME, args, KEY_NOTE_ID + "=" + rowId, null) > 0;
        int cUpdateItems = mDb.update(DB_TABLE_NAME, args, KEY_NOTE_ID + "=" + rowId, null);
//        System.out.println("- cUpdateItems = " + cUpdateItems);
        return cUpdateItems > 0;
    }
    
    //insert new table
    public void insertNewTable(int tableId)
    {   
    	{
    		//format "notesTable1"
        	DB_TABLE_NAME = DB_TABLE_NAME_PREFIX.concat(String.valueOf(tableId));
            String dB_insert_table = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME + "(" +
            							KEY_NOTE_ID + " INTEGER PRIMARY KEY," +
            							KEY_NOTE_TITLE + " TEXT," +  
            							KEY_NOTE_PICTURE + " TEXT," +  
            							KEY_NOTE_BODY + " TEXT," +
            							KEY_NOTE_MARKING + " INTEGER," +
            							KEY_NOTE_CREATED + " INTEGER);";
            mDb.execSQL(dB_insert_table);         
    	}
    }
    

    
    //delete table
    public void dropTable(int tableId)
    {   
    	{
    		//format "notesTable1"
        	DB_TABLE_NAME = DB_TABLE_NAME_PREFIX.concat(String.valueOf(tableId));
            String dB_drop_table = "DROP TABLE IF EXISTS " + DB_TABLE_NAME + ";";
            mDb.execSQL(dB_drop_table);         
    	}
    }

	public int getMaxId() {
		Cursor cursor = this.getAll();
		int total = cursor.getColumnCount();
		int iMax =1;
		int iTemp = 1;
		for(int i=0;i< total;i++)
		{
			cursor.moveToPosition(i);
			iTemp = cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID));
			iMax = (iTemp >= iMax)? iTemp: iMax;
		}
		return iMax;
	}
	
	public static void deleteDB()
	{
        mDb = mDbHelper.getWritableDatabase();//????????????????
        try {
	    	mDb.beginTransaction();
	        mContext.deleteDatabase(DB_NAME);
	        mDb.setTransactionSuccessful();
	    }
	    catch (Exception e) {
	    }
	    finally {
	    	Toast.makeText(mContext,R.string.config_delete_DB_toast,Toast.LENGTH_SHORT).show();
	    	mDb.endTransaction();
	    }
	}
	
	// for DB
	public static Cursor mTabCursor;
	public Cursor mNotesCursor;
	
	/*
	 * DB functions for tab
	 * 
	 */
	void doOpen() {
		this.open();
		mTabCursor = this.getAllTab();
		mNotesCursor = this.getAll();
	}
	
	static int getAllTabCount()	{
		return mTabCursor.getCount();
	}
	
	int getTabId(int position) {
		mTabCursor.moveToPosition(position);
        return mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_ID));
	}
	
	static String getTabName(int position) {
		mTabCursor.moveToPosition(position);
        return mTabCursor.getString(mTabCursor.getColumnIndex(KEY_TAB_NAME));
	}
	
	static int getTabTableId(int position)	{
		mTabCursor.moveToPosition(position);
        return mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_TABLE_ID));
	}
	
	int getTabStyle(int position)	{
		mTabCursor.moveToPosition(position);
        return mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_STYLE));
	}
	
	void doClose()	{
		this.close();
	}
	
	int getAllCount()
	{
		return mNotesCursor.getCount();
	}
	
	String getNoteTitle(int position)
	{
		mNotesCursor.moveToPosition(position);
        return mNotesCursor.getString(mNotesCursor.getColumnIndex(KEY_NOTE_TITLE));
	}
	
	String getNotePictureString(int position)
	{
		mNotesCursor.moveToPosition(position);
        return mNotesCursor.getString(mNotesCursor.getColumnIndex(KEY_NOTE_PICTURE));
	}
	
	String getNoteBodyString(int position)
	{
		mNotesCursor.moveToPosition(position);
        return mNotesCursor.getString(mNotesCursor.getColumnIndex(KEY_NOTE_BODY));
	}
	
	Long getNoteId(int position)
	{
		mNotesCursor.moveToPosition(position);
        return (long) mNotesCursor.getInt(mNotesCursor.getColumnIndex(KEY_NOTE_ID));
	}
	
	int getNoteMarking(int position)
	{
		mNotesCursor.moveToPosition(position);
		return mNotesCursor.getInt(mNotesCursor.getColumnIndex(KEY_NOTE_MARKING));
	}
	
	Long getNoteCreateTime(int position)
	{
		mNotesCursor.moveToPosition(position);
		return mNotesCursor.getLong(mNotesCursor.getColumnIndex(KEY_NOTE_CREATED));
	}
	
	
	int getCheckedItemsCount()
	{
		int cCheck =0;
		for(int i=0;i< getAllCount() ;i++)
		{
			if(getNoteMarking(i) == 1)
				cCheck++;
		}
		return cCheck;
	}

	public String getNotePictureStringById(Long mRowId) {
        Cursor noteCursor = get(mRowId);
		String pictureFileName = noteCursor.getString(noteCursor.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE));
		return pictureFileName;
	}
	
	public String getNoteTitleStringById(Long mRowId) {
		return get(mRowId).getString(get(mRowId).getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
	}
	
	public String getNoteBodyStringById(Long mRowId) {
		return  get(mRowId).getString(get(mRowId).getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
	}
	
	public Long getNoteMarkingById(Long mRowId) {
		return  get(mRowId).getLong(get(mRowId).getColumnIndexOrThrow(DB.KEY_NOTE_MARKING));
	}

	public Long getNoteCreatedTimeById(Long mRowId) {
		return  get(mRowId).getLong(get(mRowId).getColumnIndexOrThrow(DB.KEY_NOTE_CREATED));
	}

}