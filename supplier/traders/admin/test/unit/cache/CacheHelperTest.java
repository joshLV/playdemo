package unit.cache;

import java.util.Date;
import java.util.UUID;
import junit.framework.Assert;
import org.junit.Test;
import play.test.UnitTest;
import cache.CacheCallBack;
import cache.CacheHelper;

public class CacheHelperTest extends UnitTest {
    public CacheHelperTest() {
        super();
    }

    @Test
    public void testSetCacheStringIntObject() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(key));
    }

    @Test
    public void testSetCacheStringIntClassObject() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(key, value, "2s");
        Assert.assertEquals(null, CacheHelper.getCache(key, Date.class));
    }

    @Test
    public void testSetCacheStringArrayStringIntObject() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(new String[] { key }, key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(new String[] { key }, key));
    }

    @Test
    public void testGetCacheString() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(key));
    }

    @Test
    public void testGetCacheStringIntCacheCallBackOfT() {
        String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        Assert.assertEquals(value, CacheHelper.getCache(key, "2s", new CacheCallBack<String>() {

            @Override
            public String loadData() {
                return value;
            }

        }));
    }

    @Test
    public void testGetCacheStringArrayString() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(new String[] { key }, key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(new String[] { key }, key));
    }

    @Test
    public void testGetCacheStringArrayStringIntCacheCallBackOfT() {
        String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        Assert.assertEquals(value, CacheHelper.getCache(new String[] { key }, key, "2s", new CacheCallBack<String>() {

            @Override
            public String loadData() {
                return value;
            }

        }));
    }

    @Test
    public void testDeleteString() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(key));
        CacheHelper.delete(key);
        Assert.assertNull(CacheHelper.getCache(key));
    }

    @Test
    public void testDeleteStringArrayString() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        CacheHelper.setCache(new String[] { key }, key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(new String[] { key }, key));
        CacheHelper.delete(new String[] { key }, key);
        Assert.assertNull(CacheHelper.getCache(new String[] { key }, key));
    }

    @Test
    public void testPreReadStringArray() throws InterruptedException {
        final String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        CacheHelper.setPreReadCache(true);
        CacheHelper.setCache(key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(key));
        // 在本线程预读
        CacheHelper.preRead(new String[] { key });
        // 在另外一个线程里写
        Thread s = new Thread() {
            @Override
            public void run() {
                CacheHelper.setCache(key, "2s", UUID.randomUUID().toString());
                // 确定已修改
                Assert.assertNotSame(value, CacheHelper.getCache(key));
            }
        };
        s.start();
        s.join();
        // 本线程的值不变
        Assert.assertEquals(value, CacheHelper.getCache(key));
    }

    @Test
    public void testCleanPreRead() throws InterruptedException {
        final String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();
        CacheHelper.setPreReadCache(true);
        CacheHelper.setCache(key, value, "2s");
        Assert.assertEquals(value, CacheHelper.getCache(key));
        // 在本线程预读
        CacheHelper.preRead(new String[] { key });
        // 在另外一个线程里写
        Thread s = new Thread() {
            @Override
            public void run() {
                CacheHelper.setCache(key, value2, "2s");
                // 确定已修改
                Assert.assertEquals(value2, CacheHelper.getCache(key));
            }
        };
        s.start();
        s.join();
        // 本线程的值不变
        Assert.assertEquals(value, CacheHelper.getCache(key));
        // 删除后应该变成修改后的
        CacheHelper.cleanPreRead();
        Assert.assertEquals(value2, CacheHelper.getCache(key));
    }

}
