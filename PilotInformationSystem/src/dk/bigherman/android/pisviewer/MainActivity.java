package dk.bigherman.android.pisviewer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.util.Log;

import android.support.v4.app.FragmentActivity;
 
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dk.bigherman.android.pisviewer.Airfield;
import dk.bigherman.android.pisviewer.DataBaseHelper;

public class MainActivity extends FragmentActivity 
{
	GoogleMap gMap;
	String serverIP = "";
	DataBaseHelper myDbHelper;
	//private enum Colour{BLU, WHT, GRN, YLO, AMB, RED, BLK, NIL};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
			        new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myDbHelper = new DataBaseHelper(this.getApplicationContext());
		try 
		{
			// To do, rewrite it ALL
			myDbHelper.createDataBase();
		}
		catch (IOException ioe)
		{
			throw new Error("Unable to create database");
		}
		catch(SQLException sqle)
		{
			throw sqle;
		}
		
		serverIP = getResources().getString(R.string.server_ip);
		
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        // Showing status
        if(status!=ConnectionResult.SUCCESS)
        { 	// Google Play Services are not available
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        }
        else
        {
        	// Google Play Services are available
 
            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            gMap = fm.getMap();
        	// Creating a LatLng object for the current location (somewhere near Aarhus! :-))
            LatLng latLng = new LatLng(56.0, 10.3);
     
            // Showing the current location in Google Map
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
     
            // Zoom in the Google Map at a level where all (most) of Denmark will be visible
            gMap.animateCamera(CameraUpdateFactory.zoomTo(6));
            
            loadMarkersTask loader = new loadMarkersTask();
            loader.execute(latLng);
//            
        }
		myDbHelper = new DataBaseHelper(this.getApplicationContext());
		try 
		{
			// To do, rewrite it ALL
			myDbHelper.createDataBase();
		}
		catch (IOException ioe)
		{
			throw new Error("Unable to create database");
		}
		catch(SQLException sqle)
		{
			throw sqle;
		}
		
