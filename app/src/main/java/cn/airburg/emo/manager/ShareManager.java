package cn.airburg.emo.manager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import cn.airburg.emo.dialog.DownToUpDialog;
import cn.airburg.emo.manager.share.QQContextShare;
import cn.airburg.emo.manager.share.Share;
import cn.airburg.emo.manager.share.WeiboShare;
import cn.airburg.emo.manager.share.WeixinFriendShare;
import cn.airburg.emo.manager.share.WeixinShare;
import cn.airburg.emo.utils.LogUtils;

/**
 * 分享管理器，可以分享到微信、微信朋友圈、微博
 * Created by u17 on 2015/1/31.
 */
public class ShareManager {

	/**
	 * 应用上下文
	 */
	private Context mContext ;

	/**
	 * 分享对话框
	 */
	private Dialog mShareDialog ;
	private Activity activity;

	public ShareManager(Activity activity) {
		this.activity = activity ;
		this.mContext = activity ;

	}

	public void shareScreenshot(final String screenshotPath) {
		if(null == mShareDialog) {
			//mShareManager = new ShareManager(this) ;
			mShareDialog = new DownToUpDialog(mContext) ;
			mShareDialog.setContentView(cn.airburg.emo.R.layout.activity_share);

			mShareDialog.findViewById(cn.airburg.emo.R.id.btnShareWeixin).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(" -> btnShareWeixin");
					toWeixin(screenshotPath);
					mShareDialog.dismiss();
				}
			});
			mShareDialog.findViewById(cn.airburg.emo.R.id.btnShareWeixinFriend).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(" -> btnShareWeixinFriend");
					toWeixinFriend(screenshotPath);
					mShareDialog.dismiss();
				}
			});
			mShareDialog.findViewById(cn.airburg.emo.R.id.btnShareWeibo).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(" -> btnShareWeibo");
					toWeibo(screenshotPath);
					mShareDialog.dismiss();
				}
			});
			mShareDialog.findViewById(cn.airburg.emo.R.id.btnShareQQ).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(" -> btnShareQQ");
					toQQ(screenshotPath, activity);
					mShareDialog.dismiss();
				}
			});


	}

		mShareDialog.show();

	}


	/**
	 * 分享到微信
	 * @param imagePath  图片地址
	 */
	public void toWeixin(String imagePath) {
		Share weixinShare = new WeixinShare(mContext) ;
		weixinShare.shareImage(imagePath);
		
	}

	/**
	 * 分享到微信朋友圈
	 * @param imagePath  图片地址
	 */
	public void toWeixinFriend(String imagePath) {
		Share weixinFriendShare = new WeixinFriendShare(mContext) ;
		weixinFriendShare.shareImage(imagePath);
	}

	/**
	 * 分享到微博
	 * @param imagePath  图片地址
	 */
	public void toWeibo(String imagePath) {
		Share weiboShare = new WeiboShare(mContext) ;
		weiboShare.shareImage(imagePath);
	}

	public void toQQ(String imagePath, Activity activity){
		Share qqShare = new QQContextShare(mContext, activity);
		qqShare.shareImage(imagePath);
	}

}
