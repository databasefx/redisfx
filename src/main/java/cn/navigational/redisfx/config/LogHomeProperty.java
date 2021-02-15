package cn.navigational.redisfx.config;

import ch.qos.logback.core.PropertyDefinerBase;
import cn.navigational.redisfx.AppPlatform;

/**
 * logback 动态配置
 *
 * @author yangkui
 * @since 1.0
 */
public class LogHomeProperty extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return AppPlatform.getApplicationDataFolder();
    }
}
