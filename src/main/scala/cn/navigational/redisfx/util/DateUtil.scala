package cn.navigational.redisfx.util

import java.text.SimpleDateFormat
import java.util.Date

/**
 * 时间日期处理工具类
 *
 * @author yangkui
 */
object DateUtil {
  //默认时间、日期格式
  private val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

  /**
   * 将Date对象按照指定的格式转换为可读字符串
   *
   * @param format 目标格式
   * @param date   目标对象
   * @return 返回可读字符串
   */
  def formatDate(format: String = DEFAULT_DATE_FORMAT, date: Date): String = {
    val dateFormat = new SimpleDateFormat(format)
    dateFormat.format(date)
  }

  def formatNow(format: String = DEFAULT_DATE_FORMAT): String = {
    this.formatDate(format, new Date())
  }
}
