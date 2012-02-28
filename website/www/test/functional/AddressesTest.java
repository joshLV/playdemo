package functional;

import models.consumer.Address;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户收货地址控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/16/12
 * Time: 11:36 AM
 */
public class AddressesTest extends FunctionalTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Address.class);
        Fixtures.loadModels("fixture/addresses.yml");
    }

    @Test
    public void testDelete() {
        Long addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test1");
        Http.Response response = DELETE("/orders/addresses/" + addressId);
        assertIsOk(response);
    }
}
