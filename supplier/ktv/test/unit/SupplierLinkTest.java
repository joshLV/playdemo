package unit;

import helper.SupplierLink;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

/**
 * User: yan
 * Date: 13-4-11
 * Time: 下午6:17
 */
public class SupplierLinkTest extends UnitTest {
    @Test
    public void test_getLink() {
        assertEquals("http://localhost.uhuila.net/users", SupplierLink.getHomeLink("/users"));
        assertEquals("/ktv", SupplierLink.getKtvLink("/ktv"));
    }

}
