package functional;

import java.util.HashMap;
import java.util.Map;

import models.sms.VxSms;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import factory.FactoryBoy;

public class VxSmsesTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testSendEmptySMS() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("mobiles", "13811881188,15802831123");
        params.put("content", "");
        Response response = POST("/vxsms", params);
        assertIsOk(response);
        assertContentMatch("^1\\|", response);
        
        assertEquals(0l, VxSms.count());
    }
    
    @Test
    public void testPostCreateSingleSMS() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("mobiles", "13811881188");
        params.put("content", "hello, world!");
        Response response = POST("/vxsms", params);
        assertIsOk(response);
        assertContentMatch("^0\\|", response);
        
        assertEquals(1l, VxSms.count());
    }
    

    @Test
    public void testPostCreateMultiSMS() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("mobiles", "13811881188,15802831123");
        params.put("content", "hello, world!");
        Response response = POST("/vxsms", params);
        assertIsOk(response);
        assertContentMatch("^0\\|", response);
        
        assertEquals(2l, VxSms.count());
    }
}
