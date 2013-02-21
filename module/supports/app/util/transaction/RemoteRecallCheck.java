package util.transaction;

import cache.CacheCallBack;
import cache.CacheHelper;

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

    public <T> T call(final String callPrefix, final RemoteCallback<T> callback) {
        String cacheKey = "REMRECALL_" + callPrefix + _callId.get();
        Boolean needRecall = CacheHelper.getCache(cacheKey, "3h", new CacheCallBack<Boolean>() {
            @Override
            public Boolean loadData() {
                return Boolean.TRUE; //默认为需要重
            }
        });

        T t = null;
        if (needRecall) {

        }

        _needRecall.remove(); //清除needRecall作用域
        return t;
    }

    public static void setCallId(String callId) {
        _callId.set(callId);
    }
    public static String getCallId() {
        if (_callId.get() == null) {
            // 没有设置过callId是不被允许的
            throw new RuntimeException("请先调用RemoteRecallCheck.setCallId(callId)方法设置一个业务唯一的ID.");
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
