package cn.airburg.emo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.airburg.emo.utils.DataUtils;
import cn.airburg.emo.utils.DisplayUtils;
import cn.airburg.emo.utils.LogUtils;
import cn.airburg.emo.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 画图View
 * Created by 杨杰 on 2015/1/13.
 */
public class GraphView extends View {
	/**
	 * 检测时间间隔，单位为：分钟
	 */
	public final static int MINUTE_GATHER_INTERVAL     =   10 ;

	/**
	 * Y轴最大刻度
	 */
	private final static int Y_MAX_SCALE				=	1000 ;

	/**
	 * Y轴最大刻度下等分线间隔刻度数量
	 */
	private final static int Y_MAX_INTERVAL_SCALE_COUNT =	Y_MAX_SCALE / 5 ;

	/**
	 * y轴刻度划分
	 */
	private final static int Y_SCALE_INCRENCE_RATE 		=	50  ;

	/**
	 * 画图的高度
	 */
	private int mHeight ;

	/**
	 * 字体大小
	 */
	private int mFontSize ;

	/**
	 * X轴最大刻度时，对应的最小单位刻度像素长度
	 */
	private float mXPerScalePixelLength;

	/**
	 * Y轴最大刻度时，对应的最小单位刻度像素长度
	 */
	private float mYPerScalePixelLength;

	/**
	 * 刻度文字距离上外边距
	 */
	private int mScaleTextMarginTop ;

	/**
	 * 刻度文字距离左外边距
	 */
	private int mScaleTextMarginLeft ;

	/**
	 * Y轴最大刻度
	 */
	private float mYMaxScale = Y_MAX_SCALE ;

	/**
	 * y轴放大比，默认值为1.0f
	 */
	private float mYScaleRatio = 1.0f ;

	/**
	 * 布局或者是画图 的上边距
	 */
	private int mLayoutMarginTop ;

	/**
	 * 检测的开始时间
	 */
	private Date mStartDatetime ;

	/**
	 * 检测的结束时间
	 */
	private Date mEndDatetime ;

	/**
	 * 测得的雾霾数据集合
	 */
	private List<Integer> mHazeDataArray = new ArrayList<Integer>();

	/**
	 * 绘图handler
	 */
	private Handler mDrawingHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0x1234) {

				// 绘图前 计算Y轴最大刻度以及Y轴的缩放比
				computeYMaxScaleAndRatio(getMaxHazeValue());

				GraphView.this.invalidate();
			}
		};
	};

	private Context mContext;

	private DisplayMetrics displayMetrics ;

	/**
	 * 时间格式化类对象
	 */
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd") ;

	/**
	 * 时间格式化类对象
	 */
	private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm") ;

	/**
	 * 停止采集监听器
	 */
	private OnStopGather mOnStopGather ;

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.mContext = context ;

		// 获取屏幕相关信息
		this.displayMetrics = Utils.getDisplayMetrics((Activity)context) ;

		if(displayMetrics.widthPixels > 1080 ) {
			this.mHeight = 200;
		} else if(displayMetrics.widthPixels == 1080) {
			// 高度
			this.mHeight = 180;
		} else if(displayMetrics.widthPixels >= 720) {
			this.mHeight = 120;
		} else if(displayMetrics.widthPixels >= 480)  {
			this.mHeight = 65;
		} else if(displayMetrics.widthPixels < 480 ) {
			this.mHeight = 50;
		}

		// 设置字体大小
		this.mFontSize = DisplayUtils.sp2Dip(context, 12) ;
		LogUtils.d(" fontSize：" + this.mFontSize + " px");

		this.mXPerScalePixelLength = displayMetrics.widthPixels ;

		this.mYPerScalePixelLength = (float) (this.mHeight * 1.0 / Y_MAX_INTERVAL_SCALE_COUNT) ;

		// 文字距离上面线的距离
		this.mScaleTextMarginTop = this.mFontSize + DisplayUtils.dip2px(mContext, 1) ;

		// 文字距离屏幕左边缘的距离
		this.mScaleTextMarginLeft = DisplayUtils.dip2px(mContext, 3) ;

		// 绘图区域上边距 距离
		this.mLayoutMarginTop = DisplayUtils.dip2px(mContext, 1) ;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 画虚线 画笔
		Paint deshPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		deshPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		deshPaint.setColor(Color.WHITE);
		deshPaint.setStrokeWidth(DisplayUtils.dip2px(mContext, 1)) ;
