
package com.google.zxing.client.android;

import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.sjning.app3.R;

public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 100L;
  private static final int OPAQUE = 0xFF;

  private final Paint paint;
  private Bitmap resultBitmap;
  private final int frameColor;
  private final int laserColor;
  private final int maskColor;
  private final int resultColor;
  private final int resultPointColor;
  private int scannerAlpha;
  private Collection<ResultPoint> possibleResultPoints;
  private Collection<ResultPoint> lastPossibleResultPoints;
  private boolean laserLinePortrait=true;
  
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint();
    Resources resources = getResources();
    
    maskColor = resources.getColor(R.color.viewfinder_mask);
    resultColor = resources.getColor(R.color.result_view);
    frameColor = resources.getColor(R.color.viewfinder_frame);
    laserColor = resources.getColor(R.color.viewfinder_laser);
    resultPointColor = resources.getColor(R.color.possible_result_points);
    scannerAlpha = 0;
    possibleResultPoints = new HashSet<ResultPoint>(5);
  }

  @Override
  public void onDraw(Canvas canvas) {
    Rect frame = CameraManager.get().getFramingRect();
    if (frame == null) {
      return;
    }
   
//    System.out.println("frame.left="+frame.left);
//    System.out.println("frame.top="+frame.top);
//    System.out.println("frame.right="+frame.right);
//    System.out.println("frame.bottom="+frame.bottom);
    
//    Tools.println("娴滃瞼娣惍涔╮aw canvas鐎规枻绱�+width+" 妤傛﹫绱�+height);
//    System.out.println("canvas width:"+width);
//    System.out.println("canvas height:"+height);
    
    //娣囶喗鏁奸崜宥勫敩閻拷//    int width = canvas.getWidth();
//    int height = canvas.getHeight();
//    // Draw the exterior (i.e. outside the framing rect) darkened 閻忔媽澹婇拏娆愭緲
//    paint.setColor(resultBitmap != null ? resultColor : maskColor);
//    canvas.drawRect(0, 0, width, frame.top, paint);
//    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
//    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
//    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(OPAQUE);
      canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
    } else {
    	
      // Draw a two pixel solid black border inside the framing rect
      //閻㈣鍤鎴ｅ濡楀棜绔熷锟�/      paint.setColor(frameColor);
//      int lineLen=30;
//      paint.setStrokeWidth(3);
//      float[] leftTop={frame.left,frame.top,frame.left+lineLen,frame.top,frame.left,frame.top,frame.left,frame.top+lineLen};//瀹革缚绗�//      canvas.drawLines(leftTop, paint);
//      float[] rightTop={frame.right-lineLen,frame.top,frame.right,frame.top,frame.right,frame.top,frame.right,frame.top+lineLen};//閸欏厖绗�//      canvas.drawLines(rightTop, paint);
//      float[] leftBottom={frame.left,frame.bottom,frame.left,frame.bottom-lineLen,frame.left,frame.bottom,frame.left+lineLen,frame.bottom};//瀹革缚绗�//      canvas.drawLines(leftBottom, paint);
//      float[] rightBottom={frame.right,frame.bottom,frame.right-lineLen,frame.bottom,frame.right,frame.bottom-lineLen,frame.right,frame.bottom};//閸欏厖绗�//      canvas.drawLines(rightBottom, paint);
   
      //閸樼喐娼甸惃鍕敩閻拷//      canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
//      canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
//      canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
//      canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);
      	
      //閻㈣鍤痪銏ｅ閻ㄥ嫪鑵戠痪锟�     // Draw a red "laser scanner" line through the middle to show decoding is active
//      paint.setColor(laserColor);
//      paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//      scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
      
//      int middlex=frame.width()/2+frame.left;
//      int middley = frame.height() / 2 + frame.top;
//      int midLen=30;
//      float[] middleLines={middlex-midLen,middley,middlex+midLen,middley,middlex,middley-midLen,middlex,middley+midLen};
//      canvas.drawLines(middleLines, paint);
      
      //娣囶喗鏁奸崜宥勫敩閻拷      // Draw a red "laser scanner" line through the middle to show decoding is active
//      paint.setColor(laserColor);
//      paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//      scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//      int middle = frame.height() / 2 + frame.top;
//      if(laserLinePortrait){
//    	  canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
//      }else{
//        float left=frame.left+(frame.right-frame.left)/2-2;
//        float top=frame.top-(frame.right-frame.left)/2-2;
//        canvas.drawRect(left, frame.top, left+2, frame.bottom-2, paint);
//      }
      
      
      Collection<ResultPoint> currentPossible = possibleResultPoints;
      Collection<ResultPoint> currentLast = lastPossibleResultPoints;
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints = null;
      } else {
        possibleResultPoints = new HashSet<ResultPoint>(5);
        lastPossibleResultPoints = currentPossible;
        paint.setAlpha(OPAQUE);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentPossible) {
//          canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
        	 canvas.drawCircle(frame.left + point.getY(), frame.top + point.getX(), 6.0f, paint);
        }
      }
      if (currentLast != null) {
        paint.setAlpha(OPAQUE / 2);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentLast) {
          canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
        }
      }

      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }
  }

  public void changeLaser(){
	   if(laserLinePortrait){
		   laserLinePortrait=false;
	   }else{
		   laserLinePortrait=true;
	   }
  }
  public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    possibleResultPoints.add(point);
  }

}
