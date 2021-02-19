package cn.navigational.redisfx.enums;

/**
 * 枚举redis数据类型
 *
 * @author yangkui
 */
public enum RedisDataType {
    /**
     * set type
     */
    SET(false, "set", true, true),
    /**
     * list type
     */
    LIST(false, "list", true, true),
    /**
     * hash type
     */
    HASH(false, "hash", true, false),
    /**
     * zset type
     */
    Z_SET(false, "zset", true, true),
    /**
     * string type
     */
    STRING(true, "string", true, false);

    private final boolean ttl;
    private final String name;
    private final boolean edit;
    private final boolean paging;

    RedisDataType(boolean ttl, String name, boolean edit, boolean paging) {
        this.ttl = ttl;
        this.name = name;
        this.edit = edit;
        this.paging = paging;
    }

    public String getName() {
        return name;
    }

    public boolean isTtl() {
        return ttl;
    }

    public boolean isEdit() {
        return edit;
    }

    public static RedisDataType getDataType(String str) {
        final RedisDataType dataType;
        if (str.equals(SET.name)) {
            dataType = SET;
        } else if (str.equals(LIST.name)) {
            dataType = LIST;
        } else if (str.equals(HASH.name)) {
            dataType = HASH;
        } else if (str.equals(Z_SET.name)) {
            dataType = Z_SET;
        } else if (str.equals(STRING.name)) {
            dataType = STRING;
        } else {
            throw new RuntimeException("未知Redis数据类型");
        }
        return dataType;
    }

    public boolean isPaging() {
        return paging;
    }
}
