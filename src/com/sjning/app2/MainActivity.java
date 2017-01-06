package com.sjning.app2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sjning.app2.db.DBTool;
import com.sjning.app2.intrface.TopBarClickListener;
import com.sjning.app2.receive.MessageItem;
import com.sjning.app2.receive.SMSHandler;
import com.sjning.app2.receive.WatchService;
import com.sjning.app2.tools.FileUtils;
import com.sjning.app2.tools.NormalUtil;
import com.sjning.app2.tools.SmsContent;
import com.sjning.app2.tools.UserSession;
import com.sjning.app2.ui.TopBar;
import com.sjning.app3.R;

public class MainActivity extends Activity implements OnClickListener {
	private ListView listView;
	private View dataView;
	private View noData;
	private Button sendBtn, restartBtn, cleanBtn;

	private MyAdapter adapter;
	private Dialog deleteDialog;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			adapter.notifyDataSetChanged();
			if (adapter.getCount() == 0) {
				noData.setVisibility(View.VISIBLE);
				dataView.setVisibility(View.INVISIBLE);
			} else {
				noData.setVisibility(View.INVISIBLE);
				dataView.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTopBar();

		if (UserSession.isFirst(this)) {
			NormalUtil.deletePath();
			DBTool.getInstance().deleteAll(this);
			UserSession.setFirstFalse(this);
		}

		dataView = findViewById(R.id.data_view);
		sendBtn = (Button) findViewById(R.id.btn_send);
		sendBtn.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.listview);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MessageAct.class);
				intent.putExtra("message", (MessageItem) adapter.getItem(arg2));
				startActivity(intent);
			};

		});
		noData = findViewById(R.id.nodata);
		restartBtn = (Button) findViewById(R.id.btn_restart);
		restartBtn.setOnClickListener(this);
		cleanBtn = (Button) findViewById(R.id.btn_clean);
		cleanBtn.setOnClickListener(this);

		WatchService.actionReschedule(this);
		System.out.println("rrrrrrrrrrrrrrrrrrr");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initListView();
	}

	private void initListView() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Uri uri = Uri.parse("content://sms/inbox");
				SmsContent sc = new SmsContent(MainActivity.this, uri);
				List<MessageItem> itemsTemp = sc.getSmsInfo();
				if (itemsTemp != null) {
					for (MessageItem item : itemsTemp) {
						if (SMSHandler.filterMessage(item)) {
							DBTool.getInstance().saveMessage(MainActivity.this,
									item);
						}
					}
				}

				List<MessageItem> items = DBTool.getInstance().getSavedMessage(
						getApplicationContext(), null,
						// UserSession.getPhone(getApplicationContext())
						null);
				adapter.setData(items);
				mHandler.sendEmptyMessage(0);
			}

		}).start();
	}

	class MyAdapter extends BaseAdapter {
		private List<MessageItem> items = new ArrayList<MessageItem>();

		public void setData(List<MessageItem> tasks) {
			this.items.clear();
			if (tasks != null) {
				this.items.addAll(tasks);
				tasks.clear();
				tasks = null;
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getOutPutMessage() {
			Date date = new java.util.Date();
			String dateTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
					.format(date);
			String temp = "TM: " + dateTime + "\r\n" + "\r\n";
			if (items != null && !items.isEmpty()) {
				for (int i = 0; i < items.size(); i++) {
					MessageItem item = items.get(i);
					temp += ("##: " + item.getPhone() + ":  "
							+ item.getChildItems().size() + "\r\n"
							+ getItemBody(item) + "\r\n");
				}
			}
			return temp;
		}

		private String getItemBody(MessageItem item) {
			StringBuffer sBuffer = new StringBuffer();
			int size = item.getChildItems().size();
			for (int i = size; i > 0; i--) {
				sBuffer.append(i + " > ")
						.append(item.getChildItems().get(size - i))
						.append("\r\n");

			}
			return sBuffer.toString();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			MessageItem item = this.items.get(position);
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.message_item, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView.findViewById(R.id.text1);
				holder.text2 = (TextView) convertView.findViewById(R.id.text2);
				holder.text3 = (TextView) convertView.findViewById(R.id.text3);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.text1.setText(item.getPhone());
			holder.text2.setText(item.getDate());
			if (item.getChildItems() != null)
				holder.text3.setText(item.getChildItems().size() + "");
			return convertView;

		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		// Intent intent = new Intent();
		// intent.setClass(this, Search.class);
		// startActivity(intent);
	}

	class ViewHolder {
		TextView text1;
		TextView text2;
		TextView text3;

	}

	private void setTopBar() {
		TopBar topBar = (TopBar) findViewById(R.id.topBar);
		topBar.hiddenLeftButton(true);
		topBar.hiddenRightButton(false);
		topBar.setTitle(getString(R.string.app_name));
		topBar.setRightDrawable(R.drawable.close);
		topBar.setTopBarClickListener(new TopBarClickListener() {

			@Override
			public void rightBtnClick() {

				finish();

			}

			@Override
			public void leftBtnClick() {

			}
		});

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_send:
			try {
				SimpleDateFormat tempDate = new SimpleDateFormat("yyyyMM");
				String datetime = tempDate.format(new java.util.Date());
				String fileName = "eposton" + "-"+datetime + ".txt";
				String filePath = NormalUtil.getRootDir();
				if (FileUtils.checkFileExist(filePath + fileName)) {
					FileUtils.deleteFile(filePath + fileName);
				}
				FileUtils.saveToSDCardOrRAM(this, fileName,
						adapter.getOutPutMessage(), filePath);
				Intent intent = new Intent(this, OkAct.class);
				startActivity(intent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btn_restart:
			WatchService.actionReschedule(this);
			break;
		case R.id.btn_clean:
			final boolean isExit = isSystemMsgExit();
			View view = getLayoutInflater().inflate(R.layout.dlg_delete, null);
			final Button button = (Button) view.findViewById(R.id.button);
			final Button btnCancle = (Button) view
					.findViewById(R.id.button_cancle);
			TextView textTip = (TextView) view.findViewById(R.id.text_tip);
			if (isExit) {
				textTip.setText("清空数据之前需先删除短信里的相关数据！");
			} else {
				textTip.setText("清空列表数据前确定已把数据发送导出？");
			}

			btnCancle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteDialog.dismiss();
				}
			});
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isExit) {

					} else {
						DBTool.getInstance().deleteAll(MainActivity.this);
						NormalUtil.deletePath();
						initListView();
					}
					deleteDialog.dismiss();
				}
			});
			deleteDialog = new AlertDialog.Builder(this).setTitle("删除提示").setView(view).create();
			deleteDialog.setCanceledOnTouchOutside(true);
			deleteDialog.show();
			break;
		}
	}

	private boolean isSystemMsgExit() {
		Uri uri = Uri.parse("content://sms/inbox");
		SmsContent sc = new SmsContent(MainActivity.this, uri);
		List<MessageItem> itemsTemp = sc.getSmsInfo();
		if (itemsTemp != null) {
			for (MessageItem item : itemsTemp) {
				if (SMSHandler.filterMessage(item)) {
					return true;
				}
			}
		}
		return false;
	}
}
