package cn.airburg.emo.parser;

import cn.airburg.emo.utils.ByteUtils;
import cn.airburg.emo.utils.LogUtils;

/**
 * 设备类型0解析实现类，读取第6组数据
 * Created by YangJie on 2015/2/4.
 */
public class DeviceTypeZeroParser extends DefaultParser {
    @Override
    public short parser(byte[] deviceData) {
//	    LogUtils.d(" -> ");
        if(null == deviceData) {
            LogUtils.w("the device data is null. ");
            throw new IllegalStateException("the device data is null.") ;
        }

        byte[] pm2dot5Byte = new byte[2] ;
        System.arraycopy(deviceData, 10, pm2dot5Byte, 0, 2);

        byte temp = pm2dot5Byte[0] ;
        pm2dot5Byte[0] = pm2dot5Byte[1] ;
        pm2dot5Byte[1] = temp ;

        return ByteUtils.getShort(pm2dot5Byte, 0) ;
    }
}
