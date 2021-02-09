package cn.navigational.redisfx.enums;

/**
 * 枚举Redis响应状态值
 *
 * @author yangkui
 * @since 1.0
 */
public enum RedisReplyStatus {
    /**
     * 响应成功
     */
    OK,
    /**
     * 响应错误
     */
    ERR,
    /**
     * PING 成功
     */
    PONG,
}
