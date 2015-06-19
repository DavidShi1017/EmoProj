package cn.airburg.emo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import cn.airburg.emo.R;
import cn.airburg.emo.SelfApplication;
import cn.airburg.emo.activity.MainActivity;
import cn.airburg.emo.manager.AppManager;
import cn.airburg.emo.utils.Constants;
import cn.airburg.emo.utils.LogUtils;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by u17 on 2015/1/20.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	/**
	 * 微信
	 */
	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID, false);
		// 将该app注册到微信
//		api.registerApp(Constants.WEIXIN_APP_ID);


		// 微信sdk
	    api.handleIntent(getIntent(), this);

	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq baseReq) {
		LogUtils.d(JSON.toJSONString(baseReq));

		finish() ;
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp baseResp) {
		LogUtils.d(JSON.toJSONString(baseResp));

		int result = 0;

		switch (baseResp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				result = R.string.errcode_success;
				AppManager.getAppManager().finishActivity();
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				result = R.string.errcode_cancel;
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				result = R.string.errcode_deny;
				break;
			default:
				result = R.string.errcode_unknown;
				break;
		}

		Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

		finish() ;
	}
}
