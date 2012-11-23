package functional;

import helper.Domain;

import org.junit.Before;
import org.junit.Test;

import play.test.FunctionalTest;
import factory.FactoryBoy;

/**
 * 页面上的域名的工具类的测试.
 * <p/>
 * User: sujie
 * Date: 10/24/12
 * Time: 11:25 AM
 */
public class DomainFuncTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void testGetWWWHost() {
        String domain = Domain.getWWWHost(newRequest());
        assertEquals("http://localhost:9001", domain);
    }

    @Test
    public void testGetHomeHost() {
        String domain = Domain.getHomeHost(newRequest());
        assertEquals("http://localhost:9002", domain);
    }
}
    