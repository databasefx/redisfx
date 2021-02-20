package cn.navigational.redisfx

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisMainViewController
import javafx.application.{Application, Platform}
import javafx.scene.text.Font
import javafx.stage.Stage
import org.slf4j.{Logger, LoggerFactory}

import java.io.IOException
import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object Launcher {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[RedisFx], args: _*)
  }
}


class RedisFx extends Application with AppPlatform.AppNotificationHandler {
  private val logger: Logger = LoggerFactory.getLogger(classOf[RedisFx])

  override def init(): Unit = {
    Future {
      Font.loadFont(RedisFxResource.load("font/font1715.ttf").openStream(), 15d)
    }
  }

  override def start(primaryStage: Stage): Unit = {
    this.setApplicationUncaughtExceptionHandler()
    try {
      if (!AppPlatform.requestStart(this, getParameters)) {
        Platform.exit()
        return
      }
      RedisMainViewController.openMainView()
    } catch {
      case x: IOException => x.printStackTrace()
    }
  }

  override def handleLaunch(files: util.List[String]): Unit = {
  }

  override def handleOpenFilesAction(files: util.List[String]): Unit = {}

  override def handleMessageBoxFailure(x: Exception): Unit = {}

  private def setApplicationUncaughtExceptionHandler(): Unit = {
    if (Thread.getDefaultUncaughtExceptionHandler == null) {
      // Register a Default Uncaught Exception Handler for the application
      Thread.setDefaultUncaughtExceptionHandler(new RedisFxUncaughtExceptionHandler())
    }
  }


  private class RedisFxUncaughtExceptionHandler extends Thread.UncaughtExceptionHandler {
    override def uncaughtException(t: Thread, e: Throwable): Unit = {
      logger.error("Progress happen a un-catch exception.", e)
    }
  }

}