package com.sjning.app2.receive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sjning.app2.db.DBTool;

public class SMSHandler extends Handler

{

	public static final String TAG = "SMSHandler";

	private Context mContext;

	public SMSHandler(Context context) {
		super();
		this.mContext = context;
	}

	public void handleMessage(Message message) {
		Log.i(TAG, "handleMessage: " + message);
		MessageItem item = (MessageItem) message.obj;
		if (filterMessage(item)) {
			DBTool.getInstance().saveMessage(mContext, item);
		}
	}

	public static boolean filterMessage(MessageItem item) {
		String body = item.getBody();
		// String body = "*2016-06-15 11:44:47*";
		System.out.println("短信内容:" + body);
		if (!TextUtils.isEmpty(body) && body.length() > 14) {
			if (body.startsWith("*") && body.endsWith("*")) {
				System.out.println("HHHH:" + body);
				String[] dates = body.substring(1, body.length() - 1).split(
						"\\*");
				for (String date : dates) {
					System.out.println("kkkkk:" + date);
					if (date.length() > 13 && date.length() < 19
							&& date.contains("  ")) {

					} else {
						return false;
					}
				}
				List<String> childItems = new ArrayList<String>();
				Collections.addAll(childItems, dates);
				item.setChildItems(childItems);
				
				if(item.getPhone().length() > 11){
					String phone = item.getPhone();
					phone = phone.substring(phone.length() -11);
					item.setPhone(phone);
				}
				return true;
			}
		}
		return false;
	}
}
