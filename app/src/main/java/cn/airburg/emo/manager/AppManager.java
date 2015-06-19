package cn.airburg.emo.manager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import java.util.Stack;

import cn.airburg.emo.activity.MainActivity;
import cn.airburg.emo.utils.LogUtils;

/**
 * Created by shig on 2015/6/12.
 */
public class AppManager {

    /**
     * @author zhaokaiqiang
     * @ClassName: net.oschina.app.AppManager
     * @Description: Activity管理类：用于管理Activity和退出程序
     * @date 2014-11-2 上午11:27:55
     */


    private static Stack<Activity> activityStack;

    private static AppManager instance;


    private AppManager() {

    }


    /**
     * 单一实例
     */

    public static AppManager getAppManager() {

        if (instance == null) {
            instance = new AppManager();

        }

        return instance;

    }


    /**
     * 添加Activity到堆栈
     */

    public void addActivity(Activity activity) {
        LogUtils.e(" -> " + activity.getLocalClassName());
        if (activityStack == null) {

            activityStack = new Stack<Activity>();

        }

        activityStack.add(activity);

    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */

    public Activity currentActivity() {

        Activity activity = activityStack.lastElement();

        return activity;
    }


    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */

    public void finishActivity() {

        Activity activity = activityStack.lastElement();
        LogUtils.e("-->" + activity.getLocalClassName());
        if (!"activity.MainActivity".equals(activity.getLocalClassName())){
            finishActivity(activity);
            activity.setResult(Activity.RESULT_CANCELED);
        }
    }


    /**
     * 结束指定的Activity
     */

    public void finishActivity(Activity activity) {

        if (activity != null) {

            activityStack.remove(activity);

            activity.finish();

            activity = null;

        }

    }

    /**
     * 结束指定类名的Activity
     */

    public void finishActivity(Class<?> cls) {

        for (Activity activity : activityStack) {

            if (activity.getClass().equals(cls)) {

                finishActivity(activity);

            }

        }

    }

    /**
     * 结束所有Activity
     */

    public void finishAllActivity() {

        for (int i = 0, size = activityStack.size(); i < size; i++) {

            if (null != activityStack.get(i)) {

                activityStack.get(i).finish();

            }

        }

        activityStack.clear();

    }

    /**
     * 退出应用程序
     */

    public void exitApp(Context context) {

        try {

            finishAllActivity();

            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            activityMgr.killBackgroundProcesses(context.getPackageName());

            System.exit(0);

        } catch (Exception e) {

        }

    }
}
