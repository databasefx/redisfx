package cn.navigational.redisfx.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 枚举富文本表格列
 *
 * @author yangkui
 * @since 1.0
 */
public enum RichTextTableColumn {
    /**
     * All redis data type value index
     */
    INDEX("序号", "index", RedisDataType.HASH, RedisDataType.LIST, RedisDataType.SET, RedisDataType.Z_SET),
    /**
     * Redis Z_SET data type scope
     */
    SCORE("权重", "score", RedisDataType.Z_SET),
    /**
     * Hash key
     */
    HASH_KEY("键", "key", RedisDataType.HASH),
    /**
     * All Redis data type value
     */
    VALUE("值", "value", RedisDataType.Z_SET, RedisDataType.LIST, RedisDataType.HASH, RedisDataType.SET);

    private final String columnName;
    private final String mapperFiled;
    private final RedisDataType[] dataTypes;

    RichTextTableColumn(String columnName, String mapperFiled, RedisDataType... dataTypes) {
        this.columnName = columnName;
        this.dataTypes = dataTypes;
        this.mapperFiled = mapperFiled;
    }

    public String getColumnName() {
        return columnName;
    }

    public RedisDataType[] getDataTypes() {
        return dataTypes;
    }

    public String getMapperFiled() {
        return mapperFiled;
    }

    public static List<RichTextTableColumn> getTableColumn(RedisDataType dataType) {
        var list = new ArrayList<RichTextTableColumn>();
        for (RichTextTableColumn value : RichTextTableColumn.values()) {
            for (RedisDataType type : value.dataTypes) {
                if (dataType == type) {
                    list.add(value);
                    break;
                }
            }
        }
        return list;
    }
}
