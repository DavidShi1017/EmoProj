package cn.airburg.emo.utils;

/**
 * Java和C++ 的数值类型最低位和最高位转化
 * @author SECRET
 *
 */
public class TypePositionUtils {
	
	/**
	 * 从C/C++的字节获取Java的UnsignedShort数值
	 * @param value 
	 * @return
	 */
	public static int getUnsignedShortFromCpp(int value) {
		// return (value >> 8 | (value & 0x00FF) << 8) ;
		return (int)(moveByte(value, 0, 1) | moveByte(value, 1, 0) ) ;
	}
	
	/**
	 * 从Java的字节获取C++的UnsignedShort数值
	 * @param value 
	 * @return
	 */
	public static int getCppUnsignedShortFromJava(int value) {
		// return (value >> 8 | (value & 0x00FF) << 8) ;
		return (int) (moveByte(value, 0, 1) | moveByte(value, 1, 0)) ;
	}
	
	/**
	 * 从C/C++的字节获取Java的UnsignedShort数值
	 * @param value 
	 * @return
	 */
	public static long getUnsignedIntFromCpp(long value) {
		// return (value >> 8 | (value & 0x00FF) << 8) ;
		return (int)(moveByte(value, 0, 3) | moveByte(value, 3, 0) |  moveByte(value, 1, 2) | moveByte(value, 2, 1)) ;
	}
	
	/**
	 * 从Java的字节获取C++的UnsignedShort数值
	 * @param value 
	 * @return
	 */
	public static int getCppUnsignedIntFromJava(long value) {
		// return (value >> 8 | (value & 0x00FF) << 8) ;
		return (int)(moveByte(value, 0, 3) | moveByte(value, 3, 0) |  moveByte(value, 1, 2) | moveByte(value, 2, 1)) ;
	}
	
	/**
	 * 移动字节，将第from个字节移动到第to个字节处，from和to均大于0
	 * @param number 数值
	 * @param from	第from字节
	 * @param to 第to个字节
	 * @return	交换后的字节
	 */
	public static long  moveByte(long number, int from, int to) {
		if (from < 0 || to < 0) {
			throw new IllegalArgumentException("from or to < 0") ;
		}
		
		long tmp = number & (0xFF << (8*from)) ;
		
		return from>to ? tmp >> (8*(from-to)) : tmp << (8*(to-from)) ;
	}
	
}
