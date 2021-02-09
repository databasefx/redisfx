package cn.navigational.redisfx.util

import cn.navigational.redisfx.model.RedisKey
import cn.navigational.redisfx.util.RedisDataType.{JSON, RedisDataType, TEXT, UN_KNOWN, XML}
import org.dom4j.DocumentHelper

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps


object RedisDataType extends Enumeration {
  type RedisDataType = Value
  val TEXT, XML, JSON, UN_KNOWN = Value

  def getDataType(str: String): RedisDataType = {
    if (str.equals(TEXT.toString)) {
      TEXT
    } else if (str.equals(XML.toString)) {
      XML
    } else if (str.equals(JSON.toString)) {
      JSON
    } else {
      UN_KNOWN
    }
  }
}

object RedisDataUtil {

  val FILE_SEPARATOR = ":"

  def getRedisValType(str: String): RedisDataType = {
    //判断是否json
    val json = _json(str)
    if (json != RedisDataType.UN_KNOWN) {
      return json
    }
    //判断是否xml
    val xml = _xml(str)
    if (xml != RedisDataType.UN_KNOWN) {
      return xml
    }
    TEXT
  }

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
   * 格式化字符串
   *
   * @param str      待格式化字符串
   * @param dataType 数据类型
   * @return
   */
  def formatVal(str: String, dataType: RedisDataType): String = {
    try {
      dataType match {
        case JSON =>
          JSONUtil.formatJsonStr(str)
        case XML =>
          DocumentHelper.parseText(str).asXML()
        case _ => str
      }
    } catch {
      case ex: Exception => str
    }
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

  /**
   * 判断给定字符串是否json数据
   *
   * @param str 待解析数据
   * @return
   */
  private def _json(str: String): RedisDataType = {
//    try {
    //      val json = JSONUtil.objToJson(str)
    //      json match {
    //        case _: JSONObject =>
    //          JSON
    //        case _: JSONArray =>
    //          JSON_ARRAY
    //        case _ =>
    //          UN_KNOWN
    //      }
    //    } catch {
    //      case ex: Exception => UN_KNOWN
    //    }
    UN_KNOWN
  }

  /**
   * 判断给定数据是否xml
   *
   * @param str 待解析数据
   * @return
   */
  private def _xml(str: String): RedisDataType = {
    try {
      DocumentHelper.parseText(str)
      XML
    } catch {
      case ex: Exception => UN_KNOWN
    }
  }
}
