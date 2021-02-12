package cn.navigational.redisfx.util

import org.dom4j.DocumentHelper
import org.dom4j.io.{OutputFormat, XMLWriter}

import java.io.StringWriter


object XMLUtil {
  /**
   * 判断给定的字符串是否xml格式
   *
   * @param str 目标字符串
   * @return 如果是xml格式true,否则false
   */
  def validXML(str: String): Boolean = {
    var valid = false
    try {
      DocumentHelper.parseText(str)
      valid = true
    } catch {
      case ex: Exception => ex.printStackTrace()
    }
    valid
  }

  /**
   * 格式化xml字符串
   *
   * @param str 待格式化字符串
   * @return
   */
  def formatXML(str: String): String = {
    val sw = new StringWriter()
    val doc = DocumentHelper.parseText(str)
    val format = OutputFormat.createPrettyPrint()
    format.setEncoding(doc.getXMLEncoding)
    val writer = new XMLWriter(sw, format)
    try
      writer.write(doc)
    finally
      if (writer != null) writer.close()
    sw.toString
  }
}
