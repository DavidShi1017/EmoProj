package cn.airburg.emo.manager.share;

import android.content.Context;

/**
 * 分享默认实现
 * Created by u17 on 2015/1/31.
 */
public class DefaultShare implements Share {
	/**
	 * 应用上下文
	 */
	protected Context mContext ;

	public DefaultShare(Context context) {
		this.mContext = context ;
	}


	@Override
	public void shareImage(String bitmapPathString) { }

	@Override
	public void shareString(String value) { }
}
