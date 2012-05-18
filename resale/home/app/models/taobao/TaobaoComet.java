package models.taobao;

import com.taobao.api.internal.stream.Configuration;
import com.taobao.api.internal.stream.TopCometStream;
import com.taobao.api.internal.stream.TopCometStreamFactory;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * 与淘宝主动通知服务建立连接
 *
 * @author : likang
 * Date: 12-5-3
 *
 */
@OnApplicationStart(async = true)
@Every("24h")
public class TaobaoComet extends Job{
    private static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "12621657");
    private static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "b0d06603b45a281f783b6ccd72ad8745");
    private static final String COMET_ON = Play.configuration.getProperty("taobao.comet.on", "false");
    public void doJob(){
        if(Play.runingInTestMode() || COMET_ON.equals("false")){
            return;
        }
        //不传入具体的userId，这样所有授权此应用的账号消息都会发到此连接
        Configuration conf = new Configuration(APPKEY,APPSECRET,null);
        TopCometStream stream = new TopCometStreamFactory(conf).getInstance();
        stream.setConnectionListener(new TaobaoCometConnListenerImpl());
        stream.setMessageListener(new TaobaoCometMessageListenerImpl());
        stream.start();
    }
}
