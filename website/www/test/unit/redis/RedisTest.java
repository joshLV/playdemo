package unit.redis;

import org.junit.Test;
import play.modules.redis.Redis;
import play.test.UnitTest;

import java.util.Map;

/**
 * User: tanglq
 * Date: 13-6-9
 * Time: 下午3:12
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
