package com.sjning.app2;

import com.sjning.app2.ui.TopBar;
import com.sjning.app3.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

public class OkAct extends Activity {
	private TextView content;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			finish();
		};
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		String info = getIntent().getStringExtra("info");
		setContentView(R.layout.main);
		
		setTopBar();
		content = (TextView) findViewById(R.id.text_content);
//		content.setText(info);
		content.setText("导出报告成功!");
		startAnimation();
	}

	
	private void startAnimation(){
		Animation animation = new AlphaAnimation(1f, 0.0f);
		animation.setDuration(1000);
		animation.setInterpolator(this,
				android.R.anim.anticipate_interpolator);
		animation.setRepeatCount(2);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessageDelayed(0, 500);
			}
		});
		content.startAnimation(animation);
	}
	private void setTopBar() {
		TopBar topBar = (TopBar) findViewById(R.id.topBar);
		topBar.hiddenLeftButton(true);
		topBar.hiddenRightButton(true);
		topBar.setTitle(getString(R.string.app_name));
	}
}
