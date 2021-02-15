package cn.navigational.redisfx.helper

import cn.navigational.redisfx.assets.RedisFxResource
import javafx.scene.image.Image

object AssetHelper {
  //app全局样式
  val APP_STYLE: String = RedisFxResource.load("css/AppStyle.css").toExternalForm
  //app默认图标
  val APP_ICON = new Image(ClassLoader.getSystemResourceAsStream("cn/navigational/redisfx/assets/icon/icon.png"))
  //app默认图标2x
  val APP_ICON_2X = new Image(ClassLoader.getSystemResourceAsStream("cn/navigational/redisfx/assets/icon/icon@2x.png"))

}
