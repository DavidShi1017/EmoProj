package cn.airburg.emo.activity;

import com.alibaba.fastjson.JSON;
import com.umeng.update.UmengUpdateAgent;

import cn.airburg.emo.R;
import cn.airburg.emo.adapter.SelfPageAdapter;
import cn.airburg.emo.dialog.DownToUpDialog;

import cn.airburg.emo.listeners.DialogActionCallBack;
import cn.airburg.emo.manager.AppManager;
import cn.airburg.emo.manager.ContinuousMonitoringManager;
import cn.airburg.emo.manager.SelfManager;
import cn.airburg.emo.manager.ShareManager;
import cn.airburg.emo.manager.TaskManager;
import cn.airburg.emo.model.EMOStatus;
import cn.airburg.emo.parser.DeviceTypeOneParser;
import cn.airburg.emo.parser.DeviceTypeZeroParser;
import cn.airburg.emo.parser.HazeParser;
import cn.airburg.emo.service.BluetoothLeService;
import cn.airburg.emo.utils.ByteUtils;
import cn.airburg.emo.utils.DataUtils;
import cn.airburg.emo.utils.DialogUtils;

import cn.airburg.emo.utils.LocationUtils;
import cn.airburg.emo.utils.LogUtils;
import cn.airburg.emo.utils.Utils;
import cn.airburg.emo.view.GraphView;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;

import android.support.v4.app.FragmentActivity;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;


import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends Activity {

    private PowerManager.WakeLock mWakeLock = null ;

    private final static int REQUEST_ENABLE_BT = 1 ;

    private final static int REQUEST_CAREMA = 2 ;

    private final static int REQUEST_Photo= 3 ;
	/**
	 * page one 动画时长
	 */
	private final static long PAGEONE_ANIMATION_TIME = 500 ;

	/**
	 * 电池电量采集时间间隔
	 */
	private final static int BATTERY_POWER_GATHER_INTERVAL = 1 * 60 * 1000 ;

	/**
	 * 监控写入数据时间
	 */
	private final static int MONITOR_WRITE_DATA_TIME = 5 * 1000 ;

	/**
	 * Stops scanning after 10 seconds.
	 */
    private static final long SCAN_PERIOD = 10 * 1000 ;

	/**
	 * 连续监测任务 开始
	 */
	private static final int MONITOR_TASK_START = 0x0001 ;

	/**
	 * 连续监测任务 结束
	 */
	private static final int MONITOR_TASK_STOP = 0x0002 ;

    private ViewPager mViewPager;

    private List<View> mViewList = new ArrayList<View>(2) ;

    private View mPageOne = null ;

    private View mPageTwo = null ;

    private PagerAdapter mPageAdapter;

    /**
     * 动画背景 前背景
     */
    private View mLayoutAnimPageOneForeground ;

    /**
     * 当前颜色资源
     */
    private int mCurrentColorRes  ;

    /**
     * 电量文本显示Layout
     */
    private LinearLayout mLinearLayoutPageOneBatteryText ;

    /**
     * 电量文本显示
     */
    private TextView mTvPagerOneBattery ;

    /**
     * 显示PM2.5值
     */
    private TextView mTvPageOneHazeValue;

	/**
	 * 显示PM2.5描述  前
	 */
	private TextView mTvPageOneHazeValueForeDesc;

	/**
	 * 显示PM2.5描述 后
	 */
	private TextView mTvPageOneHazeValueBackDesc;

	/**
	 * 电池电量不足提示
	 */
	private TextView mTvPagerOneBatteryWarn ;

    /**
     * 电池剩余电量layout
     */
    private LinearLayout mLinearLayoutPageOneBattery ;

    /**
     * 电池剩余电量
     */
    private ProgressBar mProgressPageOneBattery ;

	/**
	 * 画折线图的view
	 */
	private GraphView mGraphView ;

	/**
	 * 雾霾数据收集开始按钮
	 */
	private Button mBtnPageTwoStartGather ;

	/**
	 * 监测状态文字背景
	 */
	private View mLayoutPagerTwoStateText ;

	/**
	 * 监测状态文字
	 */
	private TextView mTvPagerTwoStateText ;

    /**
     * 回调管理handler
     */
    private Handler mCallbackHandler;

	/**
	 * 状态处理handler
	 */
	private Handler mStateProcessHandler ;

    /**
     * 扫描BLE设备状态，true表示正在扫描，false表示未少吗
     */
    private boolean mScanning;

	/**
	 * 是否打开了蓝牙，默认未打开
	 */
	private Boolean mIsOpenedBluetooth = null ;

    /**
     * 是否支持低功耗蓝牙功能，默认不支持
     */
    private boolean mIsSupportBle = false ;

    /**
     * 本机蓝牙设备
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * 扫描回调函数对象
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null ;

    /**
     * 目标通信设备
     */
    private BluetoothDevice mBluetoothDevice = null ;

    /**
     * 负责与 蓝牙设备连接、通信的 Service
     */
    private BluetoothLeService mBluetoothLeService ;

    /**
     * 当前Activity 与 负责与蓝牙进行连接、通信的Service 连接通道
     */
    private ServiceConnection mServiceConnection ;

    /**
     * 蓝牙设备 连接、断开链接、读取数据、写数据等 回调对象 其实就是一个监听器对象
     */
    private DCServiceCallback mDCServiceCallback = new DCServiceCallback() ;

    /**
     * 电池service
     */
    private BluetoothGattService mBatteryGattService ;

    /**
     * pm2.5 service
     */
    private BluetoothGattService mHazeValueGattService;

    /**
     * 是否和远程的蓝牙低功耗设备建立了连接
     */
    private boolean mConnectedGATT = false ;

    /**
     * 采集电池信息状态，默认未采集 false
     */
    private boolean mGatherBatteryPowerState = false ;

    /**
     * 采集雾霾数据状态，默认未采集 false
     */
    private boolean mGatherHazeValueState = false ;

    /**
     * 雾霾数据解析接口对象
     */
    private HazeParser mHazeParser ;

	/**
	 * pm2.5测出来的数值
	 */
	private short mHazeValue = 0 ;

	/**
	 * 实时测得的电量
	 */
	private int mBatteryPower = 0 ;

	/**
	 * 蓝牙状态接收者
	 */
	private BroadcastReceiver mBluetoothStateChangeReceiver ;

	/**
	 * home键点击事件接收者
	 */
	private BroadcastReceiver mHomeKeyEventReceiver ;

    /**
     * 关闭屏幕广播消息接收这对象
     */
    private BroadcastReceiver mScreenOnOrOffEventReceiver;

	/**
	 * 连续监测管理器
	 */
	private ContinuousMonitoringManager mContinuousMonitoringManager ;

    /**
     * 隐藏 电池电量显示文本 任务
     */
    private Runnable mHideBatteryTipRunnable  ;

	/**
	 * 读取 监控状态runnable
	 */
	private Runnable mReadMonitorSwitchResultRunnable;

	/**
	 * 写入 监控状态runnable
	 */
	private Runnable mWriteMonitorSwitchResultRunnable;

	/**
	 * 写入 关闭监控状态runnable
	 */
	private Runnable mCloseMonitorSwitchResultRunnable ;

	/**
	 * 读取 连续监测开始时间 runnable
	 */
	private Runnable mReadMonitorStartDatetimeResultRunnable ;

	/**
	 * 写入 监控开始时间runnable
	 */
	private Runnable mWriteMonitorStartDatetimeResultRunnable ;

	/**
	 * 读取 监控数据notify状态 runnable
	 */
	private Runnable mReadMonitorDataNotifyResultRunnable ;

	/**
	 * 写入 获取监控数据notify runnable
	 */
	private Runnable mWriteReadMonitorDataNotifyResultRunnable ;

    /**
     * 读取设备类型超时
     */
    private Runnable mReadDeviceTypeTimeoutRunnable ;

	/**
	 * 重试扫描设备
	 */
	private Runnable mRetryScanLEDeviceRunnable ;

    /**
     * 连接emo设备超时runnable
     */
    private Runnable mConnectEMODeviceTimeoutRunnable ;

    /**
     * 发现emo设备服务超时runnable
     */
    private Runnable mEMOServicesDiscoveredTimeoutRunnable ;

    /**
     * 扫描设备超时 runnable
     */
    private Runnable mScanLeDeviceTimeoutRunnable ;


    private final String LIST_NAME = "NAME";

    private final String LIST_UUID = "UUID";

	/**
	 * 信息对话框
	 */
	private Dialog mCaptionDialog;



	/**
	 * 分享管理器
	 */
	private ShareManager mShareManager ;

	/**
	 * 连续监测任务列表
	 */
	private List<Integer> mMonitorTaskList = new ArrayList<Integer>(5) ;

    /**
     * emo软件状态
     */
    private EMOStatus mEMOStatus ;

    //用于判断程序是否在后台运行
    private boolean isBackgrond;


    private boolean isConnectionSuccessful;

    private Timer timer;
    private TimerTask task;
    private boolean isWarmUp = true;


   // public final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//	    requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(cn.airburg.emo.R.layout.activity_main);

        //Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
        AppManager.getAppManager().addActivity(this);
	    LogUtils.d(" -> ");

        // 屏幕常亮
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG_emo");

	    // 注册home键点击事件广播接收
	    this.mHomeKeyEventReceiver = new SelfHomeKeyEventReceiver() ;
	    registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        // 注册屏幕开启和关闭广播消息事件接收
        this.mScreenOnOrOffEventReceiver = new SelfScreenOnOrOffEventReceiver() ;
        IntentFilter screenOnOrOffFilter = new IntentFilter() ;
        screenOnOrOffFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenOnOrOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOnOrOffEventReceiver, screenOnOrOffFilter) ;

	    // 友盟自动更新
	    UmengUpdateAgent.setDeltaUpdate(false) ;    // 全量更新
	    UmengUpdateAgent.setUpdateOnlyWifi(false);
	    UmengUpdateAgent.update(this);

	    // 判断设备是否支持 蓝牙低功耗功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            Toast.makeText(this, cn.airburg.emo.R.string.ble_not_supported, Toast.LENGTH_SHORT).show();

        } else {

            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {

                Toast.makeText(this, cn.airburg.emo.R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();

            } else {
				// 注册蓝牙状态改变接收者
	            registerBluetoothStateChangeReceiver() ;

                // 初始化状态
                this.mEMOStatus = EMOStatus.STATUS_INIT ;
                LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

                // 支持蓝牙低功耗功能
                this.mIsSupportBle = true ;

                this.mLeScanCallback = new SelfLeScanCallback() ;

                this.mCallbackHandler = new Handler();

	            // 消息状态处理handler
	            this.mStateProcessHandler = new SelfHandler() ;

                // 异常电池提示
                this.mHideBatteryTipRunnable = new HideBatteryTipRunnable() ;

                // 初始化view
                initView();

                // 初始化动画
                initAnimation() ;

                // 注册监听器
                registerListener();

	            // 连续监测管理器对象
	            this.mContinuousMonitoringManager = new ContinuousMonitoringManager(this.mGraphView);

	            // 读取监控状态runnable
	            this.mReadMonitorSwitchResultRunnable = new ReadMonistorSwitchResultRunnable() ;

	            // 写入监控状态 runnable
	            this.mWriteMonitorSwitchResultRunnable = new WriteMonitorSwitchResultRunnable() ;

	            // 写入 关闭监控状态 runnable
	            this.mCloseMonitorSwitchResultRunnable = new CloseMonitorSwitchResultRunnable() ;

	            // 读取连续监控开始时间 runnable
	            this.mReadMonitorStartDatetimeResultRunnable = new ReadMonitorStartDatetimeResultRunnable() ;

	            // 写入监控开始时间 runnable
	            this.mWriteMonitorStartDatetimeResultRunnable = new WriteMonitorStartDatetimeResultRunnable() ;

	            // 读取监控数据notify runnable
	            this.mReadMonitorDataNotifyResultRunnable = new ReadMonitorDataNotifyResultRunnable() ;

	            // 写入 获取监控数据notify runnable
				this.mWriteReadMonitorDataNotifyResultRunnable = new WriteReadMonitorDataNotifyResultRunnable() ;

	            // 加载数据
	            mGraphView.doLoad() ;

	            if(!mConnectedGATT) {
		            // 发送 初始化消息
		            mStateProcessHandler.sendEmptyMessage(SelfHandler.INIT_STATE);
	            }

            }
        }

//        throw new RuntimeException("测试一下") ;
    }

	/**
	 * 注册广播接收这
	 */
	private void registerBluetoothStateChangeReceiver() {
		mBluetoothStateChangeReceiver = new BluetoothStateChangeReceiver() ;

		IntentFilter stateChangeFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBluetoothStateChangeReceiver, stateChangeFilter);
