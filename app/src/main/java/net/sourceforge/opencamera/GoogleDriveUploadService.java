package net.sourceforge.opencamera;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import static net.sourceforge.opencamera.GoogleUploadScheduler.FILE_META_KEY;
import static net.sourceforge.opencamera.GoogleUploadScheduler.FILE_PATH_KEY;


/**
 * Created by evansu on 3/19/18.
 */

public class GoogleDriveUploadService extends JobService {
	private static final String TAG = GoogleDriveUploadService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "Service created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.i(TAG, "Service destroyed");
	}

	/**
	 * When the app's MainActivity is created, it starts this service. This is so that the
	 * activity and this service can communicate back and forth. See "setUiCallback()"
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public boolean onStartJob(final JobParameters params) {

		String filePath = params.getExtras().getString(FILE_PATH_KEY);
		String fileMeta = params.getExtras().getString(FILE_META_KEY);


		// Uses a handler to delay the execution of jobFinished().
		UploadServiceBroadcastReceiver broadcastReceiver = new UploadServiceBroadcastReceiver() {
			@Override
			public void onProgress(Context context, UploadInfo uploadInfo) {
				// your implementation

			}

			@Override
			public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
				// your implementation
				Log.d(TAG, "onError: " +  uploadInfo.toString());
				if(serverResponse != null){
					Log.d(TAG, "onError: serverResponse " +  serverResponse.toString());
				}
				jobFinished(params, false);
				unregister(context);
			}

			@Override
			public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
				// your implementation
				Log.d(TAG, "onCompleted: uploadInfo " +  uploadInfo.toString());
				if(serverResponse != null){
					Log.d(TAG, "onCompleted: serverResponse " +  serverResponse.toString());
				}
				jobFinished(params, false);
				unregister(context);
			}

			@Override
			public void onCancelled(Context context, UploadInfo uploadInfo) {
				// your implementation
				jobFinished(params, false);
				unregister(context);
			}
		};
		broadcastReceiver.register(this);
		GoogleDriveUploader uploader = GoogleDriveUploader.getInstance();
		uploader.upload(filePath, fileMeta);
		Log.d(TAG, "onStartJob: scheduled " + filePath);

		// Return true as there's more work to be done with this job.
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		// Stop tracking these job parameters, as we've 'finished' executing.
		Log.i(TAG, "on stop job: " + params.getJobId());

		// Return false to drop the job.
		return false;
	}

}
