package cn.airburg.emo.manager.share;

import android.content.Context;

import cn.airburg.emo.utils.LogUtils;
import com.tencent.mm.sdk.openapi.SendMessageToWX;

/**
 * 微信朋友圈分享
 * Created by u17 on 2015/1/31.
 */
public class WeixinFriendShare extends WeixinShare {

	public WeixinFriendShare(Context context) {
		super(context);
	}

	/**
	 * 分享 图片
	 * @param bitmapPathString  bitmap 图片地址
	 */
	public void shareImage(String bitmapPathString) {
		LogUtils.d(" -> ");
		// 分享到微信朋友圈
		publishImageShare(bitmapPathString, SendMessageToWX.Req.WXSceneTimeline);
	}

	/**
	 * 分享文字
	 * @param value 文字内容
	 */
	public void shareString(String value) {
		LogUtils.d(" -> ");
	}
}