//		IntentFilter connectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
//		registerReceiver(mBluetoothStateChangeReceiver, connectedFilter);
//		IntentFilter disConnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//		registerReceiver(mBluetoothStateChangeReceiver, disConnectedFilter);
	}

    /**
     * 初始化View
     */
    private void initView() {
        this.mViewPager = (ViewPager) this.findViewById(cn.airburg.emo.R.id.viewPager);

        LayoutInflater lf = getLayoutInflater().from(this);
        this.mPageOne = lf.inflate(cn.airburg.emo.R.layout.pager_one, null) ;
        this.mPageTwo = lf.inflate(cn.airburg.emo.R.layout.pager_two, null) ;

        mViewList.add(this.mPageOne) ;
        mViewList.add(this.mPageTwo) ;

        this.mPageAdapter = new SelfPageAdapter(this.mViewList) ;

        this.mViewPager.setAdapter(this.mPageAdapter);

        // 动画背景前背景
        this.mLayoutAnimPageOneForeground = (View) this.mPageOne.findViewById(R.id.layoutAnimPageOneForeground);

        // PM2.5
        this.mTvPageOneHazeValue = (TextView) this.mPageOne.findViewById(cn.airburg.emo.R.id.tvPagerOnePM2dot5Value);
        this.mTvPageOneHazeValue.setText("--");

		// 空气质量文字描述 前
        this.mTvPageOneHazeValueForeDesc = (TextView) this.mPageOne.findViewById(cn.airburg.emo.R.id.tvPagerOneHazeValueForeDesc);

	    // 空气质量文字描述 后
        this.mTvPageOneHazeValueBackDesc = (TextView) this.mPageOne.findViewById(cn.airburg.emo.R.id.tvPagerOneHazeValueBackDesc);
	    this.mTvPageOneHazeValueBackDesc.setVisibility(View.INVISIBLE);
//        this.mTvPagerOnePM2dot5Desc.setText("--");
//        this.mTvPagerOnePM2dot5Desc.setText(R.string.pageone_scan_connect_emo);

	    // 电池电量提示
	    this.mTvPagerOneBatteryWarn = (TextView) this.mPageOne.findViewById(cn.airburg.emo.R.id.tvPagerOneBatteryWarn);

        // 显示电量
        this.mTvPagerOneBattery = (TextView) this.mPageOne.findViewById(cn.airburg.emo.R.id.tvPagerOneBattery);
        this.mTvPagerOneBattery.setText("--");

        this.mLinearLayoutPageOneBatteryText = (LinearLayout) this.mPageOne.findViewById(cn.airburg.emo.R.id.layoutPagerOneBatteryText);
        this.mLinearLayoutPageOneBatteryText.setVisibility(View.INVISIBLE);

        // 电池电量显示
        this.mProgressPageOneBattery = (ProgressBar) this.mPageOne.findViewById(cn.airburg.emo.R.id.progressPageOneBattery);
        this.mProgressPageOneBattery.setMax(100);
        this.mProgressPageOneBattery.setVisibility(ProgressBar.VISIBLE);

        this.mLinearLayoutPageOneBattery = (LinearLayout) this.mPageOne.findViewById(cn.airburg.emo.R.id.layoutPageOneBattery);
	    this.mLinearLayoutPageOneBattery.setVisibility(View.INVISIBLE);


	    // 画折线图的view
	    this.mGraphView = (GraphView) this.mPageTwo.findViewById(cn.airburg.emo.R.id.gvPagerTwoView);
		// 开始收集按钮
	    this.mBtnPageTwoStartGather = (Button) this.mPageTwo.findViewById(cn.airburg.emo.R.id.btnPagerTwoStartOrStop);
	    this.mBtnPageTwoStartGather.setClickable(false);
	    // 监测状态文字背景框
	    this.mLayoutPagerTwoStateText = this.mPageTwo.findViewById(cn.airburg.emo.R.id.layoutPagerTwoStateText) ;
	    // 监测状态文字
	    this.mTvPagerTwoStateText = (TextView) this.mPageTwo.findViewById(cn.airburg.emo.R.id.tvPagerTwoState);



    }


    /**
     * 初始化动画
     */
    private void initAnimation() {
        // 当前颜色资源值
        this.mCurrentColorRes = R.color.blue ;
    }

	private TextView mTvMainTestResult ;
    /**
     * 注册监听器
     */
    private void registerListener() {
        WindowManager windowManager = getWindowManager() ;
        final Display display = windowManager.getDefaultDisplay();
	    mTvMainTestResult = (TextView) this.findViewById(cn.airburg.emo.R.id.tvMainTestResult);

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestReadSwitch).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 读取历史记录开关
			    TaskManager.getInstance().addTask(new TaskManager.Task() {
                    @Override
                    public void run() {

                        SelfManager.getInstance().readMonitoringSwitch(mBluetoothLeService);
                    }
                });
		    }
	    });

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestWriteSwitch).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 打开历史记录开关
			    SelfManager.getInstance().openMonitorSwitch(mBluetoothLeService);
		    }
	    });

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestCloseSwitch).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 关闭历史记录开关
			    SelfManager.getInstance().closeMonitorSwitch(mBluetoothLeService);
		    }
	    });

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestReadStartDatetime).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 读取开始时间
			    SelfManager.getInstance().readMonitorStartDatetime(mBluetoothLeService);
		    }
	    });

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestWriteStartDatetime).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) { // 写入开始时间
			    SelfManager.getInstance().writeMonitorStartDatetime(mBluetoothLeService, new Date());
		    }
	    });

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestHistoryReadSwitch).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 读取历史数据开关
			    SelfManager.getInstance().readMonitorReadSwitch(mBluetoothLeService);
		    }
	    });

	    this.findViewById(cn.airburg.emo.R.id.btnMainTestWriteHistoryReadSwitch).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 写入历史数据开关
			    SelfManager.getInstance().writeContinuousMonitoringHistoryReadSwitch(mBluetoothLeService);
		    }
	    });
	    this.findViewById(cn.airburg.emo.R.id.btnMainTestHistoryData).setOnClickListener(new View.OnClickListener() {
		    @Override public void onClick(View v) { // 读取历史数据
			    SelfManager.getInstance().readMonitorData(mBluetoothLeService);
		    }
	    });



        // page one 详细信息按钮
        this.mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneCaption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i("page one caption button click.");

                // 打开详细信息Activity
//                Intent intent = new Intent(MainActivity.this, CaptionActivity.class) ;
//                MainActivity.this.startActivity(intent);

	            if(null == mCaptionDialog) {

		            mCaptionDialog = new DownToUpDialog(MainActivity.this) ;
		            mCaptionDialog.setContentView(cn.airburg.emo.R.layout.activity_caption);
	            }

	            mCaptionDialog.show();

	            // 测试动画
