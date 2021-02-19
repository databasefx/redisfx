package cn.navigational.redisfx.model;

/**
 * @author yangkui
 * @since 1.0
 */
public class RedisRichValueModel {
    private String key;
    private String value;
    private Integer index;
    private Double score;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
