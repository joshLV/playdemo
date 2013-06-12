package unit.redis;

import org.junit.Test;
import play.modules.redis.Redis;
import play.test.UnitTest;

import java.util.Map;

/**
 * 用于学习Redis用法，以及确保测试环境Redis可用。
 */
public class RedisTest extends UnitTest {

    @Test
    public void testRedisBasic() throws Exception {
        Redis.set("test.hello", "world");
        assertEquals("world", Redis.get("test.hello"));
    }

    @Test
    public void testRedisHSet() throws Exception {
        Redis.hset("test.hset", "hello", "china");
        Redis.hset("test.hset", "go", "boy");

        assertEquals("china", Redis.hget("test.hset", "hello"));
        Map<String, String> map = Redis.hgetAll("test.hset");
        assertEquals(2, map.size());
        assertEquals("boy", map.get("go"));
    }
}
