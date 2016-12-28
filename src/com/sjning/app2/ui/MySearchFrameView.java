package com.sjning.app2.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.sjning.app3.R;
import com.sjning.app2.Search;



public class MySearchFrameView extends View{
	private Rect frame;
	private Context context;
	private int scannerAlpha;
	private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
	public MySearchFrameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		scannerAlpha = 0;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		int width=Search.width-50;
		int height=Search.height-50;
		int leftOffset = (Search.width - width) / 2;
		int topOffset = (Search.height - height) / 2;
		
		frame = new Rect(leftOffset, topOffset, leftOffset + width,
				topOffset + height);
		Paint paint = new Paint();
		paint.setColor(getResources().getColor(R.color.viewfinder_frame));
		int lineLen = 50;
		paint.setStrokeWidth(3);
		
		
		float[] leftTop = { frame.left, frame.top, frame.left + lineLen,
				frame.top, frame.left, frame.top, frame.left,
				frame.top + lineLen };// 左上
		canvas.drawLines(leftTop, paint);
		float[] rightTop = { frame.right - lineLen, frame.top, frame.right,
				frame.top, frame.right, frame.top, frame.right,
				frame.top + lineLen };// 右上
		canvas.drawLines(rightTop, paint);
		float[] leftBottom = { frame.left, frame.bottom, frame.left,
				frame.bottom - lineLen, frame.left, frame.bottom,
				frame.left + lineLen, frame.bottom };// 左下
		canvas.drawLines(leftBottom, paint);
		float[] rightBottom = { frame.right, frame.bottom,
				frame.right - lineLen, frame.bottom, frame.right,
				frame.bottom - lineLen, frame.right, frame.bottom };// 右下
		canvas.drawLines(rightBottom, paint);

		paint.setColor(context.getResources().getColor(R.color.viewfinder_laser));
		paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
		scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
		int middlex = frame.width() / 2 + frame.left;
		int middley = frame.height() / 2 + frame.top;
		int midLen = 50;
		float[] middleLines = { middlex - midLen, middley, middlex + midLen,
				middley, middlex, middley - midLen, middlex, middley + midLen };
		canvas.drawLines(middleLines, paint);
	}
	

}
