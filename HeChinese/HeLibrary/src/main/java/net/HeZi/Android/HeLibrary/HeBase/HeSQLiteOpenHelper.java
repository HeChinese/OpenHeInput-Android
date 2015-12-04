/*
  * Copyright (c) 2015 Guilin Ouyang. All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package net.HeZi.Android.HeLibrary.HeBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class HeSQLiteOpenHelper extends SQLiteOpenHelper{

	private final Context mContext;
    private String mydb_name;	// = "hema_db.sqlite"
	//destination path (location) of our database on device
    private String mydb_path_and_name_in_system = "";    // /data/data/net.HeZi.Android.HeInput/databases/hema_db.sqlite

	private static String TAG = "HeSQLiteOpenHelper"; // Tag just for the LogCat window
	private SQLiteDatabase mDatabase;

	/*
	 * public SQLiteOpenHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version)

		Added in API level 1
		Create a helper object to create, open, and/or manage a database. This method always returns very quickly. 
		The database is not actually created or opened 
		until one of getWritableDatabase() or getReadableDatabase() is called.

		Parameters
			context	to use to open or create the database
			name	of the database file, or null for an in-memory database
			factory	to use for creating cursor objects, or null for the default
			version	number of the database (starting at 1); if the database is older, 
				onUpgrade(SQLiteDatabase, int, int) will be used to upgrade the database; 
				if the database is newer, onDowngrade(SQLiteDatabase, int, int) will be used to downgrade the database
	 */
	/*
	 * Called when the database needs to be upgraded. 
	 * The implementation should use this method to drop tables, add tables, 
	 * or do anything else it needs to upgrade to the new schema version
	 */

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
		Log.d(TAG,"Update database...");
        try {
            copyDatabase();
        } catch (IOException e) {
            throw new Error("Error copying database");
        }
	}
    
    @Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
        try {
            copyDatabase();
        } catch (IOException e) {
            throw new Error("Error copying database");
        }
	}

    public HeSQLiteOpenHelper(Context cxt, String db_name, int dbVersion)
    {
    	super(cxt, db_name, null, dbVersion);
    	this.mContext = cxt;
    	this.mydb_name = db_name;

		// This is just get path and name, it may not exist in the system, will check later
    	mydb_path_and_name_in_system = cxt.getDatabasePath(db_name).getAbsolutePath();
		Log.d(TAG, "my database location: "+ mydb_path_and_name_in_system);
    }
        
  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDatabase() throws IOException
    {
		//If database not exists copy it from the assets
    	boolean dbExist = checkDatabase();
    	if(dbExist)
    	{
    		//do nothing - database already exist
    		//this.getWritableDatabase();
    		//this.getReadableDatabase();
    	}
    	else
    	{
			// not really know why need these two statements
			this.getReadableDatabase();
			this.close();
     		//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
         	try
         	{
     			copyDatabase();
     		} 
         	catch (IOException e) 
         	{
         		throw new Error("Error copying database");
         	}
    	}
     }

	//Check that the database exists here: /data/data/your package/databases/DB Name
	private boolean checkDatabase()
	{
		File dbFile = new File(mydb_path_and_name_in_system);
		Log.v("dbFile", dbFile + "   " + dbFile.exists());
		return dbFile.exists();
	}

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDatabase() throws IOException{
     	
    	//Open your local db as the input stream
    	InputStream myInput = mContext.getAssets().open(mydb_name);//"hema_db.sqlite");
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(mydb_path_and_name_in_system);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0)
    	{
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close(); 
    }

	//Open the database, so we can query it
	public boolean openDatabase() throws SQLException
	{
        mDatabase =  SQLiteDatabase.openDatabase(mydb_path_and_name_in_system, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		//mDatabase = SQLiteDatabase.openDatabase(mydb_path_and_name_in_system, null, SQLiteDatabase.CREATE_IF_NECESSARY);
		return mDatabase != null;
    }

    @Override
	public synchronized void close() 
    {
		if(mDatabase != null)
			mDatabase.close();
		super.close();
	}
}