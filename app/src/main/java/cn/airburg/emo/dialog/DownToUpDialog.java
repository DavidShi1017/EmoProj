package cn.airburg.emo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

/**
 * 由bottom出现 由bottom 消失对话框
 * Created by u17 on 2015/1/31.
 */
public class DownToUpDialog extends Dialog {

	public DownToUpDialog(Context context) {
		this(context, cn.airburg.emo.R.style.down_to_up_dialog);


		Window window = this.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//对话框窗口的宽和高
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;

		//对话框窗口显示的位置
//		params.x = 100;
//		params.y = 100;

		window.setGravity(Gravity.LEFT | Gravity.BOTTOM);

		this.setCanceledOnTouchOutside(true);
	}

	protected DownToUpDialog(Context context, int theme) {
		super(context, theme);
	}
}