//	            mHazeValue = 100 ;
//                startAnimation();
            }
        }) ;

        // page one 分享按钮
        this.mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneShare).setOnClickListener(new View.OnClickListener(){

            @Override public void onClick(View v) {
                LogUtils.d("page one share button click.");
                if (!isWarmUp){
                    String filepath = Utils.getAndSaveCurrentScreenshotImage(false, MainActivity.this, display.getWidth(), display.getHeight()) ;
                    // 分享截屏
                    mShareManager = new ShareManager(MainActivity.this);
                    mShareManager.shareScreenshot(filepath) ;
                }
            }
        });

        // page two 分享按钮
        this.mPageTwo.findViewById(cn.airburg.emo.R.id.btnPagerTwoShare).setOnClickListener(new View.OnClickListener(){

            @Override public void onClick(View v) {
                LogUtils.d("page two share button click.");
                if (!isWarmUp){
                    String filepath = Utils.getAndSaveCurrentScreenshotImage(false, MainActivity.this, display.getWidth(), display.getHeight()) ;

                    // 分享截屏
                    mShareManager = new ShareManager(MainActivity.this);
                    mShareManager.shareScreenshot(filepath) ;
                }

            }
        });

        // 打开相机
        this.mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneCamera).setOnClickListener(new View.OnClickListener(){

            @Override public void onClick(View v) {
                LogUtils.d("page one Camera button click.");

                if (!isWarmUp){
                    startCarema();
                }

            }
        });



        // 电池显示电量layout
        this.mLinearLayoutPageOneBattery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLinearLayoutPageOneBatteryText.setVisibility(View.VISIBLE);
                mCallbackHandler.removeCallbacks(mHideBatteryTipRunnable);
                mCallbackHandler.postDelayed(mHideBatteryTipRunnable, 2000) ;
            }
        });

        // 设置页面滚动改变监听器
        this.mViewPager.setOnPageChangeListener(new SelfPageChangeListener());

		// 设置停止采集监听器对象
	    this.mGraphView.setOnStopGather(new GraphView.OnStopGather(){
		    @Override
		    public void onStop() {
			    stopTimingGatherPM2dot5Thread();

			    // 设置按钮状态
			    mBtnPageTwoStartGather.setText(cn.airburg.emo.R.string.pagetwo_gather_operat_button_start_text);
//			    mTvPagerTwoStateText.setText(R.string.pagetwo_gather_state_no_text);
			    mLayoutPagerTwoStateText.setVisibility(View.INVISIBLE);
			    mBtnPageTwoStartGather.setBackgroundResource(cn.airburg.emo.R.drawable.botton_start);
			    mBtnPageTwoStartGather.setTextColor(0xFF606060);
		    }
	    });

	    // 开始收集按钮
	    this.mBtnPageTwoStartGather.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {

			    // 判断是否连接了蓝牙，还没有连接到蓝牙
			    if (!mConnectedGATT) {
				    // 还没有和emo建立连接
				    Toast.makeText(MainActivity.this, cn.airburg.emo.R.string.pagetwo_not_connected_emo_tip, Toast.LENGTH_SHORT).show();

			    } else {
				    if (mContinuousMonitoringManager.isStateMonitor()) { // 正在采集

					    DialogUtils.showConfirmDialog(MainActivity.this, "确定要结束监测吗？", "已记录的曲线将一直保留，直到下一次记录。", new DialogActionCallBack() {
						    @Override public void onOkAction() {

							    // 显示对话框
//							    DialogUtils.showProgressDialog(MainActivity.this, "正在等待emo设备停止连续监测...") ;

							    stopTimingGatherPM2dot5Thread();
							    mGraphView.doStop(true);

							    // 关闭监控状态
//							    closeMonitorSwitch();
							    // 执行连续监测 stop 任务
							    addMonitorTask(MONITOR_TASK_STOP);

							    // 设置按钮状态
							    mBtnPageTwoStartGather.setText(cn.airburg.emo.R.string.pagetwo_gather_operat_button_start_text);
							    mLayoutPagerTwoStateText.setVisibility(View.INVISIBLE);
							    mBtnPageTwoStartGather.setBackgroundResource(cn.airburg.emo.R.drawable.botton_start);
							    mBtnPageTwoStartGather.setTextColor(0xFF606060);
						    }
						    @Override public void onCancelAction() { }
					    });

				    } else { // 未采集

					    // 显示对话框
//					    DialogUtils.showProgressDialog(MainActivity.this, "正在等待emo设备开始连续监测...") ;
					    // 设置开始时间
					    final Date startOrEndDatetime = new Date();

					    // 设置开始连续监测标志
//					    openMonitorSwitch();

					    // 执行连续监测 start 任务
					    addMonitorTask(MONITOR_TASK_START) ;

					    // 写入 连续监测开始时间
//					    writeMonitorStartDatetime(startOrEndDatetime);

					    mGraphView.reset();
					    mGraphView.setStartDatetime(startOrEndDatetime);
					    mGraphView.setEndDatetime(startOrEndDatetime);
					    mGraphView.add(mHazeValue);

					    // 开始采集数据
					    launchTimingGatherPM2dot5Thread();

					    // 设置按钮状态
					    mBtnPageTwoStartGather.setText(cn.airburg.emo.R.string.pagetwo_gather_operat_button_stop_text);
					    mLayoutPagerTwoStateText.setVisibility(View.VISIBLE);
					    mBtnPageTwoStartGather.setBackgroundResource(cn.airburg.emo.R.drawable.botton_stop);
					    mBtnPageTwoStartGather.setTextColor(0xFFF0F0F0);
				    }
			    }

		    }
	    });
    }

	/**
	 * 执行连续监测任务
	 * @param monitorTask task 任务
	 */
	private void addMonitorTask(int monitorTask) {
		this.mMonitorTaskList.add(monitorTask);

		if(this.mMonitorTaskList.size() == 1) {
			executeMonitorTask(monitorTask);
		}
	}

	/**
	 * 执行 连续监测 任务
	 * @param monitorTask 监测任务
	 */
	private void executeMonitorTask(int monitorTask) {
		if(MONITOR_TASK_START == monitorTask) {
            // 设置开始连续监测标志
			openMonitorSwitch();
		} else {
			// 关闭监控状态
			closeMonitorSwitch();
		}
	}

	/**
	 * 执行下一个监测任务
	 */
	private void executeNextMonitorTask() {
		if(mMonitorTaskList.size() > 0) {
			mMonitorTaskList.remove(0);
		}
		if(mMonitorTaskList.size() >= 1) {
			int taskId = mMonitorTaskList.get(0) ;
			executeMonitorTask(taskId);
		} else {
			// 开始 连续监测 后启动
			// 启动实时线程
			launchRealtimeThread();
		}
	}


	@Override
    protected void onResume() {
        super.onResume();
        this.isBackgrond = false;
        LogUtils.e("-->onResume");
        mWakeLock.acquire();

        // 支持低功耗蓝牙功能
        if (this.mIsSupportBle) {

			// 发送进入场景消息
			mStateProcessHandler.sendEmptyMessageDelayed(SelfHandler.ENTER_SCENE, 500);

        }

    }

	@Override
	protected void onPause() {
		super.onPause();
        this.isBackgrond = true;
		this.mIsOpenedBluetooth = null ;
	}

	@Override
    protected void onDestroy() {

        // 停止采集电池线程
        stopGatherBatteryThread();

        // 停止采集PM2.5数据线程
        stopGatherHazeValueThread();

	    // 解除接收蓝牙状态改变通知
	    if (null != this.mBluetoothStateChangeReceiver) {
		    unregisterReceiver(this.mBluetoothStateChangeReceiver);
	    }

		// 解除home键事件监听
		if(null != this.mHomeKeyEventReceiver) {
			unregisterReceiver(this.mHomeKeyEventReceiver);
		}

        // 解除屏幕关闭广播消息监听
        if(null != this.mScreenOnOrOffEventReceiver) {
            unregisterReceiver(this.mScreenOnOrOffEventReceiver);
        }

	    // 判断当前是否在监测
	    if (this.mContinuousMonitoringManager.isStateMonitor()) {
		    // 停止监测
		    stopTimingGatherPM2dot5Thread() ;
		    // 非自动停止
		    this.mGraphView.doStop(false);

	    }

		// 停止任务线程
		TaskManager.getInstance().stop() ;

        // 关闭连接服务
        if (null != this.mServiceConnection) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;

            this.mServiceConnection = null ;
        }

	    // 保存电池电量，这表示是从设备取得的数据
	    if (mBatteryPower > 0) {
		    DataUtils.saveBatteryPower(this, mBatteryPower);
	    }

        mWakeLock.release();

		super.onDestroy();
    }

	/**
     * 搜索蓝牙bleService
     * @param enable true:开始扫描BLE设备；false:停止扫描
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(boolean enable) {
        if (enable) {
            LogUtils.d(" -> start scan emo device ... ") ;
            this.mScanLeDeviceTimeoutRunnable = new ScanLeDeviceTimeoutRunnable() ;
            this.mCallbackHandler.postDelayed(this.mScanLeDeviceTimeoutRunnable, SCAN_PERIOD) ;

            // 设置扫描设备标志
            mScanning = true;

	        // 发送 开始搜索emo消息
	        if(!mConnectedGATT) {
		        mStateProcessHandler.sendEmptyMessageDelayed((SelfHandler.SCAN_EMO), 500) ;
	        }

            // mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.startLeScan(mLeScanCallback) ;

        } else {
            LogUtils.d(" -> stop scan emo device") ;
            // 移除 回调
            this.mCallbackHandler.removeCallbacks(this.mScanLeDeviceTimeoutRunnable);
            this.mScanLeDeviceTimeoutRunnable = null ;

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // 蓝牙已经开启成功
            LogUtils.d(" -> the bluetooth open ok. resultCode:" + resultCode) ;

            // 发送打开蓝牙消息
//	        mStateProcessHandler.sendEmptyMessage(SelfHandler.OPENED_BLUETOOTH) ;
        } else if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
	        LogUtils.d(" -> the bluetooth not open. resultCode:" + resultCode) ;
	        // 发送未打开蓝牙消息
	        mStateProcessHandler.sendEmptyMessage(SelfHandler.NOT_OPENED_BLUETOOTH) ;
        }else if (requestCode == REQUEST_CAREMA && resultCode == RESULT_OK){

            startActivityForResult(PhotoActivity.createIntent(getApplicationContext(), "", mHazeValue), REQUEST_Photo);
        }else if(requestCode == REQUEST_Photo){
            if (resultCode == RESULT_OK){
                startCarema();
            }else{
                //LogUtils.e("--> no...........");
            }

        }
    }

    private void startCarema(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File path = Utils.getPicturesPath();

        Uri imageUri = Uri.fromFile(new File(path,"image.jpg"));
        //指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CAREMA);
    }

	/**
	 * 分享屏幕截屏
	 */


    /**
     * 移除handler中的callbackRunnable
     * @param callbackRunnable 要进行移除的callbackRunnable
     */
    private void removeHandlerCallback(Runnable callbackRunnable) {
        if(null != callbackRunnable) {
            this.mCallbackHandler.removeCallbacks(callbackRunnable) ;
        }
    }

    /**
     * 移除 Handler 的所有 callback
     */
    private void removeHandlerAllCallback() {
        LogUtils.d(" -> ");

        // remove 扫描超时回调 Runnable
        if(null != this.mScanLeDeviceTimeoutRunnable) {
            this.mCallbackHandler.removeCallbacks(this.mScanLeDeviceTimeoutRunnable);
            this.mScanLeDeviceTimeoutRunnable = null ;
        }

        // 移除 连接 emo设备超时 Runnable
        if(null != mConnectEMODeviceTimeoutRunnable) {
            mCallbackHandler.removeCallbacks(this.mConnectEMODeviceTimeoutRunnable) ;
            this.mConnectEMODeviceTimeoutRunnable = null ;
        }

        // 移除 发现emo服务超时 Runnable
        if(null != mEMOServicesDiscoveredTimeoutRunnable) {
            mCallbackHandler.removeCallbacks(this.mEMOServicesDiscoveredTimeoutRunnable);
            this.mEMOServicesDiscoveredTimeoutRunnable = null ;
        }

        // 读取监控状态runnable
        mCallbackHandler.removeCallbacks(this.mReadMonitorSwitchResultRunnable) ;

        // 写入 打开监控状态 runnable
        mCallbackHandler.removeCallbacks(this.mWriteMonitorSwitchResultRunnable) ;

        // 写入 关闭监控状态 runnable
        mCallbackHandler.removeCallbacks(this.mCloseMonitorSwitchResultRunnable) ;

        // 读取 连续监控开始时间 runnable
        mCallbackHandler.removeCallbacks(this.mReadMonitorStartDatetimeResultRunnable) ;

        // 写入 监控开始时间 runnable
        mCallbackHandler.removeCallbacks(this.mWriteMonitorStartDatetimeResultRunnable) ;

        // 读取 监控数据notify runnable
        mCallbackHandler.removeCallbacks(this.mReadMonitorDataNotifyResultRunnable) ;

        // 写入 获取监控数据notify runnable
        mCallbackHandler.removeCallbacks(this.mWriteReadMonitorDataNotifyResultRunnable) ;

    }

	/**
	 * 开启连续监测状态
	 */
	private void openMonitorSwitch() {
		LogUtils.d(" -> ");

		// 停止实时监测线程
		stopRealtimeThread();

		ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_OPEN_MONITOR_SWITCH) ;

		TaskManager.getInstance().addTask(new TaskManager.Task() {
            @Override
            public void run() {
                SelfManager.getInstance().openMonitorSwitch(mBluetoothLeService);

                mCallbackHandler.postDelayed(mWriteMonitorSwitchResultRunnable, MONITOR_WRITE_DATA_TIME);
            }
        });
	}

	/**
	 * 关闭监测状态
	 */
	private void closeMonitorSwitch() {

		LogUtils.d(" -> ");

		// // 停止实时监测线程
		stopRealtimeThread();

		ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_CLOSE_MONITOR_SWITCH) ;

		// 设置停止标志
		TaskManager.getInstance().addTask(new TaskManager.Task() {
			@Override public void run() {
				SelfManager.getInstance().closeMonitorSwitch(mBluetoothLeService);

				// 监测连续监测是否关闭
				mCallbackHandler.postDelayed(mCloseMonitorSwitchResultRunnable, MONITOR_WRITE_DATA_TIME) ;
			}
		});
	}

	/**
	 * 读取监测状态开关
	 */
	private void readMonitorSwitch() {
		LogUtils.d(" -> ");
		// 设置读取状态
		ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_READ_MONITOR_SWITCH) ;
		// 读取历史记录开关
		TaskManager.getInstance().addTask(new TaskManager.Task() {
			@Override public void run() {
				// 读取连续监测开关
				SelfManager.getInstance().readMonitoringSwitch(mBluetoothLeService);

				mCallbackHandler.postDelayed(mReadMonitorSwitchResultRunnable, MONITOR_WRITE_DATA_TIME) ;
			}
		});
	}

	/**
	 * 读取监测开始时间
	 */
	private void readMonitorStartDatetime() {
		// 设置读取开始时间状态
		ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_READ_MONITOR_START_DATETIME) ;
		// 读取监测开始时间
		TaskManager.getInstance().addTask(new TaskManager.Task() {
			@Override
			public void run() {
				SelfManager.getInstance().readMonitorStartDatetime(mBluetoothLeService);

				mCallbackHandler.postDelayed(mReadMonitorStartDatetimeResultRunnable, MONITOR_WRITE_DATA_TIME) ;
			}
		});
	}

	/**
	 * 写入监测开始时间
	 * @param startDatetime 监测开始时间
	 */
	private void writeMonitorStartDatetime(final Date startDatetime) {
		ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_WRITE_MONITOR_START_DATETIME) ;

		// 设置开始连续监测时间
		TaskManager.getInstance().addTask(new TaskManager.Task() {
			@Override public void run() {
				SelfManager.getInstance().writeMonitorStartDatetime(mBluetoothLeService, startDatetime);

				mCallbackHandler.postDelayed(mWriteMonitorStartDatetimeResultRunnable, MONITOR_WRITE_DATA_TIME) ;
			}
		});
	}

	/**
	 * 读取连续监测数据
	 */
	private void readMonitorData() {
		// 停止实时监测线程
		stopRealtimeThread();

		// 设置读取监测数据标志
		ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_READ_MONITOR_DATA) ;
		// 读取监测数据
		TaskManager.getInstance().addTask(new TaskManager.Task() {
			@Override
			public void run() {
				// 读取监测数据
				SelfManager.getInstance().readMonitorData(mBluetoothLeService);

				mCallbackHandler.postDelayed(mWriteReadMonitorDataNotifyResultRunnable, MONITOR_WRITE_DATA_TIME);
			}
		});
	}



	/**
	 * 读取一次电量信息
	 */
	private void readOnceBatteryPower() {
		LogUtils.d(" -> ");

        if (mBatteryPower > 0 && mBatteryPower <= 10){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLinearLayoutPageOneBatteryText.setVisibility(View.VISIBLE);
                    mTvPagerOneBattery.setText(getString(cn.airburg.emo.R.string.remain_battery_lessthan_10));
                    mCallbackHandler.removeCallbacks(mHideBatteryTipRunnable);
                    mCallbackHandler.postDelayed(mHideBatteryTipRunnable, 2000);

                }
            });

        }
		// 提交任务
		TaskManager.getInstance().addTask(new TaskManager.Task() {
			@Override
			public void run() {

				if(null != mBluetoothLeService) {
					// 读取电池电量
					BluetoothGattCharacteristic gattBatteryCharacteristic = mBatteryGattService.getCharacteristic(UUID.fromString(MainActivity.this.getString(cn.airburg.emo.R.string.battery_level_uuid)));
					//                    mBluetoothLeService.setCharacteristicNotification(gattBatteryCharacteristic, true);
                    if (null != gattBatteryCharacteristic && null != mBluetoothLeService) {
                        mBluetoothLeService.readCharacteristic(gattBatteryCharacteristic);
                    }

				}
			}
		});
	}

	/**
	 * 读取一次pm2.5值
	 */
	private void readOnceHazeValue() {
		LogUtils.d(" -> ");
		TaskManager.getInstance().addTask(new TaskManager.Task() {
            @Override
            public void run() {
                BluetoothGattCharacteristic gattCharacteristic = SelfManager.getInstance().getBluetoothGattCharacteristic(SelfManager.BLE_CHARACTERISTIC_FFF2);
                // （1）	特征值对应0xfff2，读取蓝牙模块采集到当前PM2.5数据（一开始获取PM2.5采集到数据是不准确的，大约过了15s采集到的数值是准确的），使用18个字节
                if (null != gattCharacteristic && null != mBluetoothLeService) {
                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
                }
            }
        });
	}

    /**
     * 读取设备类型 读取0xfff7数据
     */
    private void readDeviceType() {
        LogUtils.d(" -> ");
        // 正在确定设备类型
	    mTvPageOneHazeValueForeDesc.setText(cn.airburg.emo.R.string.pageone_get_device_type_from_emo);

        BluetoothGattCharacteristic gattCharacteristic = SelfManager.getInstance().getBluetoothGattCharacteristic(SelfManager.BLE_CHARACTERISTIC_FFF7) ;

        // （1）	特征值对应0xfff2，读取蓝牙模块采集到当前PM2.5数据（一开始获取PM2.5采集到数据是不准确的，大约过了15s采集到的数值是准确的），使用18个字节
        if(null != gattCharacteristic && null != mBluetoothLeService) {

            mBluetoothLeService.readCharacteristic(gattCharacteristic);
        }

        // 添加读取设备类型超时检测
        this.mReadDeviceTypeTimeoutRunnable = new ReadDeviceTypeTimeoutRunnable() ;
        this.mCallbackHandler.postDelayed(this.mReadDeviceTypeTimeoutRunnable, ReadDeviceTypeTimeoutRunnable.TIMEOUT_PERIOD) ;
    }


	/**
	 * 启动定时采集PM2.5数据线程
	 */
	private void launchTimingGatherPM2dot5Thread() {
		mContinuousMonitoringManager.setStateMonitor(true) ; // 这句话不能注释掉，切记，有标识连续监测按钮状态的作用
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(mContinuousMonitoringManager.isStateMonitor()) {

					try {
//						Thread.sleep(10*60*1000);
						Thread.sleep(GraphView.MINUTE_GATHER_INTERVAL * 60*1000);  // 1分钟
					} catch (InterruptedException e) {
						LogUtils.e(e.getMessage());
						e.printStackTrace();
					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							mGraphView.setEndDatetime(new Date());
//							mGraphView.add(mHazeValue);
							if(mContinuousMonitoringManager.isStateMonitor()) {
								// 读取连续监测数据
								readMonitorData();
							}
						}
					});
				}
			}
		}).start();
	}

	/**
	 * 停止采集PM2.5数据线程
	 */
	private void stopTimingGatherPM2dot5Thread() {
		this.mContinuousMonitoringManager.setStateMonitor(false) ;
	}

    /**
     * 启动获取PM2.5数据的线程
     */
    private void launchGatherHazeValueThread() {
        this.mGatherHazeValueState = true ;
        new Thread(new Runnable() {
            @Override public void run() {
                while(mGatherHazeValueState) {

	                // 增加读取pm2.5实时数据 任务
	                readOnceHazeValue() ;

                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        LogUtils.e(e.getMessage());
                        e.printStackTrace();
                    }
                }

				// 发送 pm2.5采集线程停止消息
	            mStateProcessHandler.sendEmptyMessage(SelfHandler.THREAD_PM2DOT5_STOPPED) ;
            }
        }).start();
    }

    /**
     * 停止采集PM2.5数据线程
     */
    private void stopGatherHazeValueThread() {
        this.mGatherHazeValueState = false ;
    }

    // 启动获取电池电量的线程
    private void launchGatherBatteryThread() {
        this.mGatherBatteryPowerState = true ;
        new Thread(new Runnable(){

            @Override public void run() {
                while(mGatherBatteryPowerState) {
	                // 读取一次电量
	                readOnceBatteryPower() ;

                    try {
                        Thread.sleep(BATTERY_POWER_GATHER_INTERVAL);
                    } catch (InterruptedException e) {
                        LogUtils.e(e.getMessage());
                        e.printStackTrace();
                    }
                }

	            // 发送 采集电池电量线程停止 消息
	            mStateProcessHandler.sendEmptyMessage(SelfHandler.THREAD_BATTERY_POWER_STOPPED) ;
            }
        }).start();
    }

    /**
     * 停止采集电池数据线程
     */
    private void stopGatherBatteryThread() {
        this.mGatherBatteryPowerState = false ;
    }

    /**
     * 启动实时监测线程
     */
    private void launchRealtimeThread() {
        // 启动实时获取电量线程
        if(!this.mGatherBatteryPowerState) {
            launchGatherBatteryThread();
        }

        // 启动实时获取pm2.5数据线程
        if(!this.mGatherHazeValueState) {
            launchGatherHazeValueThread();
        }
    }


    /**
     * 停止实时监测线程
     */
    private void stopRealtimeThread() {
        // 停止电量和实时获取pm2.5线程
        if(this.mGatherBatteryPowerState) {
            stopGatherBatteryThread();
        }

        if(this.mGatherHazeValueState) {
            stopGatherHazeValueThread();
        }
    }

    /**
     * 停止所有采集线程
     */
    private void stopAllGatherThread() {
        stopRealtimeThread() ;

        stopTimingGatherPM2dot5Thread();
    }

	/**
	 * 进入场景
	 */
	private void doEnterScene() {
		LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());

        // 如果 emo 处于初始化状态
        if(EMOStatus.STATUS_INIT == this.mEMOStatus) {
            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            if (!mBluetoothAdapter.isEnabled()) {
                LogUtils.d(" -> bluetoothAdapter is not enabled. ");
                if (null == mIsOpenedBluetooth) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {
                LogUtils.d(" -> bluetoothAdapter is enabled. ");
                // 发送打开蓝牙消息
                mStateProcessHandler.sendEmptyMessage(SelfHandler.OPENED_BLUETOOTH);
            }
        }
	}

	/**
	 * 打开了蓝牙
	 */
	private void doOpenedBluetooth() {
		LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());
		this.mIsOpenedBluetooth = true ;

        // 如果 emo 处于初始化状态
        if(EMOStatus.STATUS_INIT == this.mEMOStatus) {
            // 搜索emo设备状态
            this.mEMOStatus = EMOStatus.STATUS_SCAN_EMO_DEVICE;
            LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

            if(!LocationUtils.isAllow(getApplicationContext()) && !LocationUtils.isShowing){
                LocationUtils.showLocationDialog(this, null);
            }

            // 扫描设备
            scanLeDevice(true);
        }
	}

	/**
	 * 没有打开蓝牙
	 */
	private void doNotOpenedBluetooth() {
		LogUtils.d(" -> do not open bluetooth.");
		this.mIsOpenedBluetooth = false ;
		mTvPageOneHazeValueForeDesc.setText(cn.airburg.emo.R.string.pageone_do_not_open_bluetooth);
	}

	/**
	 * 初始化状态
	 */
	private void doInitState() {
		LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());

        this.mEMOStatus = EMOStatus.STATUS_INIT ;
        LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());


        // 设置ui显示
		// page one
        // pm2.5 显示value
        this.mTvPageOneHazeValue.setText("--");
        this.mTvPageOneHazeValueForeDesc.setText(R.string.pageone_haze_value_init) ;
        // 背景显示文字
        this.mTvPageOneHazeValueBackDesc.setText(R.string.pageone_haze_value_init);
        this.mTvPageOneHazeValueBackDesc.setVisibility(View.INVISIBLE);

		// 隐藏电池
		mLinearLayoutPageOneBattery.setVisibility(View.INVISIBLE);
		// 隐藏电池电量警告
		mTvPagerOneBatteryWarn.setVisibility(View.INVISIBLE);

		// page two
        // 隐藏 监测中 字样
        mLayoutPagerTwoStateText.setVisibility(View.INVISIBLE);

		// 设置检测按钮为 开始检测 按钮状态
        mBtnPageTwoStartGather.setBackgroundResource(cn.airburg.emo.R.drawable.botton_start);
        mBtnPageTwoStartGather.setText(cn.airburg.emo.R.string.pagetwo_gather_operat_button_start_text);
        mBtnPageTwoStartGather.setTextColor(0xFF606060);

        mBtnPageTwoStartGather.setAlpha(0.2f);
        mBtnPageTwoStartGather.setClickable(false);
	}

	/**
	 * 搜索emo设备
	 */
	private void doScanEMODevice() {
		LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());

		mTvPageOneHazeValueForeDesc.setText(cn.airburg.emo.R.string.pageone_scan_emo);
		this.mTvPageOneHazeValue.setText("--");    // 设置雾霾显示数据为 --
	}

	/**
	 * 没有发现emo设备处理逻辑
	 */
	private void doNotFoundEMODevice() {
		LogUtils.d(" -> ");
		// page one 处理
		// 显示文本 emo不在附件
		mTvPageOneHazeValueForeDesc.setText(cn.airburg.emo.R.string.pageone_emo_not_nearby_warn);

		// 获取上次电量
		/*int batteryPower = DataUtils.getBatteryPower(MainActivity.this);
		if( batteryPower > 0 && batteryPower < 15) {  // 上次的电量保存了下来
			mTvPagerOneBatteryWarn.setVisibility(View.VISIBLE);
			mTvPagerOneBatteryWarn.setText(cn.airburg.emo.R.string.pageone_battery_warn);
		}*/

		// 隐藏电池
		mLinearLayoutPageOneBattery.setVisibility(View.INVISIBLE);

		// page two 处理
		// 开始检测按钮
		mBtnPageTwoStartGather.setClickable(false);
		mBtnPageTwoStartGather.setAlpha(0.2f);

		if (null == this.mRetryScanLEDeviceRunnable) {
			this.mRetryScanLEDeviceRunnable = new RetryScanLEDeviceRunnable() ;
		}

		// 如果关闭了屏幕，那么不进行重新扫描
		PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		// 屏幕是亮着的，那么发送重新连接设备的消息
		if(powerManager.isScreenOn()) { // 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
			// 800 毫秒后重复扫描
			mCallbackHandler.postDelayed(this.mRetryScanLEDeviceRunnable, 800);
		}
	}

	/**
	 * 发现了EMO设备
	 */
	private void doFoundEMODevice() {
		LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());
		mTvPageOneHazeValueForeDesc.setText(cn.airburg.emo.R.string.pageone_connect_emo);

		// 显示电池
		mLinearLayoutPageOneBattery.setVisibility(View.VISIBLE);

        // 连接 emo 设备
        this.mEMOStatus = EMOStatus.STATUS_CONNECT_EMO_DEVICE ;
        LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

        // 设置 连接保护，如果连接
        this.mConnectEMODeviceTimeoutRunnable = new ConnectEMODeviceTimeoutRunnable() ;
        mCallbackHandler.postDelayed(this.mConnectEMODeviceTimeoutRunnable, ConnectEMODeviceTimeoutRunnable.TIMEOUT_PERIOD);

		// 启动emo连接和通信服务
		mServiceConnection = new SelfServiceConnection();

		// 启动服务
		LogUtils.d(" -> start bluetoothLeService ... ");
		Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
		MainActivity.this.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

	}

    /**
     * 成功连接了emo设备
     */
    private void doConnectEMODeviceInitiatedSuccess() {
        LogUtils.d(" -> mEMOStatus: " + mEMOStatus.getName());
        mEMOStatus = EMOStatus.STATUS_CONNECT_EMO_DEVICE_INITIATED_SUCCESS ;
        LogUtils.d(" -> set mEMOStatus: " + mEMOStatus.getName());
    }

    /**
     * 连接emo设备失败
     */
    private void doConnectEMODeviceInitiatedFailure() {
        LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());

        // 只有 当前状态为正在连接emo设备，那么才进行相应的操作
        if (EMOStatus.STATUS_CONNECT_EMO_DEVICE == this.mEMOStatus) {

            // 断开链接状态
            this.mEMOStatus = EMOStatus.STATUS_DISCONNECTED_EMO_DEVICE;
            LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

            mConnectedGATT = false;

            // 显示Toast 提示， 连接初始化失败
            Toast.makeText(this, R.string.ble_connect_emo_device_initiated_failure, Toast.LENGTH_SHORT).show();

//        // 停止所有采集线程
//        stopAllGatherThread() ;
//
//        // 停止任务线程
//        TaskManager.getInstance().stop() ;

            // 移除所有
            removeHandlerAllCallback();

            mBluetoothDevice = null;

            // 关闭连接服务
            if (null != this.mBluetoothLeService) {
                unbindService(mServiceConnection);
                mBluetoothLeService = null;

                this.mServiceConnection = null ;
            }

            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

            // 屏幕是亮着的，那么发送重新连接设备的消息
            if (powerManager.isScreenOn()) { // 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
                LogUtils.d(" -> send reconnected emo message, the screen is on");
                // 发送 重新连接 设备 消息
                mStateProcessHandler.sendEmptyMessageDelayed(SelfHandler.RECONNECTED_EMO, 100);
            }
        }
    }

    /**
     * 连接emo设备超时
     */
    private void doConnectedEMODeviceTimeout() {
        LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());

        // 只有 当前状态为正在连接emo设备和连接初始化成功，那么才进行相应的操作
        if (EMOStatus.STATUS_CONNECT_EMO_DEVICE == this.mEMOStatus
                || EMOStatus.STATUS_CONNECT_EMO_DEVICE_INITIATED_SUCCESS == this.mEMOStatus) {

            // 断开链接状态
            this.mEMOStatus = EMOStatus.STATUS_CONNECT_EMO_DEVICE_TIMEOUT ;
            LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

            mConnectedGATT = false;

            // 显示Toast 提示， 连接超时
            if (!isBackgrond){
                Toast.makeText(this, R.string.ble_connect_emo_device_timeout, Toast.LENGTH_SHORT).show();
            }


//        // 停止所有采集线程
//        stopAllGatherThread() ;
//
//        // 停止任务线程
//        TaskManager.getInstance().stop() ;

            // 移除所有
            removeHandlerAllCallback();

            mBluetoothDevice = null;

            // 关闭连接服务
            if (null != this.mBluetoothLeService) {
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                this.mServiceConnection = null ;
            }

            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

            // 屏幕是亮着的，那么发送重新连接设备的消息
            if (powerManager.isScreenOn()) { // 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
                LogUtils.d(" -> send reconnected emo message, the screen is on");
                // 发送 重新连接 设备 消息
                mStateProcessHandler.sendEmptyMessageDelayed(SelfHandler.RECONNECTED_EMO, 100);
            }
        }
    }


	/**
	 * 和emo设备建立了连接
	 */
	private void doConnectedEMODevice() {
		LogUtils.d(" ->  mEMOStatus: " + this.mEMOStatus.getName());

        // 移除连接超时保护
        mCallbackHandler.removeCallbacks(this.mConnectEMODeviceTimeoutRunnable);
        this.mConnectEMODeviceTimeoutRunnable = null ;

        // 设置已经连接的标志
        mConnectedGATT = true;

        // 设置状态
        this.mEMOStatus = EMOStatus.STATUS_CONNECTED_EMO_DEVICE ;
        LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

        // 启动 任务管理器
        if(!TaskManager.getInstance().isRunning()) {
            TaskManager.getInstance().start() ;
        }

        // 设置发现emo服务保护runnable
        this.mEMOServicesDiscoveredTimeoutRunnable = new EMOServicesDiscoveredTimeoutRunnable() ;
        mCallbackHandler.postDelayed(this.mEMOServicesDiscoveredTimeoutRunnable, EMOServicesDiscoveredTimeoutRunnable.TIMEOUT_PERIOD);

		// 开始检测按钮
//		mBtnPageTwoStartGather.setClickable(true);
//		mBtnPageTwoStartGather.setAlpha(1f);
	}

	/**
	 * 和emo设备断开了连接
	 */
	private void doDisconnectedEMODevice() {
		LogUtils.d(" -> mEMOStatus: " + this.mEMOStatus.getName());

        isWarmUp = true;
        if (timer != null){
            timer.cancel();
            timer = null;
        }
        if (task != null){
            task.cancel();
            task = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageButton ivShare = (ImageButton) mPageOne.findViewById(R.id.btnPagerOneShare);
                ivShare.setVisibility(View.INVISIBLE);

                ImageButton ivCamera = (ImageButton) mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneCamera);
                ivCamera.setVisibility(View.INVISIBLE);

                ImageButton ivShareDisabled = (ImageButton) mPageOne.findViewById(R.id.btnPagerOneShare_disabled);
                ivShareDisabled.setVisibility(View.VISIBLE);

                ImageButton ivCameraDisabled = (ImageButton) mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneCamera_disabled);
                ivCameraDisabled.setVisibility(View.VISIBLE);

                ImageButton ivShareTwo = (ImageButton) mPageTwo.findViewById(R.id.btnPagerTwoShare);
                ivShareTwo.setVisibility(View.INVISIBLE);
                ImageButton ivShareDisabledTwo = (ImageButton) mPageTwo.findViewById(R.id.btnPagerTwoShare_disabled);
                ivShareDisabledTwo.setVisibility(View.VISIBLE);
            }
        });

        //createTimerTask();
        mConnectedGATT = false;

        // 设置断开链接状态
        this.mEMOStatus = EMOStatus.STATUS_DISCONNECTED_EMO_DEVICE ;
        LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName()) ;

        // 显示Toast 提示
        Toast.makeText(this, R.string.ble_disconnected_emo_device, Toast.LENGTH_LONG).show();

        // 停止所有采集线程
        stopAllGatherThread() ;

        // 停止任务线程
        TaskManager.getInstance().stop() ;

        // 移除所有
        removeHandlerAllCallback();

        mBluetoothDevice = null ;

        // 关闭连接服务
        if (null != this.mBluetoothLeService) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
            this.mServiceConnection = null ;
        }

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        // 屏幕是亮着的，那么发送重新连接设备的消息
        if(powerManager.isScreenOn()) { // 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
            LogUtils.d(" -> send reconnected emo message, the screen is on");
            // 发送 重新连接 设备 消息
            mStateProcessHandler.sendEmptyMessageDelayed(SelfHandler.RECONNECTED_EMO, 100);
        }
	}

    /**
     * 重新连接 emo device
     */
    private void doReconnectEMODevice() {
        LogUtils.d(" -> ");

        doInitState() ;

        if (!mBluetoothAdapter.isEnabled()) {
            LogUtils.d(" -> bluetoothAdapter is not enabled. ");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            LogUtils.d(" -> bluetoothAdapter is enabled. ");
            // 发送打开蓝牙消息
            mStateProcessHandler.sendEmptyMessage(SelfHandler.OPENED_BLUETOOTH) ;
        }
    }


    /**
     * 点亮屏幕
     */
    private void doScreenOn() {
        LogUtils.d(" -> ");
//	    this.mTvPageOneHazeValue.setText("--");
        // TODO 待实现，设置当前屏幕状态
        // 发送重新连接消息
//        mStateProcessHandler.sendEmptyMessage(SelfHandler.RECONNECTED_EMO);
        doInitState();
    }

    /**
     * 关闭屏幕（也就是点击了电源键）
     */
    private void doScreenOff() {
        LogUtils.d(" -> ");
        // TODO 关闭屏幕处理代码，待实现，测试重点：当调用了mBluetoothLeService.close(); 这个后会触发断开链接的回调吗？
        // 断开连接蓝牙服务，
        if(null != this.mBluetoothLeService) {
            this.mBluetoothLeService.disconnect();
        }
    }

    /**
     * 连接emo后，发现emo服务超时
     */
    private void doEMOServicesDiscoveredTimeout() {
        LogUtils.d(" -> discovered emo services timeout. mEMOStatus: " + this.mEMOStatus.getName()) ;

        // 当前处于连接状态，
        if(EMOStatus.STATUS_CONNECTED_EMO_DEVICE == this.mEMOStatus) {

            // 设置发现服务超时
            this.mEMOStatus = EMOStatus.STATUS_EMO_SERVICES_DISCOVERED_TIMEOUT;
            LogUtils.d(" -> set mEMOStatus: " + this.mEMOStatus.getName());

            mConnectedGATT = false;

            mBluetoothDevice = null;

            // 显示Toast 提示， 发现服务超时
            Toast.makeText(this, R.string.ble_emo_services_discovered_timeout, Toast.LENGTH_SHORT).show();

//        // 停止所有采集线程
//        stopAllGatherThread() ;
//
            // 停止任务线程
            TaskManager.getInstance().stop() ;

            // 移除所有
            removeHandlerAllCallback();

            // 关闭连接服务
            if (null != this.mBluetoothLeService) {
                unbindService(mServiceConnection);
                this.mBluetoothLeService = null;
                this.mServiceConnection = null ;
            }

            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

            // 屏幕是亮着的，那么发送重新连接设备的消息
            if (powerManager.isScreenOn()) { // 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
                LogUtils.d(" -> send reconnected emo message, the screen is on");
                // 发送 重新连接 设备 消息
                mStateProcessHandler.sendEmptyMessageDelayed(SelfHandler.RECONNECTED_EMO, 100);
            }
        }

    }

	/**
	 * 连接emo后，会发现EMO提供的所有服务，在CONNECTED_EMO后执行
	 */
	private void doEMOServicesDiscovered() {
		LogUtils.d(" -> discovered emo services. mEMOStatus: " + this.mEMOStatus.getName());

        // 移除发现emo服务超时保护
        mCallbackHandler.removeCallbacks(this.mEMOServicesDiscoveredTimeoutRunnable);
        this.mEMOServicesDiscoveredTimeoutRunnable = null ;

//		mTvPageOneHazeValue.setText("0");
//		// 正在获取监测数据
//		mTvPagerOnePM2dot5Desc.setText(cn.ablecloud.emo.R.string.pageone_get_data_from_emo);

        // 当前处于连接状态，那么读取数据
        if(EMOStatus.STATUS_CONNECTED_EMO_DEVICE == this.mEMOStatus) {
            // 读取设备类型
            readDeviceType() ;
        }


//		// 读取一次pm2.5数据
//		readOnceHazeValue();
//
//		// 读取一次电量信息
//		readOnceBatteryPower();
//
//		// 读取连续监测
//		readMonitorSwitch() ;

	}

	/**
	 * 第一次进入第二页（连续监测页面）
	 */
	private void doFirstEnterContinuousMonitoringView() {
		LogUtils.d("-> enter");

//		if(!mConnectedGATT) {   // 没有连接，那么直接返回
//			return ;
//		}
//
//		// 读取监测状态开关
//		readMonitorSwitch();
	}

	/**
	 * 进入第一页
	 */
	private void doEnterPageView0() {
		LogUtils.d(" -> ");
//		if(mConnectedGATT) {
//			// 停止连续监测线程
//			if(this.mContinuousMonitoringManager.isStateMonitor()) {
//				stopTimingGatherPM2dot5Thread();
//			}
//
//			// 开启 实时监测线程
//			launchGatherHazeValueThread();
//
//			// 开启获取电池电量线程
//			launchGatherBatteryThread();
//		}
	}

	/**
	 * 进入第二页
	 */
	private void doEnterPageView1() {
		LogUtils.d(" -> ");
//		if(mConnectedGATT) {
//			// 关闭 实时监测线程
//			stopGatherHazeValueThread();
//
//			// 关闭获取电量线程
//			stopGatherBatteryThread();
//
//
//			// 设置不可点击
//			this.mBtnPageTwoStartGather.setClickable(false);
//			this.mBtnPageTwoStartGather.setAlpha(0.2f);
//
//			// 读取连续监测
//			readMonitorSwitch() ;
//		}
	}

	/**
	 * pm2.5采集线程停止
	 */
	private void doHazeValueThreadStopped() {
		LogUtils.d(" -> the thread of haze value gather stopped.");
	}

	/**
	 * 电池电量采集线程停止
	 */
	private void doBatteryPowerThreadStopped() {
		LogUtils.d(" -> the thread of battery power gather stopped.");

	}

    /**
     * 读取设备类型超时
     */
    private void doReadDeviceTypeTimeout() {
        LogUtils.d(" -> ") ;

        // 重新读取
        readDeviceType() ;
    }

    /**
     * 读取设备类型完毕
     * @param deviceType 设备类型
     */
    private void doReadDeviceTypeFinished(int deviceType) {
        LogUtils.d(" -> deviceType:" + deviceType);
        // 需要判断BLE_CHARACTERISTIC_FFF7也就是deviceType类型，如果是负数，重新连接
        isConnectionSuccessful = true;
        // 移除标志
        removeHandlerCallback(this.mReadDeviceTypeTimeoutRunnable);
        this.mReadDeviceTypeTimeoutRunnable = null ;

        // 根据设备类型确定雾霾数据解析器
        if(0 == deviceType) {
            mHazeParser = new DeviceTypeZeroParser() ;
        } else {
            mHazeParser = new DeviceTypeOneParser() ;
        }

        // 雾霾实时监测值显示为0
        mTvPageOneHazeValue.setText("0");
        // 正在获取实时监测数据
	    mTvPageOneHazeValueForeDesc.setText(cn.airburg.emo.R.string.pageone_get_data_from_emo);

        // 读取一次pm2.5数据
        readOnceHazeValue();

        // 读取一次电量信息
        readOnceBatteryPower();

        // 读取连续监测
        readMonitorSwitch() ;
        createTimerTask();
    }

    private void createTimerTask(){
        task = null;
        timer = null;
        if (task == null){
            task = new TimerTask(){
                public void run() {
                    isWarmUp = false;
                    mTvPageOneHazeValueForeDesc.setOnClickListener(null);

                    if (timer != null){
                        timer.cancel();
                        timer = null;
                    }
                    if (task != null){
                        task.cancel();
                        task = null;
                    }



                }
            };
        }
        if (timer == null){
            timer = new Timer(true);
            timer.schedule(task, 20000);
        }
    }

	/**
	 * 读取监测开关完毕
	 */
	private void doReadMonitorSwitchFinished(int openSwitch) {
		LogUtils.d(" -> ");

		// 处理监控
		if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_READ_MONITOR_SWITCH)) {
			mCallbackHandler.removeCallbacks(this.mReadMonitorSwitchResultRunnable) ; // 移除callback
			ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_READ_MONITOR_SWITCH) ; // 移除标志
		}

//		mBtnPageTwoStartGather.setClickable(true);

		if(1 == openSwitch) {
//			mBtnPageTwoStartGather.setBackgroundResource(cn.ablecloud.emo.R.drawable.botton_stop);
//			mBtnPageTwoStartGather.setText(cn.ablecloud.emo.R.string.pagetwo_gather_operat_button_stop_text);
////			mBtnPageTwoStartGather.setAlpha(1f);
//			mBtnPageTwoStartGather.setTextColor(0xFFF0F0F0);

			// 显示 监测中 字样
//			mLayoutPagerTwoStateText.setVisibility(View.VISIBLE);

			// 设置监测状态为 正在监测
			mContinuousMonitoringManager.setStateMonitor(true);

			// 启动连续监测线程
			launchTimingGatherPM2dot5Thread();

		} else {
			// 设置监测状态 未监测状态
//			mContinuousMonitoringManager.setStateMonitor(false);
//
//			mBtnPageTwoStartGather.setAlpha(1f);
//			mBtnPageTwoStartGather.setClickable(true);
		}

		// 读取监测开始时间
		readMonitorStartDatetime();

	}

	/**
	 * 读取监测开始时间完毕
	 */
	private void doReadMonitorStartDatetimeFinished(Date datetime) {
		LogUtils.d(" -> ");

		// 处理监控
		if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_READ_MONITOR_START_DATETIME)) {
			mCallbackHandler.removeCallbacks(this.mReadMonitorStartDatetimeResultRunnable) ; // 移除callback
			ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_READ_MONITOR_START_DATETIME) ; // 移除标志
		}

		if (null != datetime) {
			// 设置开始时间
			mGraphView.setStartDatetime(datetime);

			// 读取连续监测数据
			readMonitorData();
		} else {
			// 设置开始按钮可以点击
			mBtnPageTwoStartGather.setAlpha(1f);
			mBtnPageTwoStartGather.setClickable(true);

			// 启动实时监测线程
			launchRealtimeThread();
		}
	}

	/**
	 * 读取监测片段数据完毕
	 * @param monitorData 监测数据
	 */
	private void doReadMonitorFragmentDataFinished(byte[] monitorData) {
		LogUtils.d(" -> ");
		mContinuousMonitoringManager.addMonitorDataArray(monitorData);
	}

	/**
	 * 读取监测数据完毕
	 */
	private void doReadMonitorDataFinished() {
		LogUtils.d(" -> ");

		if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_READ_MONITOR_NOTIFICATION_SWITCH)) {
			mCallbackHandler.removeCallbacks(this.mReadMonitorDataNotifyResultRunnable);
			ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_READ_MONITOR_NOTIFICATION_SWITCH) ;
		}

		byte[] monitorDataArray = this.mContinuousMonitoringManager.getMonitorDataArray() ;

		List<Integer> dataArray = new ArrayList<Integer>(monitorDataArray.length/2) ;

		byte[] monitorData = new byte[2] ;
		byte temp ;
		for(int i=0; i<monitorDataArray.length; i+=2) {
			System.arraycopy(monitorDataArray, i, monitorData, 0, 2);
			temp = monitorData[0] ;
			monitorData[0] = monitorData[1] ;
			monitorData[1] = temp ;

			// 雾霾矫正处理
			dataArray.add((int) (ByteUtils.getShort(monitorData, 0) * HazeParser.HAZE_CORRECT_FACTOR));
		}

		if(dataArray.size() > 0) {
			mGraphView.setEndDatetime(new Date(mGraphView.getStartDatetime().getTime() + (dataArray.size()-1) * GraphView.MINUTE_GATHER_INTERVAL * 60 * 1000));
			mGraphView.add(dataArray);
		}


		// 如果处于监控状态
		if(mContinuousMonitoringManager.isStateMonitor()) {
			mBtnPageTwoStartGather.setBackgroundResource(cn.airburg.emo.R.drawable.botton_stop);
			mBtnPageTwoStartGather.setText(cn.airburg.emo.R.string.pagetwo_gather_operat_button_stop_text);
			mBtnPageTwoStartGather.setTextColor(0xFFF0F0F0);

			// 显示 监测中 字样
			mLayoutPagerTwoStateText.setVisibility(View.VISIBLE);
		}

		// 设置开始监测按钮可以点击
		mBtnPageTwoStartGather.setClickable(true);
		mBtnPageTwoStartGather.setAlpha(1f);

		// 启动实时获取电量线程
		launchRealtimeThread();

	}



    /**
     * 根据pm2.5的值获取pager_one背景颜色
     * @param pm2dot5 pm2.5值
     * @return 背景颜色id值
     */
    private int getHazeValueBgColorRes(int pm2dot5) {
        int pm2dot5Color = cn.airburg.emo.R.color.blue ;
        if(pm2dot5 < 36) {
            pm2dot5Color = cn.airburg.emo.R.color.blue ;
        } else if(pm2dot5 < 76) {
            pm2dot5Color = cn.airburg.emo.R.color.green ;
        } else if(pm2dot5 < 116) {
            pm2dot5Color = cn.airburg.emo.R.color.yellow ;
        } else if(pm2dot5 < 151) {
            pm2dot5Color = cn.airburg.emo.R.color.orange ;
        } else if(pm2dot5 < 251) {
            pm2dot5Color = cn.airburg.emo.R.color.red ;
        } else if(pm2dot5 < 351) {
            pm2dot5Color = cn.airburg.emo.R.color.purple ;
        } else {
            pm2dot5Color = cn.airburg.emo.R.color.black ;
        }

        return pm2dot5Color ;
    }

    private View.OnClickListener tvDescButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isWarmUp && task != null && timer != null){
                Toast.makeText(MainActivity.this, R.string.device_warm_up, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 开始动画
     */
    private void startAnimation() {


	    int targetColorRes = getHazeValueBgColorRes(mHazeValue) ;

        // 颜色值没有变化
        if(mCurrentColorRes == targetColorRes) {
	        if(!Utils.getHazeValueDesc(mHazeValue).equals(mTvPageOneHazeValueForeDesc.getText())) {
		        mTvPageOneHazeValueForeDesc.setText(Utils.getHazeValueDesc(mHazeValue));
	        }
            return ;
        }

        String hazeValueDesc = Utils.getHazeValueDesc(mHazeValue) ;
        /*if (hazeValueDesc.equals(getString(R.string.device_warm_up_title))){
            return;
        }*/

	    // 背景颜色变换动画
	    Integer colorFrom = getResources().getColor(mCurrentColorRes);
	    Integer colorTo = getResources().getColor(targetColorRes);

	    mCurrentColorRes = targetColorRes ;

	    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
	    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
		    @Override
		    public void onAnimationUpdate(ValueAnimator animator) {
			    mLayoutAnimPageOneForeground.setBackgroundColor((Integer)animator.getAnimatedValue());
		    }
	    });
	    colorAnimation.setRepeatCount(0);
	    colorAnimation.setDuration(PAGEONE_ANIMATION_TIME) ;
	    colorAnimation.start() ;

		// 空气质量描述语变换动画
	    // 空气质量描述语


	    // 淡出 动画
	    AlphaAnimation fadeOutAnim = new AlphaAnimation(0.6f, 0.0f) ;
	    fadeOutAnim.setDuration(PAGEONE_ANIMATION_TIME);
	    fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
		    @Override public void onAnimationStart(Animation animation) { }
		    @Override public void onAnimationRepeat(Animation animation) { }

		    @Override
		    public void onAnimationEnd(Animation animation) {
			    mTvPageOneHazeValueBackDesc.setVisibility(View.INVISIBLE);

			    mTvPageOneHazeValueForeDesc.setText(mTvPageOneHazeValueBackDesc.getText());
			    mTvPageOneHazeValueForeDesc.setAlpha(0.6f);
		    }
	    });

	    // 淡入动画
	    AlphaAnimation fadeInAnim = new AlphaAnimation(0.0f, 0.6f) ;
	    fadeInAnim.setDuration(PAGEONE_ANIMATION_TIME);

	    // 后 显示出来
	    mTvPageOneHazeValueBackDesc.setAlpha(0.0f);
	    mTvPageOneHazeValueBackDesc.setVisibility(View.VISIBLE);
	    mTvPageOneHazeValueBackDesc.setText(hazeValueDesc);

	    mTvPageOneHazeValueForeDesc.startAnimation(fadeOutAnim);
	    mTvPageOneHazeValueBackDesc.startAnimation(fadeInAnim);
//	    mTvPageOneHazeValueForeDesc.


    }

    /**
     *  Device scan callback.
     */
    private final class SelfLeScanCallback implements BluetoothAdapter.LeScanCallback {

        @Override public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogUtils.d(" -> Scan device rssi is: " + rssi);
	        LogUtils.d(" -> Scan device scanRecord is: " + scanRecord.length);
//            LogUtils.d("the thread name:" + Thread.currentThread().getName());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
	                LogUtils.i(" -> onScanResult : deviceName:" + device.getName());
                    if (null != device.getName() && device.getName().contains(MainActivity.this.getString(cn.airburg.emo.R.string.device_name))) {
                        LogUtils.d(" -> onScanResult -> stop Scan");
                        // 停止扫描BLE设备
                        scanLeDevice(false);

                        // 找到了目标通信设备
                        MainActivity.this.mBluetoothDevice = device;

	                    // 发送找到emo设备消息
	                    mStateProcessHandler.sendEmptyMessage(SelfHandler.FOUND_EMO) ;

//	                    // 启动emo通信服务
//                        mServiceConnection = new SelfServiceConnection();
//
//                        // 启动服务
//                        LogUtils.d(" -> onScanResult : start bluetoothLeService. ");
//                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
//                        MainActivity.this.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    }
                }
            });
        }
    }

    /**
     * 和服务连接通道
     */
    private final class SelfServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LogUtils.d(" -> ");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                LogUtils.e(" -> Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.setBLEServiceCallback(mDCServiceCallback); // 设置回调
            boolean success = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
            LogUtils.d(" -> connect result:" + success);
            if (success) { // 连接初始化成功
                // 发送 连接emo设备初始化成功消息
                mStateProcessHandler.sendEmptyMessage(SelfHandler.CONNECT_EMO_INITIATED_SUCCESS) ;
            } else { // 连接初始化失败
                // 发送 连接emo设备初始化失败 消息
                mStateProcessHandler.sendEmptyMessage(SelfHandler.CONNECT_EMO_INITIATED_FAILURE) ;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.d(" -> current thread name: " + Thread.currentThread().getName());
            mBluetoothLeService = null;
            mBluetoothDevice = null ;

            mServiceConnection = null ;
        }
    };


    /**
     * BleService 回调实现类
     */
    private final class DCServiceCallback implements BluetoothLeService.BLEServiceCallback {

	    /**
	     * 显示写数据结果
	     */
	    public void displayWriteResult(final BluetoothGattCharacteristic characteristic, final int status) {
		    UUID characteristicUUID = characteristic.getUuid();
		    if(UUID.fromString(SelfManager.BLE_CHARACTERISTIC_FFF4).equals(characteristicUUID)) {
				// 读取历史数据
			    runOnUiThread(new Runnable() {
				    @Override public void run() {

				    }
			    }) ;
		    } else if(UUID.fromString(SelfManager.BLE_CHARACTERISTIC_FFF5).equals(characteristicUUID)) {
			    // 监测读取历史数据开关
			    runOnUiThread(new Runnable() {
				    @Override public void run() {
					    if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_READ_MONITOR_DATA)) {
						    ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_READ_MONITOR_DATA);
						    mCallbackHandler.removeCallbacks(mWriteReadMonitorDataNotifyResultRunnable);
					    }

					    // 延迟两秒读取 读取数据完毕状态
					    mCallbackHandler.postDelayed(new MonitorDataReadFinishedRunnable(), 2500) ;

//					    String msg = "FFF5 status: " + status + ", value: " + characteristic.getValue()[0] ;
//					    mTvMainTestResult.setText(msg);
//					    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				    }
			    });

		    } else if(UUID.fromString(SelfManager.BLE_CHARACTERISTIC_FFF6).equals(characteristicUUID)) {
				// 连续监测开关
			    runOnUiThread(new Runnable() {
                    @Override public void run() {

	                    if(1 == characteristic.getValue()[0]) { // 开启连续监控
		                    if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_OPEN_MONITOR_SWITCH)) {
			                    ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_OPEN_MONITOR_SWITCH) ;
		                    }
		                    mCallbackHandler.removeCallbacks(mWriteMonitorSwitchResultRunnable);

		                    // 写入 连续监测开始时间
					        writeMonitorStartDatetime(mGraphView.getStartDatetime());

	                    } else {    // 关闭连续监控
                            //判断F8， F8如果有值证明上一次监测结束，然后读历史，没有就不读。 0xFFF9
		                    if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_CLOSE_MONITOR_SWITCH)) {
			                    ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_CLOSE_MONITOR_SWITCH) ;
		                    }
		                    mCallbackHandler.removeCallbacks(mCloseMonitorSwitchResultRunnable);

							// 执行下一个监测任务
		                    executeNextMonitorTask();

