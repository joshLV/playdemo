package models.taobao;

import com.taobao.api.internal.stream.message.TopCometMessageListener;
import play.Logger;

/**
 * @author : likang
 * Date: 12-5-3
 */
public class TaobaoCometMessageListenerImpl implements TopCometMessageListener {
    @Override
    public void onConnectMsg(String s) {
        Logger.info("taobao comet message on connect msg: \n" + s);
    }

    @Override
    public void onHeartBeat() {
        Logger.debug("taobao comet message listener, on heart beat");
    }

    @Override
    public void onReceiveMsg(String s) {
        Logger.info("taobao comet message on receive msg: \n" + s);
        TaobaoCometUtil.send(s);
    }

    @Override
    public void onConnectReachMaxTime() {
        Logger.error("taobao comet message listener, on connect reach max time");
    }

    @Override
    public void onDiscardMsg(String s) {
        Logger.info("taobao comet message listener, on discard msg");
    }

    @Override
    public void onServerUpgrade(String s) {
        Logger.info("taobao comet message listener, on server upgrade");
    }

    @Override
    public void onServerRehash() {
        Logger.info("taobao comet message listener, on server rehash");
    }

    @Override
    public void onServerKickOff() {
        Logger.error("taobao comet message listener, on server kick off");
    }

    @Override
    public void onClientKickOff() {
        Logger.error("taobao comet message listener, on client kick off");
    }

    @Override
    public void onOtherMsg(String s) {
        Logger.info("taobao comet message listener, on other msg:\n" + s);
    }

    @Override
    public void onException(Exception e) {
        Logger.error(e, "taobao comet message listener, on exception");
    }
}