		serverIP = getResources().getString(R.string.server_ip);
	}
	
	public boolean onOptionsItemSelected (MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_settings:
				final AlertDialog.Builder alert = new AlertDialog.Builder(this);
				
				alert.setTitle("Set Server IP Address");
				alert.setIcon(R.drawable.setserver_inverse);
				
			    final EditText input = new EditText(this);
			    alert.setView(input);
			    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() 
			    {
			        public void onClick(DialogInterface dialog, int whichButton) 
			        {
			            String value = input.getText().toString().trim();
			            //Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
			            serverIP = value;
			        }
			    });

			    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			    {
			        public void onClick(DialogInterface dialog, int whichButton)
			        {
			            dialog.cancel();
			        }
			    });
			    alert.show();
				break;
		}
		return true;
	}
	
	private void hideOSDKeyboard(View view)
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private void moveCameraToIcao(String icaoCode)
	{
		Log.i("airfields", "Start db load");
		myDbHelper.openDataBase();
        LatLng mapCentre = myDbHelper.icaoToLatLng(icaoCode);
	 	myDbHelper.close();
        gMap.moveCamera(CameraUpdateFactory.newLatLng(mapCentre));

	}
	
	private void showMetarText(JSONObject metarJson)
	{
    	String metar = "";
		try {
			metar = metarJson.getString("report");
		} catch (JSONException e) {
			e.printStackTrace();
		}    
		TextView textMetar = (TextView) findViewById(R.id.text_metar);
		textMetar.setText(metar);
	}
	
	public void showMetar(View view)
	{
    	Log.i("Test", "Hide OSD Keyboard");
		hideOSDKeyboard(view);
		EditText icaoText = (EditText) findViewById(R.id.edit_icao);
    	String icaoCode = icaoText.getText().toString().toUpperCase();
    	
    	//Validate ICAO code.
    	Log.i("Test", "Validate ICAO");
    	boolean flag = CommonMethods.validateIcao(icaoCode, "^[A-Z]{4}$", myDbHelper);
    	// If invalid show error message and return
    	if (!flag)
    	{
	    	Toast.makeText(getApplicationContext(), "Invalid ICAO code", Toast.LENGTH_LONG).show();
    		return;
    	}
    	Log.i("Test", "Move Camera");
		moveCameraToIcao(icaoCode);

   		//Replace with some background thread
    	String readMetarFeed = CommonMethods.getJson("http://" + serverIP + "/test_json.php?icao=" + icaoCode);
    	
    	try {
	    	JSONObject jsonObject = new JSONObject(readMetarFeed);
	    	
	    	//Log.d(MainActivity.class.getName(), jsonObject.getString("icao"));
	    	//Log.d(MainActivity.class.getName(), jsonObject.getString("time"));
	    	//Log.d(MainActivity.class.getName(), jsonObject.getString("report"));
	    	//Show metar information in whitespace
	    	Log.i("Test", "Show Metar Information");
	    	showMetarText(jsonObject);

	    	
        } catch (JSONException e) {        	
        	e.printStackTrace();
    		Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    } 
	
	private void drawMapMarkers(List<MarkerOptions> markersOpt)
	{
		for (MarkerOptions markerOpt : markersOpt) 
		{
			gMap.addMarker(markerOpt);
		}
	}

	private List<MarkerOptions> makeListMarkersMetarInformation(ArrayList<Airfield> airfields)
	{
		List<Marker> markers = new ArrayList<Marker>();

		List<MarkerOptions> markersOpt = new ArrayList<MarkerOptions>();
		String colour = "";
		JSONObject metarJson = new JSONObject();
		
		int icon_state=R.drawable.icn_empty;
                  
        for (int i=0; i<airfields.size();i++)
        {
        	String readMetarFeed = CommonMethods.getJson("http://" + serverIP + "/test_json.php?icao=" + airfields.get(i).getIcaoCode());
        	Log.i("airfields", airfields.get(i).getIcaoCode());
        	
        	if(readMetarFeed != "")
        	{
        		Log.i("airfields", readMetarFeed);
        		
    	    	try {
            		metarJson = new JSONObject(readMetarFeed);
					colour = metarJson.getString("colour");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    	if (colour.contentEquals("BLU"))
    	    	{
    	    		icon_state=R.drawable.icn_blue;
    	    	}
    	    	else if (colour.contentEquals("WHT"))
    	    	{
    	    		icon_state=R.drawable.icn_white;
    	    	}
    	    	else if (colour.contentEquals("GRN"))
    	    	{
    	    		icon_state=R.drawable.icn_green;
    	    	}
    	    	else if (colour.contentEquals("YLO"))
    	    	{
    	    		icon_state=R.drawable.icn_yellow;
    	    	}
    	    	else if (colour.contentEquals("AMB"))
    	    	{
    	    		icon_state=R.drawable.icn_amber;
    	    	}
    	    	else if (colour.contentEquals("RED"))
    	    	{
    	    		icon_state=R.drawable.icn_red;
    	    	}
    	    	else if (colour.contentEquals("NIL"))
    	    	{
    	    		icon_state=R.drawable.icn_empty;
    	    	}
        	}
        		try {
					markersOpt.add(new MarkerOptions().position(new LatLng(airfields.get(i).getLat(), airfields.get(i).getLng()))
					.title(airfields.get(i).getName())
					.snippet(metarJson.getString("report"))
					.icon(BitmapDescriptorFactory.fromResource(icon_state)));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
        return markersOpt;		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private class loadMarkersTask extends AsyncTask<LatLng, Void, List<MarkerOptions>> 
	{

		@Override
		protected List<MarkerOptions> doInBackground(LatLng... params) {
	    	List<MarkerOptions> markersOpt;
	    	LatLng latLng = params[0];
            LatLngBounds mapBounds = new LatLngBounds(new LatLng(latLng.latitude-3.0, latLng.longitude-(2.5/Math.cos(latLng.latitude*Math.PI/180))), new LatLng(latLng.latitude+3.0, latLng.longitude+(2.5/Math.cos(latLng.latitude*Math.PI/180))));
          
            myDbHelper.openDataBase();
    	 	ArrayList<Airfield> airfields = myDbHelper.airfieldsInArea(mapBounds);
    	 	myDbHelper.close();
	    	Log.i("Test", "Make list with markers");
	    	markersOpt = makeListMarkersMetarInformation(airfields);
			return markersOpt;
		}

		@Override
		protected void onPostExecute(List<MarkerOptions> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
	    	Log.i("Test", "Draw markers");
			drawMapMarkers(result);			
		}		
		
	}
}	
