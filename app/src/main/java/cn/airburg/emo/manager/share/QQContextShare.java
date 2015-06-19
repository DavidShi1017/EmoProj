package cn.airburg.emo.manager.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import cn.airburg.emo.R;
import cn.airburg.emo.activity.MainActivity;
import cn.airburg.emo.manager.AppManager;
import cn.airburg.emo.utils.LogUtils;

/**
 * Created by shig on 2015/6/9.
 */
public class QQContextShare extends DefaultShare implements IUiListener {
    private Tencent mTencent;
    private Handler mHandler;
    private Bundle shareParams;

    private Activity activity;
    private static final String APP_ID = "1104671506";

    public QQContextShare(Context context, Activity activity) {
        super(context);
        this.activity = activity;
        mTencent = Tencent.createInstance(APP_ID, context);

        mHandler = new Handler();
    }
    @Override
    public void shareString(String value) {

    }

    @Override
    public void shareImage(String bitmapPath) {
        LogUtils.e("image path is::" + APP_ID);
        Bundle b = getShareBundle(bitmapPath);
        if(b != null){
            shareParams = b;
            Thread thread = new Thread(shareThread);
            thread.start();
        }
    }

    private Bundle getShareBundle(String bitmapPath){
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, bitmapPath);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "emo空气检测");
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        return params;
    }



    Handler shareHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    Runnable shareThread = new Runnable() {

        public void run() {
            doShareToQQ(shareParams);
            Message msg = shareHandler.obtainMessage();

            // 将Message对象加入到消息队列当中
            shareHandler.sendMessage(msg);

        }
    };
    private void doShareToQQ(Bundle params) {
        mTencent.shareToQQ(activity, params, this);
    }

    @Override
    public void onError(UiError uiError) {
        showResult("shareToQQ:", "onError code:" + uiError.errorCode
                + ", msg:" + uiError.errorMessage + ", detail:"
                + uiError.errorDetail);
        Toast.makeText(activity, R.string.weibo_toast_share_failed, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onComplete(Object o) {
        Toast.makeText(activity, R.string.weibo_toast_share_success, Toast.LENGTH_SHORT).show();
        AppManager.getAppManager().finishActivity();

    }

    @Override
    public void onCancel() {
        Toast.makeText(activity, R.string.weibo_toast_share_canceled, Toast.LENGTH_SHORT).show();
    }

    private void showResult(final String base, final String msg) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                LogUtils.e("-->" + base + msg);
            }
        });
    }
}
