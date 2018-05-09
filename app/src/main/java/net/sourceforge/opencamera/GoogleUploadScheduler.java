package net.sourceforge.opencamera;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by evansu on 3/19/18.
 * This class provides automatic scheduling of upload request on top of GoogleDriveUploader. It
 * only submit upload job when wifi is available.
 */

public class GoogleUploadScheduler {
	private static final String TAG = GoogleUploadScheduler.class.getSimpleName();
	public static final String FILE_META_KEY
			= BuildConfig.APPLICATION_ID + ".FILE_META_KEY";
	public static final String FILE_PATH_KEY =
			BuildConfig.APPLICATION_ID + ".FILE_PATH_KEY";

	private static GoogleUploadScheduler mInstance;
	private static Context mCtx;
	private static ComponentName mServiceComponent;
	private int mJobId = 0;
	private GoogleUploadScheduler(Context context) {
		mCtx = context.getApplicationContext();
		Intent startServiceIntent = new Intent(mCtx, GoogleDriveUploadService.class);
		mServiceComponent = new ComponentName(mCtx, GoogleDriveUploadService.class);
		mCtx.startService(startServiceIntent);
	}
	public static synchronized void initialize(Context context){
		if (mInstance == null) {
			mInstance = new GoogleUploadScheduler(context);
		}
	}
	public static synchronized GoogleUploadScheduler getInstance(Context context){
		if (mInstance == null) {
			mInstance = new GoogleUploadScheduler(context);
		}
		return mInstance;
	}
	public static synchronized GoogleUploadScheduler getInstance() throws NullPointerException {
		if (mInstance == null) {
			throw new NullPointerException("GoogleUploadScheduler instance is not initialized. ");
		}
		return mInstance;
	}
	public void scheduleUpload(final String filePath, final Map<String, String> metaData){
		JobInfo.Builder builder = new JobInfo.Builder(mJobId++, mServiceComponent);
		builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
		PersistableBundle extras = new PersistableBundle();
		extras.putString(FILE_PATH_KEY, filePath);
		extras.putString(FILE_META_KEY, new JSONObject(metaData).toString());

		builder.setExtras(extras);
		Log.d(TAG, "Scheduling job");
		JobScheduler tm = (JobScheduler) mCtx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		tm.schedule(builder.build());

	}


}
