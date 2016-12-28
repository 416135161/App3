package com.sjning.app2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.InactivityTimer;
import com.google.zxing.client.android.ViewfinderView;
import com.google.zxing.client.android.camera.CameraManager;
import com.sjning.app2.db.DBTool;
import com.sjning.app2.intrface.TopBarClickListener;
import com.sjning.app2.receive.BootService;
import com.sjning.app2.receive.MessageItem;
import com.sjning.app2.tools.MessageSender;
import com.sjning.app2.tools.NormalUtil;
import com.sjning.app2.tools.UserSession;
import com.sjning.app2.ui.TopBar;
import com.sjning.app3.R;

public class Search extends Activity implements SurfaceHolder.Callback {

	private boolean hasSurface = false;
	private boolean playBeep = true;

	public static int width = 0;// 二维码扫描区宽度
	public static int height = 0;// 二维码扫描区高度
	private static final float BEEP_VOLUME = 0.80f;
	private static final long VIBRATE_DURATION = 200L;

	private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES;

	static {
		DISPLAYABLE_METADATA_TYPES = new HashSet<ResultMetadataType>(5);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ISSUE_NUMBER);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.SUGGESTED_PRICE);
		DISPLAYABLE_METADATA_TYPES
				.add(ResultMetadataType.ERROR_CORRECTION_LEVEL);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.POSSIBLE_COUNTRY);
	}

	private enum Source {
		NATIVE_APP_INTENT, PRODUCT_SEARCH_LINK, ZXING_LINK, NONE
	}

	private CaptureActivityHandler handler = null;
	private ViewfinderView viewfinderView = null;
	private MediaPlayer mediaPlayer = null;

	private Source source = null;
	private String returnUrlTemplate = null;
	private Vector<BarcodeFormat> decodeFormats = null;
	private String characterSet = null;
	private InactivityTimer inactivityTimer = null;
	private FrameLayout frameLayout = null;
	private SurfaceView surfaceView = null;
	private TextView tip = null;
	private String lastContent;
	private Handler handler2 = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				doCloseWaitDialog();
				break;
			case 1:
				doShowWaitDialog((String) msg.obj);
				break;
			case 2:
				String content = (String) msg.obj;
