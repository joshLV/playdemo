package functional;

import models.consumer.Address;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

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
        Http.Response response = GET("/areas/districts/top/1/6");
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
        Http.Response response = GET("/areas/subs/1");
        assertIsOk(response);
        assertContentType("application/json", response);
    }

    @Test
    public void testTopAreas() {
        Http.Response response = GET("/areas/subs/1/6");
        assertIsOk(response);
        assertContentType("application/json", response);
    }
    
    
    @Test
    public void testShowArea() {   	
    	String areaId = (String) Fixtures.idCache.get("models.sales.Area-city1");
        Http.Response response = GET("/areas/areas/"+areaId);
        assertIsOk(response);
        assertContentType("application/json", response);
        assertEquals("{\"id\":\"1\",\"name\":\"city1\",\"displayOrder\":100,\"areaType\":\"CITY\"}",response.out.toString());
        //output:  {"id":"1","name":"city1","displayOrder":100,"areaType":"CITY"}      
    }
}
