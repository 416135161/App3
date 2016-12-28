/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sjning.app2.receive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

/**
 */
public class WatchService extends Service {

	public static void actionReschedule(Context context) {
		Intent i = new Intent();
		i.setClass(context, WatchService.class);
		context.startService(i);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		System.out.println("start service watch");
		Intent intentLocation = new Intent(this, BootService.class);
		startService(intentLocation);
		reschedule();
		stopSelf(startId);
	}

	@Override
	public void onDestroy() {
		System.out.println("onDestroy");
		super.onDestroy();
	}

	private void cancel() {
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
		i.setClassName("com.sjning.app3",
				"com.sjning.app3.receive.WatchService");

		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		alarmMgr.cancel(pi);
	}

	private void reschedule() {
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
		i.setClassName("com.sjning.app3",
				"com.sjning.app3.receive.WatchService");

		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 5 * 60 * 1000, pi);

	}

	public IBinder onBind(Intent intent) {
		return null;
	}

}
