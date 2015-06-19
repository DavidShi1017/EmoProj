package cn.airburg.emo.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import cn.airburg.emo.R;
import cn.airburg.emo.listeners.DialogActionCallBack;

/**
 * Created by shig on 2015/6/12.
 */
public class LocationUtils {


    private static final String PROPERTY_IS_ALLOW = "isAllow";
    private static final String PREFERENCES_LOCATION = "cn.airburg.emo.location.app";
    public static boolean isShowing;

    public static void setAllow(Context context) {

        SharedPreferences.Editor editor = getRatePreferences(context).edit();
        editor.putBoolean(PROPERTY_IS_ALLOW, true);
        editor.commit();
    }

    public static boolean isAllow(Context context) {

        boolean isAllow = getRatePreferences(context).getBoolean(PROPERTY_IS_ALLOW, false);

        return isAllow;
    }

    private static SharedPreferences getRatePreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_LOCATION, Context.MODE_PRIVATE);
    }


    public static void showLocationDialog(final Context context, final DialogActionCallBack callbackListener){


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(null);
        builder.setTitle(context.getString(R.string.review_option_title));
        builder.setMessage(context.getString(R.string.review_option_info));

        builder.setPositiveButton(context.getString(R.string.review_option_yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setAllow(context);
                if (null != callbackListener) {
                    callbackListener.onOkAction();
                }
                isShowing = false;
            }
        });

        builder.setNegativeButton(context.getString(R.string.review_option_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                isShowing = false;
            }
        });

        builder.create().show();
        isShowing = true;
    }



}
