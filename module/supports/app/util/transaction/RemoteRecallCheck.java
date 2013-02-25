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
    private static ThreadLocal<Boolean> _needRecall = new ThreadLocal<>();

    public static <T> T call(final String callPrefix, final RemoteCallback<T> callback) {
        if (getCallId() == null) {
            // 调用前没有设置callId，认为是不需要CallRecallCheck（兼容旧的调用者）
            return callback.doCall();
        }
        String cacheNeedRecallKey = "REMRECALL_" + callPrefix + getCallId();
        String cacheResultKey = "REMCALLRESULT_" + callPrefix + getCallId();
        Boolean needRecall = CacheHelper.getCache(cacheNeedRecallKey, "3h", new CacheCallBack<Boolean>() {
            @Override
            public Boolean loadData() {
                return Boolean.TRUE; //默认为需要重试
            }
        });

        T t;

        if (needRecall) {
            t = callback.doCall();
            CacheHelper.setCache(cacheResultKey, t, "3h");
        } else {
            t = CacheHelper.getCache(cacheResultKey);
        }
        Logger.info("RemoteRecallCheck.call: cacheNeedRecallKey=" + cacheNeedRecallKey + ", value=" + needRecall +
                ", t=" + t);

        CacheHelper.setCache(cacheNeedRecallKey, getNeedRecall(), "3h"); //记录是否要重试

        _needRecall.remove(); //清除needRecall作用域
        return t;
    }

    public static void setCallId(String callId) {
        _callId.set(callId);
    }
    public static String getCallId() {
        if (_callId.get() == null) {
            // 没有设置过callId，则return null，以实现不支持RecallCheck的功能
            return null;
        }
        return _callId.get();
    }

    public static void setNeedRecall(Boolean value) {
        _needRecall.set(value);
    }
    public static Boolean getNeedRecall() {
        if (_needRecall.get() == null) {
            // 没有设置过needRecall，则默认为truee，需要重调用。
            return Boolean.TRUE;
        }
        return _needRecall.get();
    }

    public static void cleanUp() {
        _callId.remove();
    }
}
