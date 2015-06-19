package cn.airburg.emo.wbapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import cn.airburg.emo.SelfApplication;
import cn.airburg.emo.activity.MainActivity;
import cn.airburg.emo.manager.AppManager;
import cn.airburg.emo.utils.Constants;
import cn.airburg.emo.utils.LogUtils;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

/**
 * Created by u17 on 2015/1/21.
 */
public class WBEntryActivity extends Activity implements IWeiboHandler.Response {
	/**
	 * 微博分享接口
	 */
	private IWeiboShareAPI mWeiboShareAPI ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtils.d("测试");
		// 创建微博 SDK 接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

		// 注册到新浪微博
		mWeiboShareAPI.registerApp();

		// 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
		// 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
		// 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
		// 失败返回 false，不调用上述回调
//		if (savedInstanceState != null && null != mWeiboShareAPI) {
		mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
//		}
	}

	/**
	 * @see {@link Activity#onNewIntent}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		if(null != mWeiboShareAPI) {
			mWeiboShareAPI.handleWeiboResponse(intent, this);
		}
	}

	@Override
	public void onResponse(BaseResponse baseResponse) {
		switch (baseResponse.errCode) {
			case WBConstants.ErrorCode.ERR_OK:
				Toast.makeText(WBEntryActivity.this, cn.airburg.emo.R.string.weibo_toast_share_success, Toast.LENGTH_SHORT).show();
				AppManager.getAppManager().finishActivity();
				break;
			case WBConstants.ErrorCode.ERR_CANCEL:
				Toast.makeText(WBEntryActivity.this, cn.airburg.emo.R.string.weibo_toast_share_canceled, Toast.LENGTH_SHORT).show();
				break;
			case WBConstants.ErrorCode.ERR_FAIL:
				Toast.makeText(WBEntryActivity.this, getString(cn.airburg.emo.R.string.weibo_toast_share_failed) + "Error Message: " + baseResponse.errMsg,
						Toast.LENGTH_SHORT).show();
				break;
		}

		finish();
	}
}
