package cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;

/**
 * 缓存使用的包装类.
 * 功能描述：缓存操作,可以预读部分key到本地，增加读取性能<br>
 * 支持多级key，可以批量失效同一个baseKey的一批缓存<br>
 * 例如:setCache(new String[]{"user_"},"1",User对象)，实际存储为:<br>
 * "user_" = "xxxxxx"，User对象存放在:<br>
 * ${"user_"}_1 = User对象，即User对象的cacheKey为xxxxxx_1<br>
 * 多个User对象存为xxxxxx_2,xxxxxx_3,xxxxxx_4<br>
 * 删除单个对象delete(new String[]{"user_"},"1")<br>
 * 删除所有用户对象delete("user_")<br>
 * "user_"删掉以后，${"user_"}_1会变成yyyyy_1,因为${"user_"}的可以找不到后系统会随机生成另一值<br>
 * 这样${"user_"}_1会变成yyyyy_1就相当于删除了以前所有xxxxxx_x的对象，因为xxxxxx是随机生成，在也不会获得到<br>
 * 
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class CacheHelper {
    private static final String defaultExpireSeconds = "24h"; // 默认超时时间
    private static final String defaultBaseKeyExpireSeconds = "240h"; // 默认BaseKey超时时间

    private static final ThreadLocal<HashMap<String, Object>> preReadCacheMap = new ThreadLocal<HashMap<String, Object>>();
    private static final ThreadLocal<Boolean> preReadCache = new ThreadLocal<Boolean>();

    /**
     * 根据二级key，生成最终存储的key
     * @param baseKey
     * @param subKey
     * @return
     */
    public static String getCacheKey(String baseKey, String subKey) {
        return getCacheKey(new String[]{baseKey}, subKey, defaultBaseKeyExpireSeconds);
    }

    /**
     * 根据二级key，生成最终存储的key
     * @param baseKeys
     * @param subKey
     * @return
     */
    public static String getCacheKey(String[] baseKeys, String subKey) {
        return getCacheKey(baseKeys, subKey, defaultBaseKeyExpireSeconds);
    }

    /**
     * 根据二级key，生成最终存储的key
     * @param baseKeys
     * @param subKey
     * @param expireSeconds
     * @return
     */
    public static String getCacheKey(String[] baseKeys, String subKey, String expireSeconds) {
        if (baseKeys == null || baseKeys.length == 0) {
            throw new IllegalArgumentException("baseKey数组不能为空");
        }
        if (StringUtils.isBlank(subKey)) {
            throw new IllegalArgumentException("subKey不能为空");
        }
        // 拼接baseKey
        StringBuilder fullBaseKey = new StringBuilder();
        for (String baseKey : baseKeys) {
            if (StringUtils.isBlank(baseKey)) {
                baseKey = "NULL";  //baseKey为空，则设置为NULL值，这样所有空值都可以得到一个randomKey.
            }
            final String _baseKey = baseKey;
            String cachedBaseKey = getCache(baseKey, expireSeconds, new CacheCallBack<String>() {
                public String loadData() {
                    String randomKey = getRandomKey(_baseKey);
                    Logger.debug("重新生成Key:" + randomKey);
                    return randomKey;
                }
            });
            fullBaseKey.append(cachedBaseKey).append("_");
        }
        Logger.debug("返回CacheKey:" + fullBaseKey + subKey);
        return fullBaseKey + subKey;
    }

    public static void setCache(String key, Object value) {
        setCache(key, value, defaultExpireSeconds);
    }

    public static boolean exists(String key) {
        return getCache(key) != null;
    }

    public static void setCache(String key, Object value, String expireSeconds) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key不能为空");
        }
        try {
            Cache.set(key, value, expireSeconds);
            // 加到预读Map中，同一线程下次读取时会成为本地内存读取，速度更快
            addToPreReadMap(key, value);
        } catch (Exception e) {
            Logger.warn("When set cache[key:" + key + "] found exception.", e);
            delete(key);
        }
    }

    public static void setCache(String[] baseKeys, String subKey, Object value) {
        setCache(getCacheKey(baseKeys, subKey), value);
    }

    public static void setCache(String[] baseKeys, String subKey, Object value, String expireSeconds) {
        setCache(getCacheKey(baseKeys, subKey, expireSeconds), value, expireSeconds);
    }

    public static <T> T getCache(String key) {
        return getCache(key, null, defaultExpireSeconds, null);
    }

    public static <T> T getCache(String key, Class<T> targetClass) {
        return getCache(key, targetClass, defaultExpireSeconds, null);
    }

    public static <T> T getCache(String key, CacheCallBack<T> callback) {
        return getCache(key, null, defaultExpireSeconds, callback);
    }

    public static <T> T getCache(String key, Class<T> targetClass, CacheCallBack<T> callback) {
        return getCache(key, targetClass, defaultExpireSeconds, callback);
    }

    public static <T> T getCache(String key, String expireSeconds, CacheCallBack<T> callback) {
        return getCache(key, null, expireSeconds, callback);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCache(String key, Class<T> targetClass, String expireSeconds, CacheCallBack<T> callback) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key不能为空");
        }
        // 尝试使用mget得到的值.
        Object preReadObject = getFromPreReadMap(key);
        if (preReadObject != null) {
            if (targetClass != null) {
                try {
                    // 检测类型是否正确
                    targetClass.cast(preReadObject);
                    Logger.debug("预读Key:%s成功，返回值%s", key, preReadObject);
                    return (T) preReadObject;
                } catch (ClassCastException e) {
                    Logger.warn("Get Object[" + key + "] from preReadMap Error. can't cast to " + targetClass.getName()
                            + " from " + preReadObject.getClass().getName(), e);
                    // 防止下次出错，删除后，重新加载进缓存
                    deleteToPreReadMap(key);
                }
            } else {
                return (T) preReadObject;
            }
        }

        try {
            Object cacheObject = Cache.get(key);
            if (cacheObject == null) {
                if (callback != null) {
                    // TODO: 使用类或实例级别锁粒度太粗，在并发过1k后，容易因为别的key的缓存操作而阻塞.
                    synchronized (CacheHelper.class) {
                        Object dataObject = Cache.get(key);
                        if (dataObject == null) {
                            Logger.debug(" 调用loadData(), key:%s", key);
                            dataObject = callback.loadData();
                        }
                        if (dataObject != null) {
                            if (targetClass != null) {
                                try {
                                    // 检测类型是否正确
                                    targetClass.cast(dataObject);
                                    Cache.set(key, dataObject, expireSeconds);
                                    // 加到预读Map中，同一线程下次读取时会成为本地内存读取，速度更快
                                    addToPreReadMap(key, dataObject);
                                    return (T) dataObject;
                                } catch (ClassCastException e) {
                                    Logger.warn("Get Object[" + key + "] from callback Error. can't cast to "
                                            + targetClass.getName() + " from " + dataObject.getClass().getName(), e);
                                }
                            } else {
                                Cache.set(key, dataObject, expireSeconds);
                                // 加到预读Map中，同一线程下次读取时会成为本地内存读取，速度更快
                                addToPreReadMap(key, dataObject);
                                return (T) dataObject;
                            }
                        }
                    }
                }
            } else {
                if (targetClass != null) {
                    try {
                        // 检测类型是否正确
                        targetClass.cast(cacheObject);
                        // 加到预读Map中，同一线程下次读取时会成为本地内存读取，速度更快
                        addToPreReadMap(key, cacheObject);
                        return (T) cacheObject;
                    } catch (ClassCastException e) {
                        Logger.warn("Get Object[" + key + "] from callback Error. can't cast to " + targetClass.getName()
                                + " from " + cacheObject.getClass().getName(), e);
                        // 防止下次出错，删除后，重新加载进缓存
                        delete(key);
                    }
                } else {
                    // 加到预读Map中，同一线程下次读取时会成为本地内存读取，速度更快
                    addToPreReadMap(key, cacheObject);
                    return (T) cacheObject;
                }
            }
        } catch (Exception e) {
            Logger.warn("When get cache[key:" + key + "] found exception:"+e.getMessage(), e);
            delete(key);
        }
        return null;
    }

    public static <T> T getCache(String[] baseKeys, String subKey) {
        return getCache(getCacheKey(baseKeys, subKey), null, defaultExpireSeconds, new CacheCallBack<T>() {
            public T loadData() {
                return null;
            }

        });
    }

    public static <T> T getCache(String[] baseKeys, String subKey, Class<T> targetClass) {
        return getCache(getCacheKey(baseKeys, subKey), targetClass, defaultExpireSeconds, new CacheCallBack<T>() {
            @Override
            public T loadData() {
                return null;
            }

        });
    }

    public static <T> T getCache(String[] baseKeys, String subKey, CacheCallBack<T> callback) {
        return getCache(getCacheKey(baseKeys, subKey), null, defaultExpireSeconds, callback);
    }

    public static <T> T getCache(String[] baseKeys, String subKey, Class<T> targetClass, CacheCallBack<T> callback) {
        return getCache(getCacheKey(baseKeys, subKey), targetClass, defaultExpireSeconds, callback);
    }

    public static <T> T getCache(String[] baseKeys, String subKey, String expireSeconds, CacheCallBack<T> callback) {
        return getCache(getCacheKey(baseKeys, subKey), null, expireSeconds, callback);
    }

    public static <T> T getCache(String[] baseKeys, String subKey, Class<T> targetClass, String expireSeconds,
            CacheCallBack<T> callback) {
        return getCache(getCacheKey(baseKeys, subKey), targetClass, expireSeconds, callback);
    }

    public static void delete(String key) {
        Logger.debug("删除Key:" + key);
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key不能为空");
        }
        deleteToPreReadMap(key);
        try {
            Cache.delete(key);
        } catch (Exception e1) {
            Logger.warn("When delete cache[key:" + key + "] found exception.", e1);
        }
    }

    public static void delete(String[] baseKeys, String subKey) {
        delete(getCacheKey(baseKeys, subKey));
    }

    public static boolean isPreReadCache() {
        // return Boolean.TRUE.equals(preReadCache.get());
        return true;
    }

    public static void setPreReadCache(boolean preReadCache) {
        if (preReadCache) {
            CacheHelper.preReadCache.set(preReadCache);
        } else {
            CacheHelper.preReadCache.remove();
        }
    }

    /**
     * 使用mget预先读取指定的Key值，提升缓存性能.
     *
     * @param keys
     */
    public static void preRead(List<String> keys) {
        preRead(keys.toArray(new String[]{}));
    }

    public static void preRead(String... keys) {
        if (isPreReadCache()) {
            try {
                Map<String, Object> maps = Cache.get(keys);
                if (preReadCacheMap.get() == null) {
                    preReadCacheMap.set(new HashMap<String, Object>());
                }
                preReadCacheMap.get().putAll(maps);
            } catch (Exception e) {
                Logger.warn("When get cache[key:" + StringUtils.join(keys, ",") + "] found exception.", e);
            }
        }        
    }

    /**
     * 清除预读Map。
     * 用于保证线程池的安全。
     */
    public static void cleanPreRead() {
        if (preReadCacheMap.get() != null) {
            preReadCacheMap.get().clear();
        }
        setPreReadCache(false);
    }

    /**
     * 从预读列表中查询key对应的值.
     *
     * @param key
     * @return
     */
    protected static Object getFromPreReadMap(String key) {
        if (isPreReadCache() && preReadCacheMap.get() != null) {
            return preReadCacheMap.get().get(key);
        }
        return null;
    }

    /**
     * 把值加到预读Map中.
     *
     * @param key
     * @param value
     */
    protected static void addToPreReadMap(String key, Object value) {
        if (isPreReadCache()) {
            if (preReadCacheMap.get() == null) {
                preReadCacheMap.set(new HashMap<String, Object>());
            }
            preReadCacheMap.get().put(key, value);
        }
    }

    /**
     * 从预读Map中删除值.
     *
     * @param key
     * @param value
     */
    protected static void deleteToPreReadMap(String key) {
        if (isPreReadCache()) {
            if (preReadCacheMap.get() == null) {
                preReadCacheMap.set(new HashMap<String, Object>());
            }
            preReadCacheMap.get().remove(key);
        }
    }

    private static String getRandomKey(String originalKey) {
        return originalKey + UUID.randomUUID().toString();
    }

}
