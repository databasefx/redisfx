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
    SET("set"),
    /**
     * list type
     */
    LIST("list"),
    /**
     * hash type
     */
    HASH("hash"),
    /**
     * zset type
     */
    Z_SET("zset"),
    /**
     * string type
     */
    STRING("string");

    private final String name;

    RedisDataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
}
