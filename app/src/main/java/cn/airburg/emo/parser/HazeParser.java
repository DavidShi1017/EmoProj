package cn.airburg.emo.parser;

/**
 * 雾霾数据解析器
 * Created by YangJie on 2015/2/4.
 */
public interface HazeParser {
	/**
	 * 雾霾矫正系数
	 */
	public static final float HAZE_CORRECT_FACTOR = 1.265f ;
    /**
     * 解析雾霾数据
     * @param deviceData 设备采集到的数据,byte数组
     * @return 解析得到的数据
     */
    public short parser(byte[] deviceData) ;
}
