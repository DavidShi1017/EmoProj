package cn.airburg.emo;

import android.app.Application;
import android.content.Context;

import cn.airburg.emo.exception.handler.GlobalScopeCrashHandler;

/**
 * Created by u17 on 2015/1/25.
 */
public class SelfApplication extends Application {
	private static Context sApplicationContext ;
	@Override
	public void onCreate() {
		super.onCreate();

		sApplicationContext = this ;

		// 设置全局异常处理器
		GlobalScopeCrashHandler globalCrashHandler = new GlobalScopeCrashHandler(sApplicationContext) ;
		Thread.setDefaultUncaughtExceptionHandler(globalCrashHandler);
	}

	public static Context getContext() {
		return sApplicationContext ;
	}
}
