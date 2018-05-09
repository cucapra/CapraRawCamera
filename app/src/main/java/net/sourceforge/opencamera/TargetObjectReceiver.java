package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * Created by evansu on 4/11/18.
 * A singleton class that downloads target object text file and displays it on the camera user interface.
 * 
 */

public class TargetObjectReceiver {
	private static final String TAG = "TargetObjectReceiver";
	private static TargetObjectReceiver mInstance;
	private static final String url = "https://raw.githubusercontent.com/cucapra/CapraRawCamera/master/target_object.txt";
	private static Context mCtx;
	private String settings;
	private int refresh_counter = 0;
	private static AtomicBoolean request_sent;
	private TargetObjectReceiver(Context context){
		mCtx = context;
	}
	private static ConnectivityManager cm;

	public static synchronized TargetObjectReceiver getInstance(Context context){
		if(mInstance == null) {
			mInstance = new TargetObjectReceiver(context);
			cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			request_sent = new AtomicBoolean(false);
		}
		return mInstance;
	}
	public int drawTargets(MyApplicationInterface applicationInterface, Canvas canvas, Paint p, int location_x, int location_y, int ui_rotation) {
		int height = 0;
		refresh_counter += 1;
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
				activeNetwork.isConnectedOrConnecting();

		if ((settings == null && !request_sent.get() && isConnected) || refresh_counter > 6000 ) {
			refresh_counter = 0;
			StringRequest tokenRequest = new StringRequest(Request.Method.GET, url,
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							// Display the first 500 characters of the response string.
							Log.d(TAG, "onResponse: " + response);
							settings = response;
							request_sent.set(false);
						}
					}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					if (error.networkResponse != null) {
						int statusCode = error.networkResponse.statusCode;
						NetworkResponse response = error.networkResponse;
						Log.d(TAG, "" + statusCode + " " + response.data);
					} else {
						Log.d(TAG, "onErrorResponse: networkResponse is null");
					}
					request_sent.set(false);
				}
			});
			RequestQueue queue = VolleyQueue.getInstance(mCtx).getRequestQueue();
			request_sent.set(true);
			queue.add(tokenRequest);

		}
		if(settings != null) {
			Rect bounds = new Rect();
			p.getTextBounds(settings, 0, settings.length(), bounds);
			height = applicationInterface.drawTextWithBackground(canvas, p, settings, Color.WHITE, Color.BLACK, location_x, location_y, MyApplicationInterface.Alignment.ALIGNMENT_TOP, null, true, bounds);

		}
		return height;
	}
}
