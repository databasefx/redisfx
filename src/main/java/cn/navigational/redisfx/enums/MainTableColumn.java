package cn.navigational.redisfx.enums;

/**
 * 枚举连接界面表列名称
 *
 * @author yangkui
 * @since 1.0
 */
public enum MainTableColumn {
    /**
     * 连接名称
     */
    NAME("名称", "name"),
    /**
     * 连接主机
     */
    HOST("主机", "host"),
    /**
     * 是否本地存储
     */
    LOCAL_SAVE("本地存储", "localSave"),
    /**
     * 最后使用时间
     */
    LAST_USE_TIME("最后使用时间", "lastUseDate");
    private final String name;
    private final String mapper;

    MainTableColumn(String name, String mapper) {
        this.name = name;
        this.mapper = mapper;
    }

    public String getName() {
        return name;
    }

    public String getMapper() {
        return mapper;
    }

}
