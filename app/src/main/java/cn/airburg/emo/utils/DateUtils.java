package cn.airburg.emo.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shig on 2015/6/4.
 */
public class DateUtils {
    /**
     * Format a date to string that is according with user's date format
     *
     * @param context
     * @param date
     * @return String
     */
    public static String dateToString(Context context, Date date) {
        String dateStr = "";
		/*Context newContext;

		try {
			newContext = context.createPackageContext("com.android.launcher",Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			newContext = context;
		}	*/

        if (date != null) {
            java.text.DateFormat dateFormat = android.text.format.DateFormat
                    .getDateFormat(context);
            dateStr = dateFormat.format(date);
        }

        return dateStr;
    }

    public static String timeToString(Context context, Date date) {
        String dateStr = "";
		/*Context newContext;

		try {
			newContext = context.createPackageContext("com.android.launcher",Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			newContext = context;
		}	*/

        if (date != null) {
            java.text.DateFormat dateFormat = android.text.format.DateFormat
                    .getTimeFormat(context);
            dateStr = dateFormat.format(date);
        }

        return dateStr;
    }

    public static String dateToStringNoSpace(Date date) {

        String dateStr = "";

        if (date != null) {
            SimpleDateFormat sdfDateToStr = new SimpleDateFormat(
                    "yyyyMMddHHmmss");
            dateStr = sdfDateToStr.format(date);
        }

        return dateStr;
    }
}
