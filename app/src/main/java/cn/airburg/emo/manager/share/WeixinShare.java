package cn.airburg.emo.manager.share;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cn.airburg.emo.utils.Constants;
import cn.airburg.emo.utils.LogUtils;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;

import java.io.File;

/**
 * 微信分享
 * Created by u17 on 2015/1/31.
 */
public class WeixinShare extends DefaultShare {

	protected static final int THUMB_SIZE = 150;
	/**
	 * 微信 IWXAPI 是第三方app和微信通信的openapi接口
	 */
	private IWXAPI api;

	public WeixinShare(Context context) {
		super(context);

		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(mContext, Constants.WEIXIN_APP_ID, false);
		// 将该app注册到微信
		api.registerApp(Constants.WEIXIN_APP_ID);
	}

	/**
	 * 分享 图片
	 * @param bitmapPathString  bitmap 图片地址
	 */
	public void shareImage(String bitmapPathString) {
		LogUtils.d(" -> ");
		publishImageShare(bitmapPathString, SendMessageToWX.Req.WXSceneSession);
	}

	/**
	 * 分享文字
	 * @param value 文字内容
	 */
	public void shareString(String value) {
		LogUtils.d(" -> ");
	}

	/**
	 * 发布分享
	 * @param bitmapPathString 分享图片地址
	 * @param sceneType 分享场景
	 */
	protected void publishImageShare(String bitmapPathString, int sceneType) {
		File file = new File(bitmapPathString);

		WXImageObject imgObj = new WXImageObject();
		imgObj.setImagePath(bitmapPathString);

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;

		Bitmap bmp = BitmapFactory.decodeFile(bitmapPathString);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp,  THUMB_SIZE,  THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = sceneType ; // 分享到位置： 朋友 Or 朋友圈
		api.sendReq(req);
	}

	/**
	 * 构建 transaction
	 * @param type 类型
	 * @return transaction字符串
	 */
	protected String buildTransaction(String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}
