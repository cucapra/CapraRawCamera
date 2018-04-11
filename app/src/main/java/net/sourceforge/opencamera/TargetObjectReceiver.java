package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;



/**
 * Created by evansu on 4/11/18.
 * A singleton class that fetch target object list on request
 */

public class TargetObjectReceiver {
	private static final String TAG = "TargetObjectReceiver";
	private static TargetObjectReceiver mInstance;
	private static final String url = "https://raw.githubusercontent.com/Po-Hsun-Su/CapraRawCameraSettings/master/settings.txt";
	private static Context mCtx;
	private String settings;
	private int refresh_counter = 0;
	private TargetObjectReceiver(Context context){
		mCtx = context;
	}

	public static synchronized TargetObjectReceiver getInstance(Context context){
		if(mInstance == null) {
			mInstance = new TargetObjectReceiver(context);
		}
		return mInstance;
	}
	public int drawTargets(MyApplicationInterface applicationInterface, Canvas canvas, Paint p, int location_x, int location_y, int ui_rotation) {
		int height = 0;
		refresh_counter += 1;
		if (settings == null || refresh_counter > 60) {
			refresh_counter = 0;
			StringRequest tokenRequest = new StringRequest(Request.Method.GET, url,
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							// Display the first 500 characters of the response string.
							Log.d(TAG, "onResponse: " + response);
							settings = response;
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
				}
			});
			RequestQueue queue = VolleyQueue.getInstance(mCtx).getRequestQueue();
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
