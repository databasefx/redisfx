package cn.navigational.redisfx.model

import cn.navigational.redisfx.util.RedisDataUtil

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.BufferHasAsJava

/**
 *
 *
 * Redis key
 *
 * @param key redis key值
 */
class RedisKey(val key: String, val index: Int, val parent: RedisKey) {
  val sub: ArrayBuffer[RedisKey] = ArrayBuffer()

  /**
   * 判断当前节点是否叶子节点
   *
   * @return
   */
  def isLeaf: Boolean = {
    this.sub.isEmpty
  }

  def rowKey: String = {
    val arr = new ArrayBuffer[String]()
    arr.addOne(key)
    var temp = parent
    while (temp != null) {
      arr.addOne(temp.key)
      temp = temp.parent
    }
    String.join(RedisDataUtil.FILE_SEPARATOR, arr.reverse.asJava)
  }
}
