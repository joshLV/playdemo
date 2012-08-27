package unit.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import cache.CacheCallBack;
import cache.CacheHelper;

public class MultiGetCacheHelperTest extends UnitTest {

    @After
    public void tearDown() throws Exception {
        CacheHelper.cleanPreRead();
    }

    @Test
    public void testPreRead() {
        CacheHelper.setCache("key1", "test1", "1800s");
        CacheHelper.setCache("key2", "test2", "1800s");
        CacheHelper.setCache("key3", "test3", "1800s");

        CacheHelper.preRead("key1", "key2", "key3");

        // 因为已经缓存，所以不会得到loadData中的值
        assertEquals("test1", CacheHelper.getCache("key1", new CacheCallBack<String>() {
            @Override
            public String loadData() {
                return "change Test1";
            }
        }));
        assertEquals("test2", CacheHelper.getCache("key2", new CacheCallBack<String>() {
            @Override
            public String loadData() {
                return "change Test2";
            }
        }));

        // 修改过cache值后，因为已经有一级缓存，所以值还是不会变
        assertEquals("test2", CacheHelper.getCache("key2", new CacheCallBack<String>() {
            @Override
            public String loadData() {
                return "not test2";
            }
        }));
    }

}
