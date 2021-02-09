package cn.navigational.redisfx.editor

import java.util.Locale

/**
 *
 * This class contains static methods that depends on the platform.
 *
 * @author yangkui
 */
object EditorPlatform {
  /**
   * 获取当前os名称
   */
  private val osName = System.getProperty("os.name").toLowerCase(Locale.ROOT)
  /**
   * True if current platform is running Linux.
   */
  val IS_LINUX: Boolean = osName.contains("linux")

  /**
   * True if current platform is running Mac OS X.
   */
  val IS_MAC: Boolean = osName.contains("mac")

  /**
   * True if current platform is running Windows.
   */
  val IS_WINDOWS: Boolean = osName.contains("windows")

  /**
   * Returns true if the jvm is running with assertions enabled.
   *
   * @return true if the jvm is running with assertions enabled.
   */
  def isAssertionEnabled: Boolean = EditorPlatform.getClass.desiredAssertionStatus
}
