package com.sjning.app2.receive;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BootService extends Service {

	public static final String TAG = "BootService";

	private ContentObserver mObserver;

	@Override
	public void onCreate()

	{

		System.out.println(TAG + "onCreate()");
		super.onCreate();
		addSMSObserver();

	}

	public void addSMSObserver() {
		Log.i(TAG, "add a SMS observer. ");
		ContentResolver resolver = getContentResolver();
		Handler handler = new SMSHandler(this);
		mObserver = new SMSObserver(resolver, handler);
		resolver.registerContentObserver(SMS.CONTENT_URI, true, mObserver);
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		System.out.println(TAG + "onStart RRRr");
		
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy().");
		this.getContentResolver().unregisterContentObserver(mObserver);
		super.onDestroy();
		// Process.killProcess(Process.myPid());
		System.exit(0);
	}

}
