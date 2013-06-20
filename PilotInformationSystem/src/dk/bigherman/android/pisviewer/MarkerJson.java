package dk.bigherman.android.pisviewer;

import org.json.JSONObject;

import pl.mg6.android.maps.extensions.Marker;

public class MarkerJson {
	private JSONObject jsonObject;
	private Marker marker;
	
	public JSONObject getJsonObject()
	{
		return jsonObject;
	}
	public void setJsonObject(JSONObject value)
	{
		jsonObject = value;
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
