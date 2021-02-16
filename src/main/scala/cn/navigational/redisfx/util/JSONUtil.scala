package cn.navigational.redisfx.util

import com.alibaba.fastjson.{JSON, JSONObject}

/**
 *
 * JSON数据处理工具类
 *
 * @author yangkui
 */
object JSONUtil {
  /**
   * 将java对象转换为json字符串
   *
   * @param obj java对象
   * @return 返回序列化后的字符串
   */
  def objToJsonStr(obj: Any): String = {
    JSON.toJSONString(obj)
  }

  /**
   * 将pojo转换为JSONOBject对象
   *
   * @param obj 目标对象
   * @return
   */
  def objToJsonObj(obj: Any): JSONObject = {
    val jsonStr = objToJsonStr(obj)
    JSON.parseObject(jsonStr)
  }

  /**
   * json字符串转换为java对象
   *
   * @param json json字符串
   * @param t    java对象class
   * @tparam T 泛型参数
   * @return
   */
  def strToObj[T](json: String, t: Class[T]): T = {
    JSON.parseObject(json, t)
  }

  /**
   * JSON数组反序列化为java数组
   *
   * @param json json字符串
   * @param t    java对象类型class
   * @tparam T java对象类型
   * @return
   */
  def strToArr[T](json: String, t: Class[T]): java.util.List[T] = {
    JSON.parseArray(json, t)
  }

  /**
   * 格式化json字符串
   *
   * @param str 待格式化字符串
   * @return 格式化返回字符串
   */
  def formatJsonStr(str: String): String = {
    val json = JSON.parse(str)
    if (!json.isInstanceOf[String]) {
      JSON.toJSONString(json, true)
    } else {
      json.toString
    }
  }

  /**
   * 判断一个字符串是否json数据
   *
   * @param json json字符串
   * @return 返回判断结果
   */
  def validJSON(json: String): Boolean = {
    try {
      val obj = JSON.parse(json)
      !obj.isInstanceOf[String]
    } catch {
      case ex: Exception => false
    }
  }

}
