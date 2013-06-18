package util.transaction;

import play.Logger;
import play.modules.redis.Redis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 实现基于Redis的分布式锁机制.
 */
public class RedisLock {

    //加锁标志
    public static final String LOCKED = "TRUE";
    public static final long ONE_MILLI_NANOS = 1000000L;
    //默认超时时间（毫秒）
    public static final long DEFAULT_TIME_OUT = 3000;
    public static JedisPool pool;
    public static final Random r = new Random();
    //锁的超时时间（秒），过期删除
    public static final int EXPIRE = 300;

    protected static ThreadLocal<Set<String>> _lockKeys = new ThreadLocal<>();

    private String key;
    //锁状态标志
    private boolean locked = false;

    public static void addLockKey(String key) {
        Set<String> keys = _lockKeys.get();
        if (keys == null) {
            keys = new HashSet<>();
            _lockKeys.set(keys);
        }
        keys.add(key);
    }

    public RedisLock(String key) {
        this.key = key;
        RedisLock.addLockKey(key);
    }

    public boolean lock(long timeout) {
        long nano = System.nanoTime();
        timeout *= ONE_MILLI_NANOS;
        try {
            while ((System.nanoTime() - nano) < timeout) {
                if (Redis.setnx(key, LOCKED) == 1) {
                    Redis.expire(key, EXPIRE);
                    locked = true;
                    return locked;
                }
                Thread.sleep(3, r.nextInt(500));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.info("建立分布式锁失败：" + e.getMessage());
        }
        return false;
    }

    public boolean lock() {
        return lock(DEFAULT_TIME_OUT);
    }

    // 无论是否加锁成功，必须调用
    public void unlock() {
        if (locked) {
            Redis.del(new String[]{key});
        }
    }

    public static void unlockAll() {
        Set<String> keys = _lockKeys.get();
        if (keys != null) {
            for (String key : keys) {
                Redis.del(new String[]{key});
            }
        }
        _lockKeys.remove();
    }
}