//				content = decode(content);
				if (content != null && content.contains("##")) {
					if (lastContent == null || !lastContent.equals(content)) {
						playBeepSoundAndVibrate();
						String[] array = content.split("##");
						tip.setText(array[0]);

						List<MessageItem> list = DBTool.getInstance()
								.getSavedMessage(getApplicationContext(),
										array[0], null);
						String info;
						if (list != null)
							info = "《" + array[0] + "》,成功Yes";
						else
							info = "《" + array[0] + "》,失败No";
						MessageSender.getInstance().sendSms(array[1], info);
						sendMessageDelayed(obtainMessage(3, info), 3000);
					} else {
						 resetPreview();
					}
				} else {
					resetPreview();
				}
				lastContent = content;
				break;
			case 3:
				Intent intent = new Intent();
				intent.setClass(Search.this, OkAct.class);
				intent.putExtra("info", (String) msg.obj);
				Search.this.startActivity(intent);
				finish();
				break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.basesearch);

		frameLayout = (FrameLayout) findViewById(R.id.search_frame);
		surfaceView = (SurfaceView) findViewById(R.id.preview_view);

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		tip = (TextView) findViewById(R.id.text_tip);
		tip.setText("正在扫码采集中⋯⋯");

		init();
		startService(new Intent(this, BootService.class));
	}

	/**
	 * 二维码处理成功
	 * 
	 * @param rawResult
	 * @param barcode
	 */
	private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
		viewfinderView.setVisibility(View.GONE);
		String content = rawResult.getText();
		resetStatusView();
		// showWaitDialog(content);
		handler2.obtainMessage(2, content).sendToTarget();
	}

	public void init() {
		setTopBar();
		CameraManager.init(getApplication());
		handler = null;
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		ViewTreeObserver observer = frameLayout.getViewTreeObserver();
		observer.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				height = frameLayout.getMeasuredHeight();
				width = frameLayout.getMeasuredWidth();
				return true;
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// 初始化扫描界面
		super.onResume();
		System.out.println("5555555555555555555555" + "onResume");
		resumeEvent();
	}

	public void resumeEvent() {
		resetStatusView();

		final SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		surfaceView.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!hasSurface) {
					initCamera(surfaceHolder);
					hasSurface = true;
				}
			}

		}, 500);
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}

		source = Source.NONE;
		decodeFormats = null;
		characterSet = null;
		initBeepSound();
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseEvent();
		finish();
	}

	public void pauseEvent() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		hasSurface = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		inactivityTimer.shutdown();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	/**
	 * 成功获取二维码，在handler中调用这个方法，回传bitmap
	 * 
	 * @param rawResult
	 * @param barcode
	 */
	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();
		// lastResult = rawResult;
		if (barcode == null) {
			handleDecodeInternally(rawResult, null);
		} else {
			// playBeepSoundAndVibrate();// 播放声音和振动代表成功获取二维码
			drawResultPoints(barcode, rawResult);

			switch (source) {
			case NATIVE_APP_INTENT:
			case PRODUCT_SEARCH_LINK:
				handleDecodeExternally(rawResult, barcode);
				break;
			case ZXING_LINK:
				if (returnUrlTemplate == null) {
					handleDecodeInternally(rawResult, barcode);
				} else {
					handleDecodeExternally(rawResult, barcode);
				}
				break;
			case NONE: {
				handleDecodeInternally(rawResult, barcode);
			}
				break;
			}
		}
	}

	/**
	 * 把图片截图下来之后,标记二维码所在的点 Superimpose a line for 1D or dots for 2D to highlight
	 * the key features of the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2,
					barcode.getHeight() - 2);
			canvas.drawRect(border, paint);
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat()
							.equals(BarcodeFormat.UPC_A))
					|| (rawResult.getBarcodeFormat()
							.equals(BarcodeFormat.EAN_13))) {
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}

	private void handleDecodeExternally(Result rawResult, Bitmap barcode) {
		viewfinderView.drawResultBitmap(barcode);
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(VIBRATE_DURATION);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			NormalUtil.displayFrameworkBugMessageAndExit(Search.this);
			return;
		} catch (RuntimeException e) {
			NormalUtil.displayFrameworkBugMessageAndExit(Search.this);
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	private void setTopBar() {
		TopBar topBar = (TopBar) findViewById(R.id.topBar);
		topBar.hiddenLeftButton(true);
//		topBar.hiddenRightButton(true);
		topBar.hiddenRightButton(false);
		topBar.setTopBarClickListener(new TopBarClickListener() {

			@Override
			public void rightBtnClick() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(Search.this, MainActivity.class);
				startActivity(intent);
			}

			@Override
			public void leftBtnClick() {
				// TODO Auto-generated method stub
			}
		});
		topBar.setTitle("实刻订");
	}

	private void resetPreview() {
		if (handler != null) {
			handler.sendEmptyMessage(R.id.restart_preview);
		}
	}

	public final void showWaitDialog(String str) {
		Message message = handler2.obtainMessage();
		message.what = 1;
		message.obj = str;
		handler2.sendMessage(message);
	}

	public final void closeWaitDialog() {
		Message message = handler2.obtainMessage();
		message.what = 0;
		handler2.sendMessage(message);
	}

	private ProgressDialog waitDialog;

	private final void doShowWaitDialog(String str) {
		if (waitDialog == null)
			waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(str);
		waitDialog.show();
	}

	private final void doCloseWaitDialog() {
		if (waitDialog != null) {
			waitDialog.cancel();
			waitDialog = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showSetPhoneDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	private Dialog dialog;

	private void showSetPhoneDialog() {
		if (dialog == null) {
			View view = getLayoutInflater().inflate(R.layout.net_set, null);
			final EditText text1 = (EditText) view.findViewById(R.id.text1);
			final Button button = (Button) view.findViewById(R.id.button);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String num = text1.getEditableText().toString();
					if (num == null || num.equals("")) {
						Toast.makeText(getApplicationContext(), "输入格式不正确",
								Toast.LENGTH_SHORT).show();
						((Dialog) dialog).show();
					} else {
						UserSession.setPhone(getApplicationContext(),
								num.trim());
						dialog.dismiss();
					}
				}
			});
			text1.setText(UserSession.getPhone(getApplicationContext()) + "");
			dialog = new AlertDialog.Builder(this).setTitle("设置").setView(view).create();
			dialog.show();
		} else
			dialog.show();

	}
	private String decode(String souceCode) {
		if (souceCode == null || souceCode.equals(""))
			return null;
		if (souceCode.length() < 4 || !souceCode.contains("*"))
			return null;
		String[] oo = souceCode.split("\\*");
		byte offset = 0;
		byte[] bytes = new byte[oo.length - 1];
		String result;
		try {
			offset = Byte.valueOf(oo[oo.length - 1]);
			for (int j = 0; j < oo.length - 1; j++) {
				bytes[j] = (byte) (Byte.parseByte(oo[j]) - offset);
			}
			result = new String(bytes, "utf-8");
			System.out.println(result);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
		return result;
	}
}
