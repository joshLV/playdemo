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
    public void setup() {
        Fixtures.delete(Address.class);
        Fixtures.loadModels("addresses.yml");
    }

    @Ignore
    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<String, String>();
//        params.put("selectedAddressId", "0");
        params.put("address.name", "testName");
        params.put("address.address", "testAddress");
        Http.Response response = POST("/orders/addresses", params);
//        assertContentType("application/json", response);


//        assertThat(renderArgs("name"), is(notNullValue()));
//        name = (String) renderArgs("testName");
//        id = (Long) renderArgs("id");
//        assertThat(name, is("testName"));
    }
/*

    @Test
    public void testUpdate() {
        Http.Response response = DELETE("/orders/addresses/1");
        assertIsOk(response);
        assertContentType("application/json", response);
    }
*/

    @Test
    public void testDelete() {
        Long addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test1");
        Http.Response response = DELETE("/orders/addresses/" + addressId);
        assertIsOk(response);
    }
}
