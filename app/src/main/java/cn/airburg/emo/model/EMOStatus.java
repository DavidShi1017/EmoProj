package cn.airburg.emo.model;

/**
 * EMO软件工作状态
 * Created by YOJEA on 2015/4/18.
 */
public enum EMOStatus {
    /**
     * 初始化状态
     */
     STATUS_INIT(0x0001, "STATUS_INIT(初始化状态)") ,

    /**
     * 搜索 emo硬件设备 状态
     */
    STATUS_SCAN_EMO_DEVICE(0x0002, "STATUS_SCAN_EMO_DEVICE(搜索emo硬件设备)") ,

    /**
     * 连接emo硬件设备 状态
     */
    STATUS_CONNECT_EMO_DEVICE(0x0003, "STATUS_CONNECT_EMO_DEVICE(连接emo硬件设备)"),

    /**
     * 连接emo硬件设备初始化成功 状态
     */
    STATUS_CONNECT_EMO_DEVICE_INITIATED_SUCCESS(0x004, "STATUS_CONNECT_EMO_DEVICE_INITIATED_SUCCESS(连接emo硬件设备初始化成功)"),

    /**
     * 连接emo硬件设备初始化失败 状态
     */
    STATUS_CONNECT_EMO_DEVICE_INITIATED_FAILURE(0x005, "STATUS_CONNECT_EMO_DEVICE_INITIATED_FAILURE(连接emo硬件设备初始化失败)"),

    /**
     * 已经连接emo设备 状态
     */
    STATUS_CONNECTED_EMO_DEVICE(0x006, "STATUS_CONNECTED_EMO_DEVICE(已经连接emo设备)"),

    /**
     * 连接emo设备 超时 状态
     */
    STATUS_CONNECT_EMO_DEVICE_TIMEOUT(0x007, "STATUS_CONNECT_EMO_DEVICE_TIMEOUT(连接emo设备超时)"),

    /**
     * 断开与emo设备连接 状态
     */
    STATUS_DISCONNECTED_EMO_DEVICE(0x008, "STATUS_DISCONNECTED_EMO_DEVICE(断开与emo设备连接)") ,
    /**
     * 发现emo服务超时 状态
     */
    STATUS_EMO_SERVICES_DISCOVERED_TIMEOUT(0x009, "STATUS_EMO_SERVICES_DISCOVERED_TIMEOUT(发现emo服务超时)");

    /**
     * 索引
     */
    private int mIndex ;

    /**
     * 名称
     */
    private String mName ;

    private EMOStatus(int index, String name) {
        this.mIndex = index ;
        this.mName = name ;
    }

    /**
     * 根据索引获取当前状态名称
     */
    public static String getName(int index) {
        for (EMOStatus emoStatus : EMOStatus.values()) {
            if (emoStatus.getIndex() == index) {
                return emoStatus.getName() ;
            }
        }
        return null ;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

}
