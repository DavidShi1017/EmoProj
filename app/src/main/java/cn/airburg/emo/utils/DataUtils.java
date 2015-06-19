package cn.airburg.emo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 保存监测数据工具类
 * Created by YangJie on 2015/1/19.
 */
public class DataUtils {
	/**
	 * 开始时间key
	 */
	public final static String KEY_START_DATETIME = "key_start_datetime" ;

	/**
	 * 结束时间
	 */
	public final static String KEY_END_DATETIME = "key_end_datetime" ;

	/**
	 * 数据
	 */
	public final static String KEY_DATA = "key_data" ;

	/**
	 * 是否为自动停止，true表示自动停止，false表示非自动停止
	 */
	public final static String KEY_AUTO_STOP = "key_auto_stop" ;

	/**
	 * y轴最大刻度
	 */
	public final static String KEY_Y_MAX_SCALE = "key_y_max_scale" ;

	/**
	 * Y轴刻度缩放比
	 */
	public final static String KEY_Y_SCALE_RATIO = "key_y_scale_ratio" ;

	/**
	 * 保存在SharedPreference中的key值
	 */
	private final static String KEY = "key_json_string" ;

	/**
	 * 电池电量key
	 */
	private final static String KEY_BATTERY = "key_battery_power" ;

	/**
	 * 保存数据
	 * @param jsonString json数据字符串
	 */
	public static void save(Context context, String jsonString) {
		SharedPreferences sp = context.getSharedPreferences("preference", Context.MODE_PRIVATE) ;
		SharedPreferences.Editor editor = sp.edit() ;
		editor.putString(KEY, jsonString) ;
		editor.commit() ;
	}

	/**
	 * 加载数据
	 * @param context 应用上下文
	 */
	public static String load(Context context) {
		SharedPreferences sp = context.getSharedPreferences("preference", Context.MODE_PRIVATE) ;
		return sp.getString(KEY, null) ;
	}

	/**
	 * 获取电池电量
	 * @param context 应用上下文
	 * @return 电池电量，如果上次没有保存那么返回-1
	 */
	public static int getBatteryPower(Context context) {
		SharedPreferences sp = context.getSharedPreferences("battery", Context.MODE_PRIVATE) ;
		return sp.getInt(KEY_BATTERY, 0) ;
	}


	/**
	 * 保存上一次电池电量
	 * @param context 应用上下文
	 * @param battery 电池电量
	 */
	public static void saveBatteryPower(Context context, int battery) {
		SharedPreferences sp = context.getSharedPreferences("battery", Context.MODE_PRIVATE) ;
		SharedPreferences.Editor editor = sp.edit() ;
		editor.putInt(KEY_BATTERY, battery) ;
		editor.commit() ;
	}
}
