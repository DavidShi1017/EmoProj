package cn.airburg.emo.manager.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import cn.airburg.emo.R;
import cn.airburg.emo.utils.AccessTokenKeeper;
import cn.airburg.emo.utils.Constants;
import cn.airburg.emo.utils.LogUtils;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

/**
 * 微博分享
 * Created by u17 on 2015/1/31.
 */
public class WeiboShare extends DefaultShare {

	/**
	 * 微博分享接口
	 */
	private IWeiboShareAPI mWeiboShareAPI ;

	/**
	 * 微博分享后的响应信息处理
	 */
	private IWeiboHandler.Response mWeiboHandlerResponse ;

	private AuthInfo mAuthInfo;

	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
	private Oauth2AccessToken mAccessToken;

	/** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
	private SsoHandler mSsoHandler;

	/**
	 * 图片地址
	 */
	private String mImagePath ;


	public WeiboShare(Context context) {
		super(context);

		// 创建微博 SDK 接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mContext, Constants.APP_KEY);

		// 注册到新浪微博
		mWeiboShareAPI.registerApp();
	}

	/**
	 * 分享 图片
	 * @param bitmapPathString  bitmap 图片地址
	 */
	public void shareImage(String bitmapPathString) {
		this.mImagePath = bitmapPathString ;
		LogUtils.d(" -> ");
		// 从 SharedPreferences 中读取上次已保存好 AccessToken 等信息，
		// 第一次启动本应用，AccessToken 不可用
		mAccessToken = AccessTokenKeeper.readAccessToken(mContext);
		if (mAccessToken.isSessionValid()) {
			LogUtils.d("access token session valid.");
//			updateTokenView(true);
			// 发表分享微博
			publishWeiboShare(bitmapPathString) ;
		} else {
			LogUtils.d("access token session invalid.");
			// 创建微博实例
			//mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
			// 快速授权时，请不要传入 SCOPE，否则可能会授权不成功
			mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, null);
			mSsoHandler = new SsoHandler((Activity)mContext, mAuthInfo);

			// SSO 授权, 仅客户端
			//	    mSsoHandler.authorizeClientSso(new AuthListener());

			// SSO 授权, ALL IN ONE
//			mSsoHandler.authorize(new AuthListener());
			mSsoHandler.authorizeWeb(new AuthListener());
		}
	}

	/**
	 * 分享文字
	 * @param value 文字内容
	 */
	public void shareString(String value) {
		LogUtils.d(" -> ");
	}

	/**
	 * 发布微博分享
	 * @param bitmapPathString 分享图片地址
	 */
	private void publishWeiboShare(String bitmapPathString) {

		// 获取微博客户端相关信息，如是否安装、支持 SDK 的版本
		boolean isInstalledWeibo = mWeiboShareAPI.isWeiboAppInstalled();
//		int supportApiLevel = mWeiboShareAPI.getWeiboAppSupportAPI();

		// 1. 初始化微博的分享消息
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
//		if (hasText) {
//			weiboMessage.textObject = getTextObj();
//		}

		// 设置要分享的图片信息
		ImageObject imgObject = new ImageObject() ;
		Bitmap bmp = BitmapFactory.decodeFile(bitmapPathString);
		imgObject.setImageObject(bmp) ;
		weiboMessage.imageObject = imgObject;

		// 2. 初始化从第三方到微博的消息请求
		SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.multiMessage = weiboMessage;

		// 3. 发送请求消息到微博，唤起微博分享界面
//		if (isInstalledWeibo) { // 如果安装了客户端，那么唤起客户端
//			mWeiboShareAPI.sendRequest(ShareActivity.this, request);
//		} else
		{
			AuthInfo authInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
			Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
			String token = "";
			if (accessToken != null) {
				token = accessToken.getToken();
			}
			mWeiboShareAPI.sendRequest((Activity)mContext, request, authInfo, token, new WeiboAuthListener() {

				@Override public void onWeiboException( WeiboException weiboException ) {
					LogUtils.e(JSON.toJSONString(weiboException));
				}

				@Override
				public void onComplete( Bundle bundle ) {
					Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
					AccessTokenKeeper.writeAccessToken(mContext, newToken);
					LogUtils.d("onAuthorizeComplete token = " + newToken.getToken());
					// 授权成功
					Toast.makeText(mContext, R.string.weibo_toast_auth_success, Toast.LENGTH_SHORT).show();
				}
				@Override public void onCancel() { }
			});
		}
	}

	/**
	 * 微博认证授权回调类。
	 * 1. SSO 授权时，需要在 { onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
	 *    该回调才会被执行。
	 * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
	 * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
	 */
	private final class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			LogUtils.d(JSON.toJSONString(values));
			// 从 Bundle 中解析 Token
			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				// 显示 Token
//				updateTokenView(false);

				// 保存 Token 到 SharedPreferences
				AccessTokenKeeper.writeAccessToken(mContext, mAccessToken);
				Toast.makeText(mContext, R.string.weibo_toast_auth_success, Toast.LENGTH_SHORT).show();

				// 发起分享微博
				publishWeiboShare(mImagePath) ;
			} else {
				// 以下几种情况，您会收到 Code：
				// 1. 当您未在平台上注册的应用程序的包名与签名时；
				// 2. 当您注册的应用程序包名与签名不正确时；
				// 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
				String code = values.getString("code");
				String message = mContext.getString(R.string.weibo_toast_auth_failed);
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code: " + code;
				}
				Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onCancel() {
			Toast.makeText(mContext, R.string.weibo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(mContext, "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
