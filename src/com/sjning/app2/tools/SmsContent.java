package com.sjning.app2.tools;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;

import com.sjning.app2.receive.MessageItem;

public class SmsContent {
	private Activity activity;// 这里有个activity对象，不知道为啥以前好像不要，现在就要了。自己试试吧。
	private Uri uri;
	List<MessageItem> infos;

	public SmsContent(Activity activity, Uri uri) {
		infos = new ArrayList<MessageItem>();
		this.activity = activity;
		this.uri = uri;
	}

	/**
	 * Role:获取短信的各种信息 <BR>
	 * Date:2012-3-19 <BR>
	 * 
	 * @author CODYY)peijiangping
	 */
	public List<MessageItem> getSmsInfo() {
		String[] projection = new String[] { "_id", "address", "person",
				"body", "date", "type" };
		Cursor cusor = activity.managedQuery(uri, projection, null, null,
				"date asc");
		int phoneNumberColumn = cusor.getColumnIndex("address");
		int smsbodyColumn = cusor.getColumnIndex("body");
		int dateColumn = cusor.getColumnIndex("date");
		if (cusor != null) {
			while (cusor.moveToNext()) {
				MessageItem smsinfo = new MessageItem();
				String date = cusor.getString(dateColumn);
				date = UserSession.getDataStrFromTimeMillis(Long.valueOf(date),
						"yyyy-MM-dd HH:mm:ss");
				smsinfo.setDate(date);
				smsinfo.setPhone(cusor.getString(phoneNumberColumn));
				smsinfo.setBody(cusor.getString(smsbodyColumn));
				infos.add(smsinfo);
				// System.out.println(smsinfo.getBody());
			}
			if (VERSION.SDK_INT < 14) {
				cusor.close();
			}

		}
		return infos;
	}
}
