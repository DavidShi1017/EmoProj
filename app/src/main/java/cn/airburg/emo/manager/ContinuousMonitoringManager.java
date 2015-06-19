package cn.airburg.emo.manager;

import cn.airburg.emo.view.GraphView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 连续监测管理器类
 * Created by YangJie on 2015/1/28.
 */
public class ContinuousMonitoringManager {
	/**
	 * 读取MonitorSwitch
	 */
	public final static String KEY_READ_MONITOR_SWITCH = "key_read_monitor_switch" ;

	/**
	 * 写入MonitorSwitch 也就是打开 连续监控开关
	 */
	public final static String KEY_OPEN_MONITOR_SWITCH = "key_open_monitor_switch" ;

	/**
	 * 关闭 MonistorSwitch 也就是关闭 连续监控开关
	 */
	public final static String KEY_CLOSE_MONITOR_SWITCH = "key_close_monitor_switch" ;

	/**
	 * 读取监测开始时间 MonitorStartDatetime
	 */
	public final static String KEY_READ_MONITOR_START_DATETIME = "key_read_monitor_start_datetime" ;

	/**
	 * 写入 监测开始时间 write MonitorStartDatetime
	 */
	public final static String KEY_WRITE_MONITOR_START_DATETIME = "key_write_monitor_start_datetime" ;

	/**
	 * 读取监测数据 MonitorData
	 */
	public final static String KEY_READ_MONITOR_DATA = "key_read_monitor_data" ;

	/**
	 *  读取连续监测数据通知状态
	 */
	public final static String KEY_READ_MONITOR_NOTIFICATION_SWITCH = "key_read_monitor_notification_switch" ;


	/**
	 * 状态
	 */
	public static Set<String> sState = new HashSet<String>() ;

	/**
	 * 连续监测状态，默认为未监测状态false
	 */
	private boolean mStateMonitor ;

	/**
	 * 绘图组件
	 */
	private GraphView mGraphView ;

	/**
	 * 历史数据Array
	 */
	private List<Byte> mMonitorDataArray = new ArrayList<Byte>() ;

	public ContinuousMonitoringManager(GraphView graphView) {
		this.mGraphView = graphView ;
	}

	/**
	 * 添加片段历史数据
	 * @param fragment
	 */
	public void addMonitorDataArray(byte[] fragment) {
		for (byte b : fragment) {
			mMonitorDataArray.add(b);
		}
	}

	/**
	 * 获取历史数据
	 */
	public byte[] getMonitorDataArray() {
		byte[] monitorDataArray = new byte[this.mMonitorDataArray.size()] ;

		for(int i=0; i<this.mMonitorDataArray.size(); ++i) {
			monitorDataArray[i] = this.mMonitorDataArray.get(i) ;
		}
		this.mMonitorDataArray.clear();

		return monitorDataArray ;
	}

	public GraphView getGraphView() {
		return mGraphView;
	}

	public void setGraphView(GraphView graphView) {
		this.mGraphView = graphView;
	}

	public boolean isStateMonitor() {
		return mStateMonitor;
	}

	public void setStateMonitor(boolean mStateMonitor) {
		this.mStateMonitor = mStateMonitor;
	}
}
