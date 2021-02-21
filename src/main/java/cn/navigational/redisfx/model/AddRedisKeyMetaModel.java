package cn.navigational.redisfx.model;

import cn.navigational.redisfx.enums.RedisDataType;

/**
 * @author yangkui
 * @since 1.0
 */
public class AddRedisKeyMetaModel {
    private Long ttl = -1L;
    private String key = "";
    private Integer index = 0;
    private RedisDataType dataType = RedisDataType.STRING;

    public AddRedisKeyMetaModel() {
    }

    public AddRedisKeyMetaModel(Long ttl, String key, Integer index, RedisDataType dataType) {
        this.ttl = ttl;
        this.key = key;
        this.index = index;
        this.dataType = dataType;
    }

    public Long getTtl() {
        return ttl;
    }

    public String getKey() {
        return key;
    }

    public Integer getIndex() {
        return index;
    }

    public RedisDataType getDataType() {
        return dataType;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setDataType(RedisDataType dataType) {
        this.dataType = dataType;
    }
}
