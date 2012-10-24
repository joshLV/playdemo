package unit.helper;

import helper.Domain;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

/**
 * 页面上的域名的工具类的测试.
 * <p/>
 * User: sujie
 * Date: 10/24/12
 * Time: 11:18 AM
 */
public class DomainUnitTest extends UnitTest {
    @Test
    public void testGetWWWHost() {
        String domain = Domain.getWWWHost(null);
        assertEquals("http://www." + Play.configuration.getProperty("application.baseDomain"), domain);
    }

    @Test
    public void testGetHomeHost() {
        String domain = Domain.getHomeHost(null);
        assertEquals("http://home." + Play.configuration.getProperty("application.baseDomain"), domain);
    }
}