//		deshPaint.setAntiAlias(true); // 去锯齿
		deshPaint.setAlpha((int)(255*0.7));

		Path deshPath = new Path() ;
		// 虚线效果
		PathEffect effects = new DashPathEffect(new float[] { 3, 3, 3, 3 }, 0);
		deshPaint.setPathEffect(effects);

		// 文字画笔
		Paint textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setStrokeWidth(2) ;
		textPaint.setAntiAlias(true); // 去锯齿
		textPaint.setTextSize(this.mFontSize);
		textPaint.setAlpha(153);

		for (int i=0; i<5; i++) {
//			deshPath = new Path() ;
			deshPath.moveTo(0, i*mHeight + this.mLayoutMarginTop );
			deshPath.lineTo(this.displayMetrics.widthPixels, i*mHeight + this.mLayoutMarginTop );
			// canvas.drawLine(0, i*mHeight, this.displayMetrics.widthPixels, i*mHeight, deshPaint);

			// 画虚线
			canvas.drawPath(deshPath, deshPaint);

			// 画刻度文字
			canvas.drawText(String.valueOf((int)(mYMaxScale/5*(5-i))), mScaleTextMarginLeft, i*mHeight + this.mLayoutMarginTop + mScaleTextMarginTop, textPaint);
		}

		// 显示开始时间
		if(null != mStartDatetime) {
			canvas.drawText(mDateFormat.format(mStartDatetime), displayMetrics.widthPixels-DisplayUtils.dip2px(mContext, 65), mScaleTextMarginTop + this.mLayoutMarginTop , textPaint);
		}


		// 绘折线图 以及 折线和X轴组成的区域
		drawBrokenLineGraph(canvas);

		// 画x周以及坐标
		drawXaxis(canvas);
	}

	/**
	 * 绘折线图 以及 折线和X轴组成的区域
	 */
	private void drawBrokenLineGraph(Canvas canvas) {
		// 绘折线 画笔
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true); // 去锯齿
		paint.setColor(0xFFF0F0F0);
		paint.setStrokeWidth(DisplayUtils.dip2px(mContext, 1));

		// 折线和X周覆盖区域 画笔
		Paint paint2 = new Paint();
		paint2.setColor(0xFFF0F0F0);
		paint2.setStyle(Paint.Style.FILL);
		paint2.setAlpha((int)(255*0.3));    // 设置透明度

		float baseX = 0 ;
		float baseY = (float) (5*mHeight - DisplayUtils.dip2px(mContext, 0.5f) );

		if(mHazeDataArray.size() == 1) {
		    Path path = new Path();
		    path.moveTo(baseX, baseY - mHazeDataArray.get(0) * getDrawYHeight());
		    path.lineTo(displayMetrics.widthPixels, baseY - mHazeDataArray.get(0) * getDrawYHeight());

		    Path path2 = new Path();
		    path2.moveTo(baseX, baseY);
		    path2.lineTo(baseX, baseY - mHazeDataArray.get(0) * getDrawYHeight());

		    path2.lineTo(displayMetrics.widthPixels, baseY - mHazeDataArray.get(0) * getDrawYHeight());
		    path2.lineTo(displayMetrics.widthPixels, baseY);

		    // 画折线
		    canvas.drawPath(path, paint);

		    // 画填充
		    canvas.drawPath(path2, paint2);
		}

		if (mHazeDataArray.size() > 1) {
			Path path = new Path();
			path.moveTo(baseX, baseY - mHazeDataArray.get(0) * getDrawYHeight());

			Path path2 = new Path();
			path2.moveTo(baseX, baseY);
			path2.lineTo(baseX, baseY - mHazeDataArray.get(0) * getDrawYHeight());

			for (int i = 1; i < mHazeDataArray.size(); ++i) {
				if(mHazeDataArray.size() == i+1) {
					path.lineTo(displayMetrics.widthPixels, baseY - mHazeDataArray.get(i) * getDrawYHeight());
					path2.lineTo(displayMetrics.widthPixels, baseY - mHazeDataArray.get(i) * getDrawYHeight());
				} else {
					path.lineTo(baseX + i * mXPerScalePixelLength, baseY - mHazeDataArray.get(i) * getDrawYHeight());
					path2.lineTo(baseX + i * mXPerScalePixelLength, baseY - mHazeDataArray.get(i) * getDrawYHeight());
				}
			}
//			path.lineTo(this.displayMetrics.widthPixels, baseY - mHazeDataArray.get(mHazeDataArray.size()-1) * mYPerScalePixelLength);
//			path2.lineTo(this.displayMetrics.widthPixels, baseY - mHazeDataArray.get(i) * mYPerScalePixelLength);

			path2.lineTo(displayMetrics.widthPixels, baseY);

			canvas.drawPath(path, paint);

			// 画填充
			canvas.drawPath(path2, paint2);
		}
	}

	/**
	 * 画X轴已经坐标点
 	 */
	private void drawXaxis(Canvas canvas) {
		// 画X轴实线 以及 3个刻度
		Paint fullPaint = new Paint();
		fullPaint.setColor(Color.WHITE);
		fullPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		fullPaint.setStrokeWidth(DisplayUtils.dip2px(mContext, 1)) ;
		fullPaint.setAntiAlias(true); // 去锯齿
		fullPaint.setAlpha((int) (255 * 0.7));

		Path xPath = new Path() ;
		xPath.moveTo(0, 5*this.mHeight);
		xPath.lineTo(this.displayMetrics.widthPixels, 5*mHeight);

		// 画x轴实线
		canvas.drawPath(xPath, fullPaint);

		// 画刻度
		Path scalePath = new Path() ;
		for (int i=0; i<3; ++i) {
			scalePath.moveTo(this.displayMetrics.widthPixels/4 *(i+1), 5*this.mHeight);
			scalePath.lineTo(this.displayMetrics.widthPixels/4 *(i+1), 5*this.mHeight+DisplayUtils.dip2px(mContext, 2));
			canvas.drawPath(scalePath, fullPaint);
		}


		// 刻度时间
		// 画笔
		Paint textPaint10sp = new Paint();
		textPaint10sp.setColor(Color.WHITE);
		textPaint10sp.setStyle(Paint.Style.FILL);
		textPaint10sp.setStrokeWidth(2) ;
		textPaint10sp.setAntiAlias(true); // 去锯齿
		textPaint10sp.setTextSize(DisplayUtils.sp2Dip(mContext, 10));
		textPaint10sp.setAlpha(153);

		// 最左侧的开始时间
		if(null != mStartDatetime) {
			canvas.drawText(mTimeFormat.format(mStartDatetime), mScaleTextMarginLeft, mScaleTextMarginTop + 5*mHeight + this.mLayoutMarginTop , textPaint10sp);
		}
		// 最右侧的结束时间
		if(null != mEndDatetime) {
			canvas.drawText(mTimeFormat.format(mEndDatetime), displayMetrics.widthPixels-DisplayUtils.dip2px(mContext, 29), mScaleTextMarginTop + 5*mHeight + this.mLayoutMarginTop , textPaint10sp);
		}

		if(null != mStartDatetime && null != mEndDatetime && Utils.computeMinuteDiffer(this.mStartDatetime, this.mEndDatetime) > MINUTE_GATHER_INTERVAL) {
			Date[] dateArray = new Date[3] ;
			dateArray[1] = Utils.computeMiddleMinute(mStartDatetime.getTime(), mEndDatetime.getTime()) ;
			dateArray[0] = Utils.computeMiddleMinute(mStartDatetime.getTime(), dateArray[1].getTime()) ;
			dateArray[2] = Utils.computeMiddleMinute(dateArray[1].getTime(), mEndDatetime.getTime()) ;

			float space = (float) (displayMetrics.widthPixels / 4.0 ); // 间距
			// 画中间时间
			for (int i=0; i<3; ++i) {
				canvas.drawText(mTimeFormat.format(dateArray[i]), space*(i+1) - DisplayUtils.dip2px(mContext, 13), mScaleTextMarginTop + 5*mHeight + this.mLayoutMarginTop , textPaint10sp);
			}
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			LogUtils.d("height mode is AT_MOST");
		} else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
			LogUtils.d("height mode is EXACTLY");
		} else {
			LogUtils.d("height mode is UNSPECIFIED");
		}


//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int measuredHeight = measureHeight(heightMeasureSpec);
//
		// 宽度不变
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
//
		setMeasuredDimension(measuredWidth, measuredHeight);
		//final DisplayMetrics metrics = getResources().getDisplayMetrics();
		//setMeasuredDimension(mBitmap.getScaledWidth(metrics),mBitmap.getScaledHeight(metrics));

	}

	private int measureHeight(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.AT_MOST) {
			return this.mHeight * 5 + mScaleTextMarginTop + this.mLayoutMarginTop * 3  ;
		}
		return specSize;
	}

	/**
	 * 获取Y坐标的高度
	 * 由于最大刻度发生了变化，那么相当于最大刻度进行了缩放，相应的最大刻度下的单位刻度对应的像素长度
	 * 也需要进行相同比例的缩放
	 * @return Y坐标的高度
	 */
	private float getDrawYHeight() {
		return mYScaleRatio * mYPerScalePixelLength;
	}

	/**
	 * 计算x轴方向刻度
	 */
	private void computeXScale() {
		int minuteDiffer = Utils.computeMinuteDiffer(this.mStartDatetime, this.mEndDatetime) ;
		if(minuteDiffer < MINUTE_GATHER_INTERVAL) {
			this.mEndDatetime = this.mStartDatetime ;
			this.mXPerScalePixelLength = displayMetrics.widthPixels ;
		} else {
			if (minuteDiffer == MINUTE_GATHER_INTERVAL) {
				this.mXPerScalePixelLength = displayMetrics.widthPixels ;
			} else {
				this.mXPerScalePixelLength = displayMetrics.widthPixels / (minuteDiffer / MINUTE_GATHER_INTERVAL);
			}
		}
	}

	/**
	 * 计算Y轴最大刻度以及Y轴的缩放比
	 * @param maxHazeValue 测得的最大雾霾值
	 */
	private void computeYMaxScaleAndRatio(int maxHazeValue) {
		if(0 != maxHazeValue) {
			this.mYMaxScale = (maxHazeValue/Y_SCALE_INCRENCE_RATE + (maxHazeValue%Y_SCALE_INCRENCE_RATE!=0?1:0)) * Y_SCALE_INCRENCE_RATE ;
			this.mYScaleRatio = (float) (Y_MAX_SCALE / mYMaxScale);
		}

		LogUtils.d("mYMaxScale: " + mYMaxScale);
		LogUtils.d("this.mYScaleRatio: " + this.mYScaleRatio);
	}

	/**
	 * 重置绘图
	 */
	public void reset() {

		// 清空所有的数据
		this.mHazeDataArray.clear();
	}

	/**
	 * 获取最大的雾霾测量值
	 * @return 最大的雾霾测量值
	 */
	public int getMaxHazeValue() {
		int maxHazeValue = 0 ; // 最大的雾霾测量值

		// 从集合中查找最大的雾霾测量值
		for(int hazeValue : this.mHazeDataArray) {
			if(hazeValue > maxHazeValue ) {
				maxHazeValue = hazeValue ;
			}
		}

		return maxHazeValue ;
	}

	/**
	 * 添加数据
	 * @param hazeValue 测得的雾霾数据
	 */
	public void add(int hazeValue) {
		LogUtils.d("hazeValue: " + hazeValue);

		// 监测到的数值，如果大于了Y_MAX_SCALE，那么就等于Y_MAX_SCALE
		if(hazeValue > Y_MAX_SCALE) {
			hazeValue = Y_MAX_SCALE ;
		}

		this.mHazeDataArray.add(hazeValue);

		// 时间已经达到了12个小时，那么停止检测
		if (Utils.computeMinuteDiffer(mStartDatetime, mEndDatetime) >= 12*60) {
			doStop(true);
			notifyStopGather() ;
		}

		// 发送绘图消息
		mDrawingHandler.sendEmptyMessage(0x1234);
	}

	/**
	 * 批量添加监测数据
	 * @param monitorHazeValueList 监测数据列表
	 */
	public void add(List<Integer> monitorHazeValueList) {
		reset() ;

		LogUtils.d("monitorHazeValueList: " + JSON.toJSONString(monitorHazeValueList));

		for (Integer hazeValue : monitorHazeValueList) {
			// 监测到的数值，如果大于了Y_MAX_SCALE，那么就等于Y_MAX_SCALE
			if (hazeValue > Y_MAX_SCALE) {
				hazeValue = Y_MAX_SCALE;
			}

			this.mHazeDataArray.add(hazeValue);
		}

		// 时间已经达到了12个小时，那么停止检测
		if (Utils.computeMinuteDiffer(mStartDatetime, mEndDatetime) >= 12*60) {
			doStop(true);
			notifyStopGather() ;
		}

		// 发送绘图消息
		mDrawingHandler.sendEmptyMessage(0x1234);
	}

	/**
	 * 加载数据
	 * @return 返回上次结束是否为自动结束或者上次计时的时间到现在已经超过了12个小时，那么返回true；否者返回false
	 */
	public boolean doLoad() {
		String jsonString = DataUtils.load(mContext) ;

		if(!TextUtils.isEmpty(jsonString)) {
			JSONObject jsObject = JSON.parseObject(jsonString) ;
			mYMaxScale = jsObject.getFloat(DataUtils.KEY_Y_MAX_SCALE) ;
			mYScaleRatio = jsObject.getFloat(DataUtils.KEY_Y_SCALE_RATIO) ;
			mStartDatetime = jsObject.getDate(DataUtils.KEY_START_DATETIME) ;
			mEndDatetime = jsObject.getDate(DataUtils.KEY_END_DATETIME) ;

			computeXScale() ;

			JSONArray jsArray = jsObject.getJSONArray(DataUtils.KEY_DATA) ;
			for (int i=0; i<jsArray.size(); ++ i) {
				mHazeDataArray.add(jsArray.getInteger(i));
			}

//			boolean autoStop = jsObject.getBoolean(DataUtils.KEY_AUTO_STOP) ;

			// 有待实现
		}

		return true ;
	}

	/**
	 * 停止监测数据
	 * @param autoStop 是否为自动停止，true：自动停止；false：非自动停止
	 */
	public void doStop(boolean autoStop) {
		// 保存数据
		JSONObject jsObject = new JSONObject() ;
		jsObject.put(DataUtils.KEY_START_DATETIME, mStartDatetime) ;
		jsObject.put(DataUtils.KEY_END_DATETIME, mEndDatetime) ;
//		jsObject.put(DataUtils.KEY_AUTO_STOP, autoStop) ;
		jsObject.put(DataUtils.KEY_Y_MAX_SCALE, this.mYMaxScale) ;
		jsObject.put(DataUtils.KEY_Y_SCALE_RATIO, this.mYScaleRatio) ;
		jsObject.put(DataUtils.KEY_DATA, mHazeDataArray.toArray()) ;

		LogUtils.d(jsObject.toJSONString());

		// 保存数据
		DataUtils.save(mContext, jsObject.toJSONString());
	}

	/**
	 * 通知监测停止的观察者
	 */
	private void notifyStopGather() {
		// 通知观察者，已经停止监测
		if(null != mOnStopGather) {
			mOnStopGather.onStop();
		}
	}

	public void setStartDatetime(Date startDatetime) {
		this.mStartDatetime = startDatetime;
	}

	public Date getStartDatetime() {
		return this.mStartDatetime ;
	}

	public void setEndDatetime(Date endDatetime) {
		this.mEndDatetime = endDatetime;
		computeXScale() ;
	}

	public void setOnStopGather(OnStopGather onStopGather) {
		this.mOnStopGather = onStopGather;
	}

	/**
	 * 停止采集监听器接口
	 */
	public interface OnStopGather {
		void onStop() ;
	}
}
