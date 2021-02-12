package cn.navigational.redisfx.enums;

/**
 * Redis数据视图格式
 *
 * @author yangkui
 * @since 1.0
 */
public enum RedisDataViewFormat {
    /**
     * 二进制文本
     */
    HEX("二进制", false),
    /**
     * xml
     */
    XML("xml", true),

    /**
     * json
     */
    JSON("json", true),

    /**
     * 普通文本
     */
    PLAINT_TEXT("文本", true);

    private final String name;
    private final boolean edit;

    RedisDataViewFormat(String name, boolean edit) {
        this.name = name;
        this.edit = edit;
    }

    public boolean isEdit() {
        return edit;
    }

    public String getName() {
        return name;
    }

    public static RedisDataViewFormat getViewFormat(String name) {
        RedisDataViewFormat format = null;
        for (RedisDataViewFormat value : RedisDataViewFormat.values()) {
            if (value.name.equals(name)) {
                format = value;
                break;
            }
        }
        if (format == null) {
            throw new RuntimeException("未知:" + name + "视图格式!");
        }
        return format;
    }
}
