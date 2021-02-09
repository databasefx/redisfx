package cn.navigational.redisfx.util

import com.fasterxml.jackson.databind.ObjectMapper

/**
 *
 * JSON数据处理工具类
 *
 * @author yangkui
 */
object JSONUtil {
  private val objectMapper: ObjectMapper = new ObjectMapper()

  /**
   * 将java对象转换为json字符串
   *
   * @param obj java对象
   * @return 返回序列化后的字符串
   */
  def objToJson(obj: Any): String = {
    objectMapper.writeValueAsString(obj)
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
    objectMapper.readValue(json, t)
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
    val typeFactory = objectMapper.getTypeFactory
    objectMapper.readValue(json, typeFactory.constructCollectionType(classOf[java.util.List[T]], t))
  }

  /**
   * 格式化json字符串
   *
   * @param str 待格式化字符串
   * @return 格式化返回字符串
   */
  def formatJsonStr(str: String): String = {
    val node = objectMapper.readTree(str)
    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
  }
}
