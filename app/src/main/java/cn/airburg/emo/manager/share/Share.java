package cn.airburg.emo.manager.share;

/**
 * 分享
 * Created by u17 on 2015/1/31.
 */
public interface Share {
	/**
	 * 分享 图片
	 * @param bitmapPathString  bitmap 图片地址
	 */
	public void shareImage(String bitmapPathString) ;

	/**
	 * 分享文字
	 * @param value 文字内容
	 */
	public void shareString(String value) ;
}
