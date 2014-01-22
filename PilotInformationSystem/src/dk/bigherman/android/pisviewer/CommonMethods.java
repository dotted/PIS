package dk.bigherman.android.pisviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


import android.util.Log;

public final class CommonMethods
{
	private static boolean validateStringWithRegex(String icao, String regexPattern)
	{
		Pattern r = Pattern.compile(regexPattern);
		Matcher m = r.matcher(icao);
		if (m.find())
		{
			return true;
		}
		return false;
	}

	public static boolean validateIcao(String icao, DataBaseHelper dbHelper)
	{
		Boolean icaoIsValiated = false;
		if (validateStringWithRegex(icao, "^[A-Z]{4}$"))
		{
			dbHelper.open();
			icaoIsValiated = dbHelper.validateIcaoWithDb(icao);
			dbHelper.close();
		}
		return icaoIsValiated;
	}

	public static String getJson(String url) 
	{
		int tries = 1;
		int maxTries = 5;
		//String metarURL = "http://duku.no-ip.info/pis/android/jason.php?i=" + icaoCode;

		//String metarURL = "http://" + serverIP + "/test_json.php?icao="
		//		+ icaoCode;
		
		StringBuilder builder = new StringBuilder();
		HttpGet httpGet = new HttpGet(url);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 5000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams
		.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		client.setParams(httpParameters);
		do {
			try {
				Log.i("getJson", "download attempt #" + tries);
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					Log.e(MainActivity.class.toString(),
							"Failed to download file");
				}
				break;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				//throw new RuntimeException(
						//"Error connecting to server - check IP address.  Use Settings menu to fix this");
			} catch (ConnectTimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				//throw new RuntimeException(
						//"Error connecting to server - check IP address.  Use Settings menu to fix this");
			} finally {
				tries++;
				try {
					Thread.sleep(10000*tries);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} while (tries <= 5);
		
		if (builder != null) {
			return builder.toString();
		}
		else
		{
			String empty = "";
			return empty;
		}
		
		
	}
}