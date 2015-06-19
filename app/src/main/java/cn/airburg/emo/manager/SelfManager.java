package cn.airburg.emo.manager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.text.TextUtils;

import cn.airburg.emo.service.BluetoothLeService;
import cn.airburg.emo.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by u17 on 2015/1/25.
 */
public class SelfManager {
	/**
	 * 0xfff0 服务
	 */
	public final static String BLE_CHARACTERISTIC_FFF0      =   "0000fff0-0000-1000-8000-00805f9b34fb" ;

	/**
	 * fff1特征值
	 */
	public final static String BLE_CHARACTERISTIC_FFF1      =   "0000fff1-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff2特征值，测得pm2.5数据
	 */
	public final static String BLE_CHARACTERISTIC_FFF2      =   "0000fff2-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff3特征值
	 */
	public final static String BLE_CHARACTERISTIC_FFF3      =   "0000fff3-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff4特征值，历史数据
	 */
	public final static String BLE_CHARACTERISTIC_FFF4      =   "0000fff4-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff5特征值，1个字节获取历史数据时写入1，当变为0时，数据读取完毕
	 */
	public final static String BLE_CHARACTERISTIC_FFF5      =   "0000fff5-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff6特征值，历史数据开关，1个字节
	 */
	public final static String BLE_CHARACTERISTIC_FFF6      =   "0000fff6-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff7特征值
	 */
	public final static String BLE_CHARACTERISTIC_FFF7      =   "0000fff7-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff8特征值，表示连续监测时间，5个字节
	 */
	public final static String BLE_CHARACTERISTIC_FFF8      =   "0000fff8-0000-1000-8000-00805f9b34fb" ;
	/**
	 * fff9特征值
	 */
	public final static String BLE_CHARACTERISTIC_FFF9      =   "0000fff9-0000-1000-8000-00805f9b34fb" ;

	/**
	 * SelfManager单例对象
	 */
	private static final SelfManager mInstance = new SelfManager() ;

	/**
	 * 特征值
	 */
	private Map<String, BluetoothGattCharacteristic> mBluetoothGattCharacteristicMap = new HashMap<String, BluetoothGattCharacteristic>(9) ;


	private SelfManager() {}

	/**
	 * 获取SelfManager单例对象
	 * @return SelfManager单例对象
	 */
	public static SelfManager getInstance() {
		return mInstance ;
	}


	/**
	 * 加入特征值
	 * @param bgc
	 */
	public void putBluetoothGattCharacteristic(BluetoothGattCharacteristic bgc) {
		if(null != bgc) {
			mBluetoothGattCharacteristicMap.put(bgc.getUuid().toString(), bgc) ;
		}
	}

	/**
	 * 获取特征值对象
	 * @param uuidKey 特征值对象的uuidkey
	 * @return  如果存在返回特征值对象，否者返回null
	 */
	public BluetoothGattCharacteristic getBluetoothGattCharacteristic(String uuidKey) {
		if(!TextUtils.isEmpty(uuidKey) && mBluetoothGattCharacteristicMap.containsKey(uuidKey)) {
			return mBluetoothGattCharacteristicMap.get(uuidKey) ;
		}

		return null ;
	}

	/**
	 * 开启历史数据主动提示
	 */
	public void openContinuousMonitoringHistoryDataNotification(BluetoothLeService bluetoothLeService) {
		bluetoothLeService.setCharacteristicNotification(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF4), true);
	}

	/**
	 * 读取连续监测开关，
	 * @param bluetoothLeService ble服务
	 */
	public void readMonitoringSwitch(BluetoothLeService bluetoothLeService) {
		if(null != bluetoothLeService) {
			bluetoothLeService.readCharacteristic(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF6));
		}
	}

	/**
	 * 打开连续监测开关
	 * @param bluetoothLeService ble服务
	 */
	public void openMonitorSwitch(BluetoothLeService bluetoothLeService) {
		if(null != bluetoothLeService) {
			BluetoothGattCharacteristic bgc = mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF6) ;
			bgc.setValue(new byte[]{1}) ;
			// bluetoothLeService.readCharacteristic(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF6));
			bluetoothLeService.writeCharacteristic(bgc);
		}
	}

	/**
	 * 关闭连续监测开关
	 * @param bluetoothLeService ble服务
	 */
	public void closeMonitorSwitch(BluetoothLeService bluetoothLeService) {
		if(null != bluetoothLeService) {
			BluetoothGattCharacteristic bgc = mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF6) ;
			bgc.setValue(new byte[]{0}) ;
			// bluetoothLeService.readCharacteristic(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF6));
			bluetoothLeService.writeCharacteristic(bgc);
		}
	}

	/**
	 * 读取连续监测开始时间
	 * @param bluetoothLeService ble服务
	 */
	public void readMonitorStartDatetime(BluetoothLeService bluetoothLeService) {
		if(null != bluetoothLeService) {
			bluetoothLeService.readCharacteristic(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF8));
		}
	}

	/**
	 * 设置连续监测的开始时间
	 * @param bluetoothLeService ble服务
	 * @param startDatetime 开始时间
	 */
	public void writeMonitorStartDatetime(BluetoothLeService bluetoothLeService, Date startDatetime) {
		if(null != bluetoothLeService) {
			BluetoothGattCharacteristic bgc = mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF8) ;
//			LogUtils.d(startDatetime.getTime()+"");
//			byte[] data = bgc.getValue() ;
//			if(data == null) {
//				data = new byte[5] ;
//			}
//			ByteUtils.putInt(data, (int) (startDatetime.getTime() / 1000), 0);
////			LogUtils.d(ByteUtils.getInt(data, 0) + "");
//			bgc.setValue(data) ;

			bgc.setValue(Utils.getByteArrayFromDate(startDatetime)) ;

			// 写入数据
			bluetoothLeService.writeCharacteristic(bgc);
		}
	}

	/**
	 * 读取 读取连续监测数据是否 状态
	 * @param bluetoothLeService ble服务
	 */
	public void readMonitorReadSwitch(BluetoothLeService bluetoothLeService) {
		if(null != bluetoothLeService) {
			bluetoothLeService.readCharacteristic(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF5));
		}
	}

	/**
	 * 写入历史数据开关，其实就是想fff5历史数据中写入 1
	 * @param bluetoothLeService ble服务
	 */
	public void writeContinuousMonitoringHistoryReadSwitch(BluetoothLeService bluetoothLeService) {
		if(null != bluetoothLeService) {
			BluetoothGattCharacteristic bgc = mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF5) ;
			bgc.setValue(new byte[]{1}) ;
			bluetoothLeService.writeCharacteristic(bgc);
		}
	}

	/**
	 * 读取历史数据，其实就是想fff5历史数据中写入 1
	 * @param bluetoothLeService ble服务
	 */
	public void readMonitorData(BluetoothLeService bluetoothLeService) {
//		if(null != bluetoothLeService) {
//			bluetoothLeService.readCharacteristic(mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF4));
//		}
		if(null != bluetoothLeService) {
			BluetoothGattCharacteristic bgc = mBluetoothGattCharacteristicMap.get(BLE_CHARACTERISTIC_FFF5) ;
			bgc.setValue(new byte[]{1}) ;
			bluetoothLeService.writeCharacteristic(bgc);
		}
	}

}
