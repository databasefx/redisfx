package cn.navigational.redisfx.model.impl

import cn.navigational.redisfx.enums.RedisDataType
import cn.navigational.redisfx.model.RedisContent

/**
 * 字符串类型
 *
 * @param str 字符串内容
 */
class BasicRedisContent(val str: String) extends RedisContent(RedisDataType.STRING) {
}
