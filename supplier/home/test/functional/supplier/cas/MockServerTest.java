package functional.supplier.cas;

import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

/**
 * 这个类用于测试一些在selenium中没有覆盖的Mock方法。
 *
 * @author <a href="mailto:tangliqun@snda.com">唐力群</a>
 */
public class MockServerTest extends FunctionalTest {

    @Test
    public void testProxy() {
        String pgt = "88888888";
        Cache.set(pgt, "testuser");
        // Router.addRoute("GET", "/@cas/proxy", "supplier.cas.MockServer.proxy");
        Response response = GET("/@cas/proxy?pgt=" + pgt);
        assertContentMatch("PT-", response);
    }

    @Test
    public void testServiceValidate() {

        String ticket = "12345678";
        // Router.addRoute("GET", "/@cas/serviceValidate", "supplier.cas.MockServer.serviceValidate");
        Response response = GET("/@cas/serviceValidate?ticket=" + ticket);
        assertContentMatch(ticket, response);
    }
}
