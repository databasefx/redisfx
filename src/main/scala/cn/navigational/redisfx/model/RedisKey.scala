package cn.navigational.redisfx.model

import scala.collection.mutable.ArrayBuffer

/**
 *
 *
 * Redis key
 *
 * @param key    redis key值
 * @param index  数据库指数
 * @param uuid   缓存连接id
 *
 */
class RedisKey(val key: String, val index: Int, val uuid: String) {
  val sub: ArrayBuffer[RedisKey] = ArrayBuffer()

  /**
   * 判断当前节点是否叶子节点
   *
   * @return
   */
  def isLeaf: Boolean = {
    this.sub.isEmpty
  }
}
