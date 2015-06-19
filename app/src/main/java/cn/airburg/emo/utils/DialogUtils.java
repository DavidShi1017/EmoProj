package cn.airburg.emo.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;

import cn.airburg.emo.listeners.DialogActionCallBack;

/**
 * 对话框工具类
 * @author 杨杰  2014-3-21
 *
 */
public class DialogUtils {
	private static ProgressDialog sProgressDialog ;
	/**
	 * 显示一个进度对话框
	 * @param context 当前上下文环境
	 * @param message 要显示的信息
	 */
	public static void showProgressDialog(Context context, String message) {
		showProgressDialog(context, false, message) ;
	}
	
	/**
	 * 显示一个进度对话框
	 * @param context 当前上下文环境
	 * @param cancelable 是否可以点击返回按钮隐藏进度对话框，true：表示可以，否者不可以
	 * @param message 要显示的信息
	 */
	public static void showProgressDialog(Context context, boolean cancelable, String message) {
		String msg = "处理中，请稍等..." ;
		if(!TextUtils.isEmpty(message)) {
			msg = message ;
		}
		//显示ProgressDialog  
		sProgressDialog = ProgressDialog.show(context, null, msg, true, cancelable); 
		if(!cancelable) {
			sProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					return keyCode == KeyEvent.KEYCODE_SEARCH;
				}
			});
		}
	}
	
	/** 隐藏对话框 */
	public static void dismissProgressDialog() {
		if(sProgressDialog != null && sProgressDialog.isShowing()){
			sProgressDialog.dismiss();
			sProgressDialog = null ;
        }
	}
	
	/**
	 * 显示确认提示
	 */
	public static void showConfirmDialog(Context context, String title, String message, final DialogActionCallBack callbackListener) {
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context) ;
		if(!TextUtils.isEmpty(title)) {
			dialogBuilder.setTitle(title) ;
		}
		dialogBuilder.setMessage((TextUtils.isEmpty(message) ? "确定要执行该操作吗？" : message)) ;
		dialogBuilder.setPositiveButton(cn.airburg.emo.R.string.dialog_ok, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				if(null != callbackListener) {
					callbackListener.onOkAction() ;
				}
				dialog.dismiss() ;
			}
		} ) ;
		dialogBuilder.setNegativeButton(cn.airburg.emo.R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				if(null != callbackListener) {
					callbackListener.onCancelAction() ;
				}
			}
		} ) ;
		dialogBuilder.create().show() ;
	}

}
