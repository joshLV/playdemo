package cache;

/**
 * cache回调接口.
 */
public interface CacheCallBack<T> {
    T loadData();
}
