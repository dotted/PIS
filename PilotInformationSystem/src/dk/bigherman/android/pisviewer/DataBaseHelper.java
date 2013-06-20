package dk.bigherman.android.pisviewer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper
{		 
	//The Android's default system path of your application database.
	//private static String DB_PATH = "/data/data/dk.bigherman.android.pisviewer/databases/";
	private String dbPath; 
	private static String dbName = "airfields.rdb";

	private SQLiteDatabase database; 

	private Context context;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) 
	{
		super(context, dbName, null, 1);
		this.context = context;

		dbPath = context.getFilesDir().getPath().toString();
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * 
	 */
	public void create() throws IOException
	{
		//By calling this method an empty database will be created into the default system path
		//of your application so we are going to be able to overwrite that database with our database.
		this.getReadableDatabase();

		try
		{
			copy(dbName, dbPath + dbName);
		}
		catch (IOException e)
		{
			throw new Error("Error copying database");
		}
	}
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	public boolean isCreated()
	{
		return isCreated(dbPath + dbName);
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	public boolean isCreated(String databasePath)
	{
		SQLiteDatabase database = null;

		try
		{
			database = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);
			database.close();
			return true;
		}
		catch(SQLiteException e)
		{
			//database doesn't exist yet.
			return false;
		}
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copy() throws IOException
	{
		copy(dbName, dbPath + dbName);
	}
	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copy(String inputFilename, String outputFilepath) throws IOException
	{
		//Open your local db as the input stream
		InputStream input = context.getAssets().open(inputFilename);
		
		//Open the empty db as the output stream
		OutputStream output = new FileOutputStream(outputFilepath);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ( (length = input.read(buffer)) > 0)
		{
			output.write(buffer, 0, length);
		}

		//Close the streams
		output.flush();
		output.close();
		input.close();
	}

	public void open() throws SQLException
	{
		open(dbPath + dbName);
	}
	
	public void open(String databaseFilepath) throws SQLException
	{
		//Open the database
		database = SQLiteDatabase.openDatabase(databaseFilepath, null, SQLiteDatabase.OPEN_READONLY);
	}
	
	

	@Override
	public void close()
	{
		if(database != null)
		{
			database.close();
		}
		super.close();
		this.context = null;
		this.database = null;
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{

	}

	// Add your public helper methods to access and get content from the database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
	// to you to create adapters for your views.

	public String airfieldName(String icaoCode)
	{
		String airfieldName;

		Cursor myCursor = database.rawQuery("SELECT name FROM airfields WHERE icao ='" + icaoCode +"'", null);

		myCursor.moveToFirst();
		airfieldName = myCursor.getString(0);

		myCursor.close();

		return airfieldName;
	}

	public LatLng icaoToLatLng(String icaoCode)
	{
		LatLng latLng;

		Cursor myCursor = database.rawQuery("SELECT lat, long FROM airfields WHERE icao ='" + icaoCode +"'", null);
		myCursor.moveToFirst();

		latLng = new LatLng(myCursor.getFloat(0), myCursor.getFloat(1));

		return latLng;
	}

	public ArrayList<Airfield> airfieldsInArea(LatLngBounds mapBounds)
	{
		ArrayList<Airfield> airfieldsInArea = new ArrayList<Airfield>();
		Airfield airfield;

		Log.i("airfieldsInArea", "SELECT icao, lat, long, name FROM airfields "
				+ "WHERE lat >=" + mapBounds.southwest.latitude +" AND lat<=" + mapBounds.northeast.latitude + " AND long>=" + mapBounds.southwest.longitude + " AND long<=" + mapBounds.northeast.longitude);

		Cursor myCursor = database.rawQuery("SELECT icao, lat, long, name FROM airfields "
				+ "WHERE lat >=" + mapBounds.southwest.latitude +" AND lat<=" + mapBounds.northeast.latitude + " AND long>=" + mapBounds.southwest.longitude + " AND long<=" + mapBounds.northeast.longitude, null);

		myCursor.moveToNext();

		while (!myCursor.isAfterLast()) {
			airfield = new Airfield (myCursor.getString(0), myCursor.getDouble(1), myCursor.getDouble(2), myCursor.getString(3));
			airfieldsInArea.add(airfield);
			myCursor.moveToNext();
		}

		myCursor.close();

		Log.i("airfieldsInArea", "Rows: " + airfieldsInArea.size());

		return airfieldsInArea;
	}

	public boolean validateIcaoWithDb(String icao)
	{
		Log.i("validateIcao", "SELECT icao FROM airfields WHERE icao = '" + icao + "'");
		Cursor myCursor = database.rawQuery("SELECT icao FROM airfields WHERE icao = '" + icao + "'", null);
		if (myCursor.getCount() > 0)
			return true;
		return false;
	}
}
