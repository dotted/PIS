package dk.bigherman.android.pisviewer;

import org.json.JSONObject;

import com.google.android.gms.maps.model.Marker;

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
