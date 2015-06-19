package cn.airburg.emo.exception.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import cn.airburg.emo.R;
import cn.airburg.emo.manager.AppManager;
import cn.airburg.emo.utils.LogUtils;
import cn.airburg.emo.utils.Utils;

/**
 * 全局异常处理器
 *
 * @author 杨杰
 */
public class GlobalScopeCrashHandler implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler defaultUEH;
    private Context context;

    /**
     * 构造函数，获取默认的处理方法
     */
    public GlobalScopeCrashHandler(Context context) {
        LogUtils.i(" --> new");
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }

    /**
     * 这个接口必须重写，用来处理我们的异常信息
     *
     * @param thread
     * @param ex
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtils.e(" --> catch exception: ");
        ex.printStackTrace();

        Writer resultWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(resultWriter);
        //获取跟踪的栈信息，除了系统栈信息，还把手机型号、系统版本、编译版本的唯一标示
        StackTraceElement[] trace = ex.getStackTrace();
        StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
        System.arraycopy(trace, 0, trace2, 0, trace.length);
        trace2[trace.length + 0] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
        trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
        trace2[trace.length + 2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);

        // 追加信息，因为后面会回调默认的处理方法
        ex.setStackTrace(trace2);
        ex.printStackTrace(printWriter);

        // 把上面获取的堆栈信息转为字符串，打印出来
        String stacktraceString = resultWriter.toString();
        printWriter.close();

        LogUtils.e(stacktraceString);

        // 这里把刚才异常堆栈信息写入SD卡的Log日志里面
        Utils.saveGlobalExceptionInfo(stacktraceString);

//        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            String sdcardPath = Environment.getExternalStorageDirectory().getPath();
//            writeLog(stacktraceString, sdcardPath + "/mythou");
//        }

        if (!handleException(ex) && defaultUEH != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            defaultUEH.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LogUtils.e("error : ", e);
            }
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        //defaultUEH.uncaughtException(thread, ex);
    }

    //final Activity activity = AppManager.getAppManager().currentActivity();

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {

        if (ex == null) {
            return false;
        }

        final Activity activity = AppManager.getAppManager().currentActivity();

        if (activity == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                new AlertDialog.Builder(activity).setTitle(context.getString(R.string.review_option_title))
                        .setCancelable(false).setMessage(context.getString(R.string.exception_text))
                        .setPositiveButton(context.getString(R.string.positive_button_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppManager.getAppManager().exitApp(activity);
                            }
                        }).create().show();
                Looper.loop();
            }
        }.start();
        return true;
    }
}