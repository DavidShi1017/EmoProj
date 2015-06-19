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
     * @Description: Activity�����ࣺ���ڹ���Activity���˳�����
     * @date 2014-11-2 ����11:27:55
     */


    private static Stack<Activity> activityStack;

    private static AppManager instance;


    private AppManager() {

    }


    /**
     * ��һʵ��
     */

    public static AppManager getAppManager() {

        if (instance == null) {
            instance = new AppManager();

        }

        return instance;

    }


    /**
     * ���Activity����ջ
     */

    public void addActivity(Activity activity) {
        LogUtils.e(" -> " + activity.getLocalClassName());
        if (activityStack == null) {

            activityStack = new Stack<Activity>();

        }

        activityStack.add(activity);

    }

    /**
     * ��ȡ��ǰActivity����ջ�����һ��ѹ��ģ�
     */

    public Activity currentActivity() {

        Activity activity = activityStack.lastElement();

        return activity;
    }


    /**
     * ������ǰActivity����ջ�����һ��ѹ��ģ�
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
     * ����ָ����Activity
     */

    public void finishActivity(Activity activity) {

        if (activity != null) {

            activityStack.remove(activity);

            activity.finish();

            activity = null;

        }

    }

    /**
     * ����ָ��������Activity
     */

    public void finishActivity(Class<?> cls) {

        for (Activity activity : activityStack) {

            if (activity.getClass().equals(cls)) {

                finishActivity(activity);

            }

        }

    }

    /**
     * ��������Activity
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
     * �˳�Ӧ�ó���
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
