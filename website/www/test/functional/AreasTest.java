package functional;

import models.consumer.Address;
import models.sales.Area;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.List;

/**
 * 商圈区域控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/28/12
 * Time: 2:39 PM
 */
public class AreasTest extends FunctionalTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Address.class);
        Fixtures.loadModels("fixture/areas.yml");
    }

    @Test
    public void testShowTopCities() {
        Http.Response response = GET("/areas/cities/top/6");
        assertIsOk(response);
        assertContentType("application/json", response);
    }

    @Test
    public void testShowTopDistricts() {
        Http.Response response = GET("/areas/districts/top/6");
        assertIsOk(response);
        assertContentType("application/json", response);
    }

    @Test
    public void testShowTopAreas() {
        Http.Response response = GET("/areas/areas/top/6");
        assertIsOk(response);
        assertContentType("application/json", response);
    }

    @Test
    public void testShowAllSubAreas() {
        Http.Response response = GET("/areas/subs/021");
        assertIsOk(response);
        assertContentType("application/json", response);
    }

    @Test
    public void testTopAreas() {
        Http.Response response = GET("/areas/subs/021/6");
        assertIsOk(response);
        assertContentType("application/json", response);
    }

}
