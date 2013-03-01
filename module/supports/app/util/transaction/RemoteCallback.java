package util.transaction;

/**
 * 远程调用回调体。
 * User: tanglq
 * Date: 13-2-21
 * Time: 下午8:25
 */
public interface RemoteCallback<T> {
    /**
     * 在doCall()方法中调用远程方法，并返回一个状态，以控制RemoteRecallCheck是否在会重试。
     *
     * @return 如果认为成功或不需要重新调用，返回true，否则返回false
     */
    public T doCall();
}
