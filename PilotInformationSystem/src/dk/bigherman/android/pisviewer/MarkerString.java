package dk.bigherman.android.pisviewer;

import com.google.android.gms.maps.model.Marker;

public class MarkerString {
	private String string;
	private Marker marker;
	
	public String getString()
	{
		return string;
	}
	public void setString(String value)
	{
		string = value;
	}
	
	public Marker getMarker()
	{
		return marker;
	}
	public void setMarker(Marker value)
	{
		marker = value;
	}

}
