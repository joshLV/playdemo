package models.taobao;

import com.taobao.api.internal.stream.connect.ConnectionLifeCycleListener;
import play.Logger;

/**
 * @author : likang
 * Date: 12-5-3
 */
public class TaobaoCometConnListenerImpl implements ConnectionLifeCycleListener {

    @Override
    public void onConnect() {
        Logger.info("taobao comet connected");
    }

    @Override
    public void onException(Throwable throwable) {
        Logger.error(throwable,"taobao comet exception");
    }

    @Override
    public void onConnectError(Exception e) {
        Logger.error(e, "taobao comet connection failed");
    }

    @Override
    public void onReadTimeout() {
        Logger.error("taobao comet read timeout");
    }

    @Override
    public void onMaxReadTimeoutException() {
        Logger.error("taobao comet reach the max read timeout limit");
    }

    @Override
    public void onSysErrorException(Exception e) {
        Logger.error(e, "taobao comet system error");
    }
}
