package cn.airburg.emo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.airburg.emo.SelfApplication;

/**
 * Created by u17 on 2015/1/13.
 */
public class Utils {

	/**
	 * 获取屏幕相关属性
	 * @param activity activity
	 * @return
	 */
	public static DisplayMetrics getDisplayMetrics(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
//		float density = displayMetrics.density;     // 得到密度
//		float width = displayMetrics.widthPixels;   // 得到宽度
//		float height = displayMetrics.heightPixels; // 得到高度

		return displayMetrics ;
	}

	/**
	 * 获取和保存当前屏幕的截图
	 * @return 截屏图片保存地址
	 */
	public static Bitmap getCurrentScreenshotBitmap(Activity activity) {
		// 构建Bitmap  
		WindowManager windowManager = activity.getWindowManager() ;
		Display display = windowManager.getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		// 获取屏幕  
		View decorview = activity.getWindow().getDecorView();
		decorview.setDrawingCacheEnabled(true);
		bitmap = decorview.getDrawingCache();

		return bitmap ;
	}

	/**
	 * 获取和保存当前屏幕的截图
	 * @return 截屏图片保存地址
	 */
	public static String getAndSaveCurrentScreenshotImage(boolean isCameraScreenshotActivity, Activity activity, int width, int height) {
		// 构建Bitmap  
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// statusBarHeight是上面所求的状态栏的高度
		int titleBarHeight = contentTop - statusBarHeight;
		int cutHeight = titleBarHeight + statusBarHeight;
		int nHeight = height - cutHeight;

		// 获取屏幕  
		View decorView = activity.getWindow().getDecorView();
		decorView.setDrawingCacheEnabled(true);
		Bitmap b1 = decorView.getDrawingCache();

		Bitmap bitmap = Bitmap.createBitmap(b1, 0, cutHeight, width,  nHeight);
		//b1.recycle();
		decorView.destroyDrawingCache();
		String filepath;
		String date = DateUtils.dateToStringNoSpace(new Date());
		File dir;
		FileOutputStream fos;
        // 保存Bitmap   
		try {
			//文件  
			if (isCameraScreenshotActivity){
				dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "EMO");
				filepath = dir.getAbsolutePath() + File.separator + date + ".jpg";
			}else{
				dir = getPicturesPath(); // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				filepath = dir.getAbsolutePath() + "/Screenshot.jpg";
			}
			File file = new File(filepath);
			if(!dir.exists()){
				dir.mkdirs();
			}
			file.createNewFile();
			fos = new FileOutputStream(file);
			if (null != fos) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			LogUtils.e(e.getMessage());
			e.printStackTrace();
			filepath = null ;
		}
		return filepath ;
	}
	/**
	 * 获取SDCard的目录路径
	 * 如果存在SDCard，那么获取 /sdcard/Android/data/包名/Pictures 目录
	 * 否者获取 /data/data/包名/files/Pictures 目录
	 * @return 获取到的图片存储目录文件对象
	 */
	public static File getPicturesPath() {
		File picturesDir = null;
		//判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(sdcardExist) {
			picturesDir = SelfApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ;
		} else {
			picturesDir = SelfApplication.getContext().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE) ;
		}

		return picturesDir;
	}



	/**
	 * 保存全局异常信息
	 * @param exceptionString 全局异常信息
	 */
	public static void saveGlobalExceptionInfo(String exceptionString) {
		File docDir = null;
		//判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(sdcardExist) {
			docDir = SelfApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ;
		} else {
			docDir = SelfApplication.getContext().getDir(Environment.DIRECTORY_DOCUMENTS, Context.MODE_PRIVATE) ;
		}

		String filepath = null ;
		FileOutputStream fileOutStream = null;
		try {
			//文件  
			filepath = docDir.getAbsolutePath() + "/error.log";
			File file = new File(filepath);
			if(!docDir.exists()) {	// 创建不存在的目录
				docDir.mkdirs();
			}
			if (!file.exists()) { // 创建不存在的文件
				file.createNewFile();
			}

			fileOutStream = new FileOutputStream(file);
			if (null != fileOutStream) {
				fileOutStream.write(exceptionString.getBytes());
				fileOutStream.flush();
			}
		} catch (Exception e) {
			LogUtils.e(e.getMessage());
			e.printStackTrace();
			filepath = null ;
		} finally {
			try {
				fileOutStream.close() ;
				fileOutStream = null ;
			} catch (IOException e) {
				LogUtils.e(e.getMessage());
				e.printStackTrace();
			}
		}
	}


	/**
	 * 计算两个时间的分数差
	 * @param startDatetime 开始时间
	 * @param endDatetime 结束时间
	 * @return 分数差
	 */
	public static int computeMinuteDiffer(Date startDatetime, Date endDatetime) {
		// 开始时间秒数
		long start = startDatetime.getTime() / 1000 ;
		// 结束时间秒数
		long end = endDatetime.getTime() / 1000 ;

		// 相差分数
		Long minuteDiffer = (end - start) / 60 ;

		return minuteDiffer.intValue() ;
	}

	/**
	 * 计算中间时间
	 * @param startTime 开始时间戳
	 * @param endTime 结束时间戳
	 * @return 中间时间
	 */
	public static Date computeMiddleMinute(long startTime, long endTime) {
		long start = startTime - startTime % 1000 ;
		long end = endTime - endTime % 1000 ;

		long middle = (start + end ) / 2 ;

		Date date = new Date() ;
		date.setTime(middle);

		return date ;
	}

	/**
	 * 从长度为5的byte数组中解析出日期 分别占用一个字节 年 月 日 时 分 秒
	 * @param dateBytes 代表日期的byte数组
	 * @return 解析后的日期
	 */
	public static Date getDateFromByteArray(byte[] dateBytes) throws ParseException {
		if(null == dateBytes) {
			throw new IllegalStateException("the data byte array is null.") ;
		}

		StringBuilder stringBuilder = new StringBuilder("20") ;
		stringBuilder.append(dateBytes[0]<10?"0"+dateBytes[0]:dateBytes[0]).append('-') ;
		stringBuilder.append(dateBytes[1]<10?"0"+dateBytes[1]:dateBytes[1]).append('-') ;
		stringBuilder.append(dateBytes[2]<10?"0"+dateBytes[2]:dateBytes[2]).append(" ") ;
		stringBuilder.append(dateBytes[3]<10?"0"+dateBytes[3]:dateBytes[3]).append(':') ;
		stringBuilder.append(dateBytes[4]<10?"0"+dateBytes[4]:dateBytes[4]) ;

		LogUtils.d(" -> " + stringBuilder.toString());

		return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(stringBuilder.toString()) ;
	}

	/**
	 * 从Date对象回去byte array数据
	 * @param datetime 日期对象
	 * @return byte array，如果datatime为null返回null，否者返回封装对象
	 */
	public static byte[] getByteArrayFromDate(Date datetime) {
		if(null == datetime) {
			throw new IllegalStateException("the date object is null.") ;
		}

		Calendar calendar = Calendar.getInstance() ;
		calendar.setTime(datetime);

		byte[] dateByteArray = new byte[5] ;

		dateByteArray[0] = (byte)(calendar.get(Calendar.YEAR) - 2000) ;
		dateByteArray[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
		dateByteArray[2] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		dateByteArray[3] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		dateByteArray[4] = (byte) calendar.get(Calendar.MINUTE);

		LogUtils.d(JSON.toJSONString(dateByteArray[0]));
		LogUtils.d(JSON.toJSONString(dateByteArray[1]));
		LogUtils.d(JSON.toJSONString(dateByteArray[2]));
		LogUtils.d(JSON.toJSONString(dateByteArray[3]));
		LogUtils.d(JSON.toJSONString(dateByteArray[4]));

		return dateByteArray ;
	}


	/**
	 * 根据pm2.5的值获取提示文字字符串
	 * @param pm2dot5 pm2.5值
	 * @return 提示文字
	 */
	public static String getHazeValueDesc(int pm2dot5) {

		String pm2dot5Desc = "--" ;
		if(pm2dot5 < 36) {
			pm2dot5Desc = "良好" ;
		} else if(pm2dot5 < 76) {
			pm2dot5Desc = "正常" ;
		} else if(pm2dot5 < 116) {
			pm2dot5Desc = "轻度污染" ;
		} else if(pm2dot5 < 151) {
			pm2dot5Desc = "中度污染" ;
		} else if(pm2dot5 < 251) {
			pm2dot5Desc = "重度污染" ;
		} else if(pm2dot5 < 351) {
			pm2dot5Desc = "严重污染" ;
		} else {
			pm2dot5Desc = "不宜人类生存" ;
		}
		return pm2dot5Desc ;
	}
}
