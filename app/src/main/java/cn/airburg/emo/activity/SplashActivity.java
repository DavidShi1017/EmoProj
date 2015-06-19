package cn.airburg.emo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import java.text.ParseException;
import java.util.Date;

import cn.airburg.emo.R;
import cn.airburg.emo.utils.Utils;

/**
 * app扉页Activity
 * Created by u17 on 2015/1/31.
 */
public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.setContentView(cn.airburg.emo.R.layout.activity_splash);

//		LinearLayout layout = (LinearLayout) this.findViewById(cn.ablecloud.emo.R.id.layoutSplash);
//		Animation animEnter = AnimationUtils.loadAnimation(this, cn.ablecloud.emo.R.anim.exit_alpha) ;
//		animEnter.setAnimationListener(new Animation.AnimationListener() {
//			@Override public void onAnimationStart(Animation animation) { }
//
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				Intent intent = new Intent(SplashActivity.this, MainActivity.class) ;
//				startActivity(intent);
//
//				finish();
//			}
//
//			@Override public void onAnimationRepeat(Animation animation) { }
//		});
//		layout.startAnimation(animEnter);

		// 设置为全屏
//		setFullScreen() ;

		Handler handler = new Handler() ;

//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				cancelFullScreen();
//			}
//		}, 1500) ;

		handler.postDelayed(new Runnable() {
			@Override public void run() {
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);

				finish();
				overridePendingTransition(R.anim.enter_alpha, R.anim.exit_alpha);
			}
		}, 1800) ;

		;

		try {
			Utils.getDateFromByteArray(Utils.getByteArrayFromDate(new Date())) ;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置为全屏显示
	 */
	private void setFullScreen() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * 退出全屏显示
	 */
	private void cancelFullScreen() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
}
