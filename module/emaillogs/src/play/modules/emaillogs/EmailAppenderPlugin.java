package play.modules.emaillogs;

import play.Logger;
import play.Play;
import play.PlayPlugin;

/**
 * @author likang
 * Date: 12-7-27
 */

public class EmailAppenderPlugin extends PlayPlugin{
    @Override
    public void onLoad(){
        Logger.info("loading EmailAppenderPlugin");
        if(Play.runingInTestMode() || !Play.mode.isProd()){
            Logger.info("remove log4j appender: EMAIL");
            org.apache.log4j.Logger.getRootLogger().removeAppender("EMAIL");
        }
    }
}

