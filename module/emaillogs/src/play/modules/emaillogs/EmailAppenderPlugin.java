package play.modules.emaillogs;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import play.Logger;
import play.Play;
import play.PlayPlugin;

/**
 * @author likang
 * Date: 12-7-27
 * 当运行在测试环境或者非生产环境时，删除邮件发送的Appender
 * 同时自定义 Appender 配置，使其允许从 Play! 的配置文件中读取配置
 */

public class EmailAppenderPlugin extends PlayPlugin{
    @Override
    public void onLoad(){
        if (!Play.mode.isProd()){
            Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("EMAIL");
            if(appender != null) {
                AsyncAppender asyncAppender = (AsyncAppender) appender;
                asyncAppender.removeAppender("MQMailLog");
                Logger.info("EmailAppenderPlugin: remove MQMailLog appender");
            }
        }
    }

    @Override
    public void onApplicationStart(){
        Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("EMAIL");
        if(appender == null) {
            Logger.info("EmailAppenderPlugin: EMAIL appender not found");
            return;
        }
        AsyncAppender asyncAppender = (AsyncAppender) appender;
        EmailLoggerAppender mqAppender = (EmailLoggerAppender)asyncAppender.getAppender("MQMailLog");
        if (mqAppender == null) {
            Logger.info("EmailAppenderPlugin: MQMailLog appender not found");
            return;
        }

        mqAppender.publishToMQ = true;
        mqAppender.activateOptions();
    }
}

