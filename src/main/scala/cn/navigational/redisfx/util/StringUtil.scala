package cn.navigational.redisfx.util

object StringUtil {
  /**
   * 将字符串转换为二进制文本
   *
   * @param str 待转换文本
   * @return
   */
  def toBinary(str: String): String = {
    val strChar = str.toCharArray
    val sb = new StringBuffer()
    for (i <- 0 until strChar.length) {
      sb.append(Integer.toBinaryString(strChar(i)) + " ")
    }
    sb.toString
  }

  def isEmpty(str: String): Boolean = {
    str == null || str.trim == ""
  }

  def isNotEmpty(str: String): Boolean = {
    !this.isEmpty(str)
  }
}
