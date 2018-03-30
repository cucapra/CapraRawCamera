package net.sourceforge.opencamera;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by evansu on 3/29/18.
 */

public class FileDescriptionGenerator {
	private static FileDescriptionGenerator mInstance;
	private static Context mCtx;


	private String description;
	private FileDescriptionGenerator(Context context)  {
		mCtx = context.getApplicationContext();
		Map<String, String> descriptionMap;
		descriptionMap = new HashMap<>();
		descriptionMap.put("VERSION.SDK", android.os.Build.VERSION.RELEASE);
		descriptionMap.put("DEVICE", android.os.Build.DEVICE);
		descriptionMap.put("MODEL", android.os.Build.MODEL);
		descriptionMap.put("PRODUCT", android.os.Build.PRODUCT);
		descriptionMap.put("GUID", UUID.randomUUID().toString());
		description = new JSONObject(descriptionMap).toString();
	}

	public static synchronized FileDescriptionGenerator getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new FileDescriptionGenerator(context);
		}
		return mInstance;
	}
	public static synchronized void initialize(Context context){
		if (mInstance == null) {
			mInstance = new FileDescriptionGenerator(context);
		}
	}
	public static FileDescriptionGenerator getInstance() throws NullPointerException{
		if(mInstance == null) {
			throw new NullPointerException("Google drive instance is not initialized. ");
		}
		return mInstance;
	}
	public String getDescription(){
		return description;
	}
}
