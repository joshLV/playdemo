package play.modules.emaillogs;

import play.Play;
import play.PlayPlugin;

/**
 * @author likang
 * Date: 12-7-27
 */

public class EmailAppenderPlugin extends PlayPlugin{
    @Override
    public void onApplicationStart(){
        if(Play.runingInTestMode() || !Play.mode.isProd()){
            org.apache.log4j.Logger.getRootLogger().removeAppender("EMAIL");
        }
    }
}