//		                    DialogUtils.dismissProgressDialog();
	                    }

//	                    String msg = "FFF6 status: " + status + ", value: " + characteristic.getValue()[0] ;
//						mTvMainTestResult.setText(msg);
//	                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
		    } else if(UUID.fromString(SelfManager.BLE_CHARACTERISTIC_FFF8).equals(characteristicUUID)) {
				// 监测开始时间
			    runOnUiThread(new Runnable() {
				    @Override public void run() {

					    if(ContinuousMonitoringManager.sState.contains(ContinuousMonitoringManager.KEY_WRITE_MONITOR_START_DATETIME)) {
							ContinuousMonitoringManager.sState.remove(ContinuousMonitoringManager.KEY_WRITE_MONITOR_START_DATETIME) ;
					    }
					    mCallbackHandler.removeCallbacks(mWriteMonitorStartDatetimeResultRunnable);

					    // 执行下一个监测任务
					    executeNextMonitorTask();

//					    DialogUtils.dismissProgressDialog();

//					    long time = ByteUtils.getInt(characteristic.getValue(), 0) ;
//						String msg = null ;
//					    if(0 == time) {
//							msg = "0" ;
//					    } else {
//						    msg = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(time*1000)) ;
//					    }
//
//					    mTvMainTestResult.setText(msg);
//					    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				    }
			    });
		    }
	    }

        @Override public void displayRssi(final int rssi) {
//            runOnUiThread(new Runnable() {
//                  @Override public void run() {
//                      // LogUtils.i("displayRssi: " + rssi);
////                      MainActivity.this.displayRssi(String.valueOf(rssi));
//                  }
//              }
//            );
        }

        @Override public void displayData(final String data) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
//                    MainActivity.this.displayData(data);
                    LogUtils.i(" -> data: " + data);
                }
            });
        }

        /**
         * 显示设备类型
         * @param characteristic 特征值
         * @param data 读取到的数据
         */
        @Override public void displayDeviceType(BluetoothGattCharacteristic characteristic, byte[] data) {
//	        LogUtils.d(" data length:" + data.length);
            // 发送读取设备类型完毕消息
            LogUtils.e(" -> data" + data[0]);
            Message msg = mStateProcessHandler.obtainMessage() ;
            msg.what = SelfHandler.READ_DEVICE_TYPE_FINISHED ;
            msg.arg1 = data[0] ; // 设备类型
            msg.sendToTarget();
        }

        /**
         * 显示电池电量数据
         * @param data
         */
        @Override public void displayBattery(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
//                    LogUtils.d("data lenght:" + data.length);
//                    LogUtils.d("data:" + data[0]);
                    // 取得电量数据
                    mBatteryPower = data[0] ;
                    if(mBatteryPower >= 10) {
                        mTvPagerOneBattery.setText(String.format(MainActivity.this.getString(cn.airburg.emo.R.string.remain_battery), mBatteryPower));
                    } else {
                        mTvPagerOneBattery.setText(String.format(MainActivity.this.getString(cn.airburg.emo.R.string.remain_battery_02), mBatteryPower));
                    }

                    // 设置电量进度
                    mProgressPageOneBattery.setProgress(mBatteryPower);
                }
            });
        }

        /**
         * 显示PM2.5数据
         */
        @Override public void displayHazeValue(final BluetoothGattCharacteristic characteristic, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(null != mHazeParser) {
                        // 解析得到 haze数据
	                    mHazeValue = (short)(mHazeParser.parser(data) * HazeParser.HAZE_CORRECT_FACTOR) ;
                    }

                    // 设置pm2.5显示值
                    mTvPageOneHazeValue.setText(mHazeValue + "");
//                    mTvPagerOnePM2dot5Desc.setText(getHazeValueDesc(mHazeValue));

                    mTvPageOneHazeValueForeDesc.setOnClickListener(tvDescButtonOnClickListener);
                    // 开始播放转化动画

                    if(isWarmUp){
                        mTvPageOneHazeValueForeDesc.setText(getString(R.string.device_warm_up_title));
                    }else{

                        startAnimation() ;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageButton ivShare = (ImageButton) mPageOne.findViewById(R.id.btnPagerOneShare);
                                ivShare.setVisibility(View.VISIBLE);

                                ImageButton ivCamera = (ImageButton) mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneCamera);
                                ivCamera.setVisibility(View.VISIBLE);

                                ImageButton ivShareDisabled = (ImageButton) mPageOne.findViewById(R.id.btnPagerOneShare_disabled);
                                ivShareDisabled.setVisibility(View.GONE);

                                ImageButton ivCameraDisabled = (ImageButton) mPageOne.findViewById(cn.airburg.emo.R.id.btnPagerOneCamera_disabled);
                                ivCameraDisabled.setVisibility(View.GONE);

                                ImageButton ivShareTwo = (ImageButton) mPageTwo.findViewById(R.id.btnPagerTwoShare);
                                ivShareTwo.setVisibility(View.VISIBLE);
                                ImageButton ivShareDisabledTwo = (ImageButton) mPageTwo.findViewById(R.id.btnPagerTwoShare_disabled);
                                ivShareDisabledTwo.setVisibility(View.GONE);
                            }
                        });
                    }

                }
            });
        }

	    /**
	     * 显示连续监测开关状态
	     * @param characteristic 连续监测开关状态特征值
	     * @param data 数据
	     */
	    @Override
	    public void displayMonitorSwitch(BluetoothGattCharacteristic characteristic, final byte[] data) {
		    LogUtils.d(String.valueOf(data[0]));

		    final int openState = data[0] ;

		    // 发送 历史数据开关读取 完毕
		    Message message = mStateProcessHandler.obtainMessage() ;
		    message.what = SelfHandler.READ_MONITOR_SWITCH_FINISHED;
		    message.arg1 = openState ;
		    mStateProcessHandler.sendMessage(message) ;

//		    runOnUiThread(new Runnable() {
//			    @Override public void run() {
//				    String msg = "历史记录开关数据为：" + openState ;
//				    mTvMainTestResult.setText(msg);
//				    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
//			    }
//		    });
	    }

	    /**
	     * 显示连续监测开始时间
	     * @param characteristic 连续监测开始时间特征值
	     * @param data 数据
	     */
	    @Override
	    public void displayMonitorStartDatetime(BluetoothGattCharacteristic characteristic, final byte[] data) {
		    LogUtils.d(ByteUtils.getInt(data, 0) + "");

		    final int time = ByteUtils.getInt(data, 0) ;


		    Date datetime = null ;

		    if(0 == data[0]) {
				datetime = null ;
		    } else {
			    try {
				    datetime = Utils.getDateFromByteArray(data);
			    } catch (ParseException e) {
				    e.printStackTrace();
				    LogUtils.d(e.getMessage());
				    datetime = new Date();
			    }
		    }

		    // 发送 历史数据开关读取 完毕
		    Message message = mStateProcessHandler.obtainMessage() ;
		    message.what = SelfHandler.READ_MONITOR_START_DATETIME_FINISHED;
		    message.obj = datetime ;
		    mStateProcessHandler.sendMessage(message) ;

//		    runOnUiThread(new Runnable() {
//			    @Override
//			    public void run() {
//
//				    String msg = null ;
//				    if(0 == time) {
//					    msg = "0" ;
//				    } else {
//					    msg = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(time*1000)) ;
//				    }
//
//				    msg = "开始监测时间: " + msg;
//				    mTvMainTestResult.setText(msg);
//				    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
//			    }
//		    });

//			    runOnUiThread(new Runnable() {
//				    @Override public void run() {
//
//					    // 设置开始时间
//					    Date startDatetime = new Date() ;
//					    startDatetime.setTime(time*1000);
//					    mGraphView.setStartDatetime(startDatetime);
//				    }
//			    });

	    }

	    /**
	     * 显示连续监测读取状态
	     * @param characteristic 连续监测读取数据状态特征值
	     * @param data 数据
	     */
	    @Override
	    public void displayMonitorReadSwitch(BluetoothGattCharacteristic characteristic, final byte[] data) {
//		    LogUtils.d(JSON.toJSONString(data.length));
		    LogUtils.d(JSON.toJSONString(data[0]));

		    if(0 == data[0]) { // 读取监测数据完毕
				mStateProcessHandler.sendEmptyMessage(SelfHandler.READ_MONITOR_DATA_FINISHED) ;
		    }
//		    else {
//			    // 发送 读取数据完毕状态
//			    mCallbackHandler.postDelayed(new MonitorDataReadFinishedRunnable(), 2500) ;
//		    }

//		    runOnUiThread(new Runnable() {
//			    @Override
//			    public void run() {
//				    int openState = data[0];
//
//				    String msg = "历史记录读取开关数据为：" + openState;
//				    mTvMainTestResult.setText(msg);
//				    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
//			    }
//		    });
	    }

	    /**
	     * 显示连续监测 历史数据
	     * @param characteristic 连续监测历史数据特征值
	     * @param data 数据
	     */
	    @Override
	    public void displayMonitorData(BluetoothGattCharacteristic characteristic, final byte[] data) {
		    LogUtils.d(JSON.toJSONString(data.length));

		    // 发送读取监测间断数据完毕
		    Message msg = mStateProcessHandler.obtainMessage() ;
		    msg.what = SelfHandler.READ_MONITOR_FRAGMENT_DATA_FINISHED;
		    msg.obj = data ;
		    msg.sendToTarget();

//		    runOnUiThread(new Runnable() {
//			    @Override
//			    public void run() {
//				    String msg = "读取到的历是数据长度为：" + data.length;
//				    mTvMainTestResult.setText(msg);
//				    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
//			    }
//		    });

//		    LogUtils.d(JSON.toJSONString(data[0]));
	    }

        @Override public void notifyConnectedGATT() {
            LogUtils.d("connected GATT success.");

            // 发送 和emo建立了连接消息
            mStateProcessHandler.sendEmptyMessage(SelfHandler.CONNECTED_EMO) ;

//            runOnUiThread(new Runnable() {
//                @Override public void run() {
//                }
//            });

        }

        @Override public void notifyDisconnectedGATT() {
            LogUtils.d("disconnected GATT.");

            // 发送 和emo断开了连接消息
            mStateProcessHandler.sendEmptyMessage(SelfHandler.DISCONNECTED_EMO) ;

//            runOnUiThread(new Runnable() {
//                @Override public void run() {
//                }
//            });
        }

        @Override public void displayGATTServices() {
            LogUtils.d(" -> displayGATTServices. ");
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    if (mBluetoothLeService != null) {
                        // MainActivity.this.displayGattServices(mBluetoothLeService.getSupportedGattServices());

                        // 电池service
                        mBatteryGattService = mBluetoothLeService.getGattService(UUID.fromString(MainActivity.this.getString(cn.airburg.emo.R.string.service_battery_uuid))) ;

//                        LogUtils.d("displayGATTServices mBatteryGattService:" + JSON.toJSONString(mBatteryGattService));

                        // 读取电池电量
//                        BluetoothGattCharacteristic gattBatteryCharacteristic = mBatteryGattService.getCharacteristic(UUID.fromString(MainActivity.this.getString(R.string.battery_level_uuid))) ;
//                        mBluetoothLeService.setCharacteristicNotification(gattBatteryCharacteristic, true);
//                        mBluetoothLeService.readCharacteristic(gattBatteryCharacteristic);

                        // pm2.5 service
                        mHazeValueGattService = mBluetoothLeService.getGattService(UUID.fromString(MainActivity.this.getString(cn.airburg.emo.R.string.service_pm2dot5_uuid))) ;
                        if (mHazeValueGattService == null){
                            LogUtils.e("mHazeValueGattService is null-------------------------");
                        }

                        List<BluetoothGattCharacteristic> gattCharacteristicList =  mHazeValueGattService.getCharacteristics() ;

                        String gattCharacteristicUUID = null ;
                        for(final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristicList) {
                            gattCharacteristicUUID = gattCharacteristic.getUuid().toString() ;
                            LogUtils.d("gattCharacteristicUUID: " + gattCharacteristicUUID);
	                        SelfManager.getInstance().putBluetoothGattCharacteristic(gattCharacteristic);
                        }

	                    // 打开历史数据notify开关
	                    SelfManager.getInstance().openContinuousMonitoringHistoryDataNotification(mBluetoothLeService) ;


//                        // 启动获取电量线程
//                        launchGatherBatteryThread() ;
//
//	                    // 启动实时获取pm2.5 线程
//	                    launchGatherHazeValueThread();

	                    // 发送发现 emo services消息
	                    mStateProcessHandler.sendEmptyMessage(SelfHandler.EMO_SERVICES_DISCOVERED) ;
                    }
                }
            });
        }
    }

    /**
     * 搜索EMO设备超时Runnable
     */
    private final class ScanLeDeviceTimeoutRunnable implements Runnable {

        @Override public void run() {
            LogUtils.d(" -> ");
            // Stops scanning after a pre-defined scan period.
            // 如果还在扫描 or 此时正处于扫描的状态，那么停止扫描 （在扫描回调接口中如果找到了指定的设备，那会会取消扫描）
            if (mScanning) {
                scanLeDevice(false); // 停止扫描
                // 没有找到emo设备 and 处于扫描状态
                if (null == mBluetoothDevice && EMOStatus.STATUS_SCAN_EMO_DEVICE == mEMOStatus) {
                    // 发送 没有发现emo设备的消息
                    mStateProcessHandler.sendEmptyMessage(SelfHandler.NOT_FOUND_EMO);
                }
            }
        }
    }

    /**
     * 连接超时检测
     */
    private final class ConnectEMODeviceTimeoutRunnable implements Runnable {
        /**
         * 超时时间 15秒
         */
        final static int TIMEOUT_PERIOD = 15 * 1000 ;

        @Override public void run() {
            LogUtils.w(" -> connect emo device time out, mEMOStatus: " + mEMOStatus.getName());
            // 发送连接失败 消息
            mStateProcessHandler.sendEmptyMessage(SelfHandler.CONNECT_EMO_TIMEOUT) ;

            removeHandlerCallback(mConnectEMODeviceTimeoutRunnable) ;
            mConnectEMODeviceTimeoutRunnable = null ;
        }
    }

    /**
     * emo设备服务发现超时
     */
    private final class EMOServicesDiscoveredTimeoutRunnable implements Runnable {
        /**
         * 超时时间 15秒
         */
        final static int TIMEOUT_PERIOD = 15 * 1000 ;

        @Override public void run() {
            LogUtils.w(" -> emo Services Discovered time out, mEMOStatus: " + mEMOStatus.getName());
            // 发送发现 emo Services 超时消息
            mStateProcessHandler.sendEmptyMessage(SelfHandler.EMO_SERVICES_DISCOVERED_TIMEOUT) ;

            removeHandlerCallback(mEMOServicesDiscoveredTimeoutRunnable) ;
            mEMOServicesDiscoveredTimeoutRunnable = null ;
        }
    }

    /**
     * 读取设备类型超时
     */
    private final class ReadDeviceTypeTimeoutRunnable implements Runnable {
        /**
         * 超时时间 5秒
         */
        final static int TIMEOUT_PERIOD = 5 * 1000 ;

        @Override public void run() {
            LogUtils.w(" -> read device type time out, mEMOStatus: " + mEMOStatus.getName());
            removeHandlerCallback(mReadDeviceTypeTimeoutRunnable) ;
            mReadDeviceTypeTimeoutRunnable = null ;

            // 发送 读取设备类型 超时消息
            mStateProcessHandler.sendEmptyMessage(SelfHandler.READ_DEVICE_TYPE_TIMEOUT) ;

        }
    }

	/**
     * 隐藏电池显示
     */
    private final class HideBatteryTipRunnable implements Runnable {
        @Override
        public void run() {
            mLinearLayoutPageOneBatteryText.setVisibility(View.INVISIBLE);
        }
    }

	/**
	 * 定时监测读取数据完毕
	 */
	private final class MonitorDataReadFinishedRunnable implements Runnable {
		@Override public void run() {
			ContinuousMonitoringManager.sState.add(ContinuousMonitoringManager.KEY_READ_MONITOR_NOTIFICATION_SWITCH) ;
			// 读取 读取连续监测数据完毕 状态
			TaskManager.getInstance().addTask(new TaskManager.Task() {
                @Override
                public void run() {
                    SelfManager.getInstance().readMonitorReadSwitch(mBluetoothLeService);

                    mCallbackHandler.postDelayed(mReadMonitorDataNotifyResultRunnable, MONITOR_WRITE_DATA_TIME);
                }
            });
		}
	}

	/**
	 * 读取监控状态结果runnable
	 */
	private final class ReadMonistorSwitchResultRunnable implements Runnable {

		@Override public void run() {
			// 读取连续监控开关
			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override public void run() {
					// 读取连续监测开关
					SelfManager.getInstance().readMonitoringSwitch(mBluetoothLeService);

					mCallbackHandler.postDelayed(mReadMonitorSwitchResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 * 写入监控状态结果runnable
	 */
	private final class WriteMonitorSwitchResultRunnable implements Runnable {

		@Override public void run() {

			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override public void run() {
					// 打开监控状态
					SelfManager.getInstance().openMonitorSwitch(mBluetoothLeService);

					mCallbackHandler.postDelayed(mWriteMonitorSwitchResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 * 关闭监控状态结果runnable
	 */
	private final class CloseMonitorSwitchResultRunnable implements Runnable {

		@Override public void run() {
			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override public void run() {
					// 关闭监控状态
					SelfManager.getInstance().closeMonitorSwitch(mBluetoothLeService);

					mCallbackHandler.postDelayed(mCloseMonitorSwitchResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 * 读取监控时间结果runnable
	 */
	private final class ReadMonitorStartDatetimeResultRunnable implements Runnable {

		@Override public void run() {
			// 读取连续监控开始时间
			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override public void run() {
					// 读取连续监测开始时间
					SelfManager.getInstance().readMonitorStartDatetime(mBluetoothLeService);

					mCallbackHandler.postDelayed(mReadMonitorStartDatetimeResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 * 写入监控时间结果runnable
	 */
	private final class WriteMonitorStartDatetimeResultRunnable implements Runnable {

		@Override
		public void run() {
			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override public void run() {
					// 写入开始监测时间
					SelfManager.getInstance().writeMonitorStartDatetime(mBluetoothLeService, mGraphView.getStartDatetime());

					mCallbackHandler.postDelayed(mWriteMonitorStartDatetimeResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 *  获取监控数据 读取notify结果 runnable
	 */
	private final class ReadMonitorDataNotifyResultRunnable implements Runnable {

		@Override
		public void run() {
			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override
				public void run() {
					SelfManager.getInstance().readMonitorReadSwitch(mBluetoothLeService);

					mCallbackHandler.postDelayed(mReadMonitorDataNotifyResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 * 写入 获取监控数据 写入notify结果 runnable
	 */
	private final class WriteReadMonitorDataNotifyResultRunnable implements Runnable {

		@Override
		public void run() {
			TaskManager.getInstance().addTask(new TaskManager.Task() {
				@Override
				public void run() {
					SelfManager.getInstance().readMonitorData(mBluetoothLeService);

					mCallbackHandler.postDelayed(mWriteReadMonitorDataNotifyResultRunnable, MONITOR_WRITE_DATA_TIME) ;
				}
			});
		}
	}

	/**
	 * 重试扫描ble设备
	 */
	private final class RetryScanLEDeviceRunnable implements Runnable {
		@Override
		public void run() {
			// 扫描设备
			scanLeDevice(true);
		}
	}

    /**
     * ViewPager切换页面监听器接口实现类
     */
    private final class SelfPageChangeListener implements ViewPager.OnPageChangeListener {
        ImageView imgDotOne ;
        ImageView imgDotTwo ;
	    /**
	     * 是否为第一次进入pageTwo，默认为false，表示非
	     */
	    boolean isFirstEnterPageTwo = false ;

        SelfPageChangeListener() {
            imgDotOne = (ImageView) findViewById(cn.airburg.emo.R.id.imgDotOne);
            imgDotOne.setImageResource(cn.airburg.emo.R.drawable.circle_white);

            imgDotTwo = (ImageView) findViewById(cn.airburg.emo.R.id.imgDotTwo);
            imgDotTwo.setImageResource(cn.airburg.emo.R.drawable.circle_gray);
            imgDotTwo.setImageAlpha(51);
        }


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

        @Override
        public void onPageSelected(int position) {
            if (0 == position) {
//	            LogUtils.d(" -> position: " + position);

                imgDotOne.setImageResource(cn.airburg.emo.R.drawable.circle_white);
                this.imgDotOne.setImageAlpha(255);

                imgDotTwo.setImageResource(cn.airburg.emo.R.drawable.circle_gray);
                this.imgDotTwo.setImageAlpha((int)(255*0.2));

	            // 发送进入第一页 消息
	            mStateProcessHandler.sendEmptyMessage(SelfHandler.ENTER_PAGEVIEW_0) ;

            } else {
//	            LogUtils.d(" -> position: " + position);
	            if(!isFirstEnterPageTwo) {  // 第一次进入page two
		            isFirstEnterPageTwo = true ;

		            // 发送第一次进入 page two页面
		            mStateProcessHandler.sendEmptyMessage(SelfHandler.FIRST_ENTER_CONTINUOUS_MONITORING_VIEW) ;
	            }

                imgDotOne.setImageResource(cn.airburg.emo.R.drawable.circle_gray);
                this.imgDotOne.setImageAlpha((int)(255*0.2));

                imgDotTwo.setImageResource(cn.airburg.emo.R.drawable.circle_white);
                this.imgDotTwo.setImageAlpha(255);

	            // 发送进入第二页 消息
	            mStateProcessHandler.sendEmptyMessage(SelfHandler.ENTER_PAGEVIEW_1) ;
            }
        }

        @Override public void onPageScrollStateChanged(int state) { }
    }

	/**
	 * 蓝牙状态接收者
	 */
	private final class BluetoothStateChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
//			LogUtils.d(JSON.toJSONString(intent));
			// 蓝牙打开广播
			if(intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
				if(mBluetoothAdapter.isEnabled()) { // 打开了蓝牙
					LogUtils.d("open bluetooth.");
					// 发送蓝牙打开消息
					mStateProcessHandler.sendEmptyMessage(SelfHandler.OPENED_BLUETOOTH) ;
				} else {
					LogUtils.d("not open bluetooth.");
				}
			}
		}
	}

	/**
	 * 捕获home键事件广播接收者
	 */
	private final class SelfHomeKeyEventReceiver extends BroadcastReceiver {
		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";
		String SYSTEM_HOME_KEY_LONG = "recentapps";

		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtils.d(" -> ");
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
					//表示按了home键,程序到了后台
					// Toast.makeText(getApplicationContext(), "home", 1).show();
					finish() ;
				}
//				else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
//					//表示长按home键,显示最近使用的程序列表
//				}
			}
		}
	}

    /**
     * 开启和关闭屏幕广播消息接收者实现类
     */
    private final class SelfScreenOnOrOffEventReceiver extends BroadcastReceiver {
//        android.intent.action.SCREEN_OFF
        @Override public void onReceive(Context context, Intent intent) {
            LogUtils.d(" -> Action:" + intent.getAction());
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // 屏幕关闭事件
                LogUtils.d(" -> screen off");
                // TODO 待实现
                // 发送屏幕关闭消息
                mStateProcessHandler.sendEmptyMessage(SelfHandler.SCREEN_OFF) ;
            } else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // 屏幕点亮事件
                LogUtils.d(" -> screen on");
                // TODO 待实现
                mStateProcessHandler.sendEmptyMessage(SelfHandler.SCREEN_ON) ;
            }
        }
    }

	/**
	 * 消息处理Handler
	 */
	private final class SelfHandler extends Handler {
		/**
		 * 进入场景
		 */
		final static int ENTER_SCENE            =   0x000050 ;
		/**
		 * 打开蓝牙
		 */
		final static int OPENED_BLUETOOTH       =   0x000052 ;
		/**
		 * 未打开蓝牙
		 */
		final static int NOT_OPENED_BLUETOOTH   =   0x000054 ;
		/**
		 * 初始化状态
		 */
		final static int INIT_STATE             =   0x000099 ;
		/**
		 * 搜索emo
		 */
		final static int SCAN_EMO               =   0x000100 ;
		/**
		 * 没有发现emo
		 */
		final static int NOT_FOUND_EMO          =   0x000102 ;

		/**
		 * 发现了emo设备
		 */
		final static int FOUND_EMO              =   0x000104 ;

		/**
		 * 和emo建立了连接
		 */
		final static int CONNECTED_EMO          =   0x000106 ;

		/**
		 * 和emo断开了连接
		 */
		final static int DISCONNECTED_EMO       =   0x000108 ;

		/**
		 *  发现 EMO Services , 在CONNECTED_EMO后执行
		 */
		final static int EMO_SERVICES_DISCOVERED    =   0x000110 ;

        /**
         * 发现 EMO Services超时
         */
        final static int EMO_SERVICES_DISCOVERED_TIMEOUT = 0x000111 ;

		/**
		 * 重新连接emo设备
		 */
		final static int RECONNECTED_EMO            =   0x000112 ;

        /**
         * 连接emo设备初始化失败
         */
        final static int CONNECT_EMO_INITIATED_FAILURE  =   0x000114 ;

        /**
         * 连接emo设备初始化成功
         */
        final static int CONNECT_EMO_INITIATED_SUCCESS  =   0x000116 ;

        /**
         * 连接emo设备超时
         */
        final static int CONNECT_EMO_TIMEOUT            =   0x000118 ;

        /**
         * SCREEN_ON 屏幕点亮了
         */
        final static int SCREEN_ON                  =   0x000120 ;

        /**
         * SCREEN_OFF 点击了电源键，将屏幕关闭掉
         */
        final static int SCREEN_OFF                 = 0x000122 ;


		/**
		 * 第一次进入第二页，连续监测页面
		 */
		final static int FIRST_ENTER_CONTINUOUS_MONITORING_VIEW     =   0x000200 ;

		/**
		 * 进入第一页
		 */
		final static int ENTER_PAGEVIEW_0           =   0x000202 ;

		/**
		 * 进入第二页
		 */
		final static int ENTER_PAGEVIEW_1           =   0x000204 ;

		/**
		 * pm2dot5采集线程停止
		 */
		final static int THREAD_PM2DOT5_STOPPED =   0x000210 ;

		/**
		 * battery power 采集线程停止
		 */
		final static int THREAD_BATTERY_POWER_STOPPED = 0x000212 ;

		/**
		 * 读取连续监测开关完毕
		 */
		final static int READ_MONITOR_SWITCH_FINISHED = 0x000220 ;

		/**
		 * 读取监测时间完毕
		 */
		final static int READ_MONITOR_START_DATETIME_FINISHED = 0x000222 ;

		/**
		 *  读取监测片段数据完毕
		 */
		final static int READ_MONITOR_FRAGMENT_DATA_FINISHED = 0x000224 ;

		/**
		 *  读取监测数据完毕
		 */
		final static int READ_MONITOR_DATA_FINISHED     = 0x000226 ;

        /**
         * 读取设备类型完毕
         */
        final static int READ_DEVICE_TYPE_FINISHED      = 0x000228 ;

        /**
         * 读取设备类型超时
         */
        final static int READ_DEVICE_TYPE_TIMEOUT       = 0x000230 ;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case ENTER_SCENE: { // 进入场景
//					LogUtils.d("ENTER_SCENE");
					doEnterScene() ;
					break ;
				}
				case OPENED_BLUETOOTH: {  // 打开蓝牙
					doOpenedBluetooth() ;
					break ;
				}
				case NOT_OPENED_BLUETOOTH: { // 没有打开蓝牙
					doNotOpenedBluetooth() ;
					break ;
				}
				case INIT_STATE: {
					doInitState() ;
					break ;
				}
				case SCAN_EMO: {
					doScanEMODevice() ;
					break ;
				}
				case NOT_FOUND_EMO : {
					doNotFoundEMODevice() ;
					break ;
				}
				case FOUND_EMO: {
					doFoundEMODevice();
					break ;
				}
				case CONNECTED_EMO: {
					doConnectedEMODevice() ;
					break ;
				}
				case DISCONNECTED_EMO: { // 和emo断开了连接
					doDisconnectedEMODevice() ;
					break ;
				}
                case CONNECT_EMO_INITIATED_SUCCESS: { // 成功连接了初始化emo设备
                    doConnectEMODeviceInitiatedSuccess() ;
                    break ;
                }
                case CONNECT_EMO_INITIATED_FAILURE: { // 连接emo设备初始化失败
                    doConnectEMODeviceInitiatedFailure() ;
                    break;
                }
                case CONNECT_EMO_TIMEOUT: { // 连接emo设备超时
                    doConnectedEMODeviceTimeout();
                    break ;
                }
                case SCREEN_ON: { // 屏幕点亮了
                    doScreenOn();
                    break ;
                }
                case SCREEN_OFF: { // 屏幕关闭了
                    doScreenOff() ;
                    break ;
                }
				case RECONNECTED_EMO: { // 重新连接emo设备
					doReconnectEMODevice() ;
					break ;
				}
				case EMO_SERVICES_DISCOVERED: { // 发现 EMO Services , 在CONNECTED_EMO后执行
					doEMOServicesDiscovered() ;
					break ;
				}
                case EMO_SERVICES_DISCOVERED_TIMEOUT: {// 发现 EMO Services 超时, 在CONNECTED_EMO后执行
                    doEMOServicesDiscoveredTimeout() ;
                    break ;
                }
				case FIRST_ENTER_CONTINUOUS_MONITORING_VIEW: { // 第一次进入连续监测页面
					doFirstEnterContinuousMonitoringView() ;
					break ;
				}
				case ENTER_PAGEVIEW_0: { // 进入第一页
					doEnterPageView0() ;
					break ;
				}
				case ENTER_PAGEVIEW_1: { // 进入第二页
					doEnterPageView1() ;
					break ;
				}
				case THREAD_PM2DOT5_STOPPED: { // pm2dotm采集线程停止
					doHazeValueThreadStopped() ;
					break ;
				}
				case THREAD_BATTERY_POWER_STOPPED: { // battery power采集线程停止
					doBatteryPowerThreadStopped();
					break ;
				}
				case READ_MONITOR_SWITCH_FINISHED: { // 读取监测开关状态完毕
					int openSwitch = msg.arg1 ;
					doReadMonitorSwitchFinished(openSwitch) ;
					break ;
				}
				case READ_MONITOR_START_DATETIME_FINISHED: { // 读取监测开始时间完毕
					doReadMonitorStartDatetimeFinished((Date)msg.obj) ;
					break ;
				}
				case READ_MONITOR_FRAGMENT_DATA_FINISHED: { // 读取片段监测数据完毕
					byte[] monitorData = (byte[])msg.obj ;
					doReadMonitorFragmentDataFinished(monitorData) ;
					break ;
				}
				case READ_MONITOR_DATA_FINISHED: {  // 读取监测数据完毕
					doReadMonitorDataFinished() ;
					break ;
				}
                case READ_DEVICE_TYPE_FINISHED: { // 读取设备类型完毕
                    doReadDeviceTypeFinished(msg.arg1) ;
                    break ;
                }
                case READ_DEVICE_TYPE_TIMEOUT: { // 读取设备类型超时
                    doReadDeviceTypeTimeout() ;
                    break ;
                }
			}
		}
	}


}
