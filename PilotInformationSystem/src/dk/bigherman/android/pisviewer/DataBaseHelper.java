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
	private String DB_PATH; 
	private static String DB_NAME = "airfields.rdb";

	private SQLiteDatabase myDataBase; 

	private Context myContext;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) 
	{
		super(context, DB_NAME, null, 1);
		this.myContext = context;

		DB_PATH = myContext.getFilesDir().getPath().toString();
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * 
	 */
	public void createDataBaseIfNotExists() throws IOException
	{
		boolean dbExist = checkDataBase();

		if(!dbExist)
		{
			//By calling this method an empty database will be created into the default system path
			//of your application so we are going to be able to overwrite that database with our database.
			this.getReadableDatabase();

			try
			{
				copyDataBase();
			}
			catch (IOException e)
			{
				throw new Error("Error copying database");
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase()
	{
		SQLiteDatabase checkDB = null;

		try
		{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
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
	private void copyDataBase() throws IOException
	{
		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ( (length = myInput.read(buffer)) > 0)
		{
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException
	{
		//Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	}

	@Override
	public void close()
	{
		if(myDataBase != null)
		{
			myDataBase.close();
		}
		super.close();
		this.myContext = null;
		this.myDataBase = null;
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

		Cursor myCursor = myDataBase.rawQuery("SELECT name FROM airfields WHERE icao ='" + icaoCode +"'", null);

		myCursor.moveToFirst();
		airfieldName = myCursor.getString(0);

		myCursor.close();

		return airfieldName;
	}

	public LatLng icaoToLatLng(String icaoCode)
	{
		LatLng latLng;

		Cursor myCursor = myDataBase.rawQuery("SELECT lat, long FROM airfields WHERE icao ='" + icaoCode +"'", null);
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

		Cursor myCursor = myDataBase.rawQuery("SELECT icao, lat, long, name FROM airfields "
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
		Cursor myCursor = myDataBase.rawQuery("SELECT icao FROM airfields WHERE icao = '" + icao + "'", null);
		if (myCursor.getCount() > 0)
			return true;
		return false;
	}
}
