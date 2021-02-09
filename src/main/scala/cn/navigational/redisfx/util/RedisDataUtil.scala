package cn.navigational.redisfx.util

import cn.navigational.redisfx.model.RedisKey

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

object RedisDataUtil {

  val FILE_SEPARATOR = ":"


  /**
   * 将Redis key生成树形结构
   *
   * @param array 目标数据
   * @return
   */
  def getRedisKeyTreeData(array: Array[String], di: Int): Array[RedisKey] = {
    val keys: ArrayBuffer[RedisKey] = new ArrayBuffer()
    for (elem <- array) {
      val index = elem.indexOf(FILE_SEPARATOR)
      if (index == -1) {
        keys.addOne(new RedisKey(elem, di, null))
      } else {
        val firstKey = elem.substring(0, index)
        val option = keys.find(it => it.key.equals(firstKey))
        var add = false
        val redisKey = if (option.isEmpty) {
          add = true
          new RedisKey(elem.substring(0, index), di, null)
        } else {
          option.get
        }
        mapRedisKey(elem.substring(index + 1), di, redisKey)
        if (add) {
          keys.addOne(redisKey)
        }
      }
    }
    keys.sortBy(it => it.sub.size).reverse.toArray
  }

  /**
   * 映射redis key文件结构
   *
   * @param key redis key
   */
  @tailrec
  private def mapRedisKey(key: String, di: Int, parent: RedisKey): Unit = {
    val keys = parent.sub
    val index = key.indexOf(FILE_SEPARATOR)
    if (index == -1) {
      keys.addOne(new RedisKey(key, di, parent))
      return
    }
    val str = key.substring(0, index)
    val leftStr = key.substring(index + 1)
    val option = keys.find(it => it.key.equals(str))
    val rk = if (option.isEmpty) {
      val temp = new RedisKey(str, di, parent)
      keys.addOne(temp)
      temp
    } else {
      option.get
    }
    this.mapRedisKey(leftStr, di, rk)
  }
}
