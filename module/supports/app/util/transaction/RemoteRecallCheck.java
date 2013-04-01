package util.transaction;

import cache.CacheCallBack;
import cache.CacheHelper;
import play.Logger;

/**
 * 记录远程调用的成功状态，以防止同一条件下重试已经成功的消息。
 * User: tanglq
 * Date: 13-2-21
 * Time: 下午8:21
 */
public class RemoteRecallCheck {

    /**
     * 当前主业务的一个唯一ID。
     * 在这个唯一ID上，可以被发起多种远程调用，用这个callId加上调用类型来标识是否需要重试.
     */
    private static ThreadLocal<String> _callId = new ThreadLocal<>();

    /**
     * 当前调用是否需要重试。
     *
     * 这个线程变量只在一个远程调用RemoteCallback.doCall()作用域有效。完成调用后，call方法会把这个值写入到Cache中。
     */
    private static ThreadLocal<Boolean> _needRecallRemote = new ThreadLocal<>();

    public static <T> T call(final String callPrefix, final RemoteCallback<T> callback) {
        if (getId() == null) { //暂先不使用recall机制
            // 调用前没有设置callId，认为是不需要CallRecallCheck（兼容旧的调用者）
            return callback.doCall();
        }
        String cacheNeedRecallKey = "REMRECALL_" + callPrefix + getId();
        String cacheResultKey = "REMCALLRESULT_" + callPrefix + getId();
        Boolean needRecall = CacheHelper.getCache(cacheNeedRecallKey, "3h", new CacheCallBack<Boolean>() {
            @Override
            public Boolean loadData() {
                return Boolean.TRUE; //默认为需要重试
            }
        });

        T t;

        Logger.info("%s: needRecall:" + needRecall + ", CacheHelper.exists(cacheResultKey)=" + CacheHelper.exists
                (cacheResultKey), getId());
        if (needRecall || !CacheHelper.exists(cacheResultKey)) {
            t = callback.doCall();
            CacheHelper.setCache(cacheResultKey, t, "3h");
            Logger.info("RemoteRecallCheck.call: doCall: t=" + t);
        } else {
            t = CacheHelper.getCache(cacheResultKey);
            Logger.info("RemoteRecallCheck.call: getFromCache: t=" + t);
        }
        Logger.info("RemoteRecallCheck.call: cacheNeedRecallKey=" + cacheNeedRecallKey +
                ", value=" + needRecall + ", t=" + t);

        CacheHelper.setCache(cacheNeedRecallKey, getNeedRecallRemote(), "3h"); //记录是否要远程调用重试

        _needRecallRemote.remove(); //清除needRecall作用域
        return t;
    }

    public static void setId(String callId) {
        _callId.set(callId);
    }
    public static String getId() {
        if (_callId.get() == null) {
            // 没有设置过callId，则return null，以实现不支持RecallCheck的功能
            return null;
        }
        return _callId.get();
    }

    /**
     * 声明为调用成功，下次不再调用
     */
    public static void signAsSuccess() {
        _needRecallRemote.set(Boolean.FALSE);
    }

    public static Boolean getNeedRecallRemote() {
        if (_needRecallRemote.get() == null) {
            // 没有设置过needRecall，则默认为truee，需要重调用。
            return Boolean.TRUE;
        }
        return _needRecallRemote.get();
    }

    public static void cleanUp() {
        _callId.remove();
    }
}
