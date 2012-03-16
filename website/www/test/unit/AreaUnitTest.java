package unit;

import models.sales.Area;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * 商圈区域单元测试.
 * <p/>
 * User: sujie
 * Date: 2/28/12
 * Time: 11:29 AM
 */
public class AreaUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Area.class);
        Fixtures.loadModels("fixture/areas.yml");
    }

    @Test
    public void testFindTopAreas() {
        List<Area> areaList = Area.findTopAreas(2);
        assertEquals(2, areaList.size());
        assertEquals("area1", areaList.get(0).name);
    }

    @Test
    public void testFindTopCities() {
        List<Area> areaList = Area.findTopCities(6);
        assertEquals(6, areaList.size());
        assertEquals("city1", areaList.get(0).name);
    }

    @Test
    public void testFindTopDistricts() {
        List<Area> areaList = Area.findTopDistricts("1", 6);
        assertEquals(6, areaList.size());
        assertEquals("district1", areaList.get(0).name);
    }

    @Test
    public void testFindAllSubAreas() {
        List<Area> areaList = Area.findAllSubAreas("1");
        assertEquals(7, areaList.size());
        assertEquals("district1", areaList.get(0).name);
    }

    @Test
    public void testFindTopAreasByDistrict() {
        String districtId = (String) Fixtures.idCache.get("models.sales.Area-district1");
        int limit = 8;
        String areaId = (String) Fixtures.idCache.get("models.sales.Area-area1");
        List<Area> areaList = Area.findTopAreas(districtId, limit, areaId);
        assertEquals(3, areaList.size());

        areaId = (String) Fixtures.idCache.get("models.sales.Area-area10");
        areaList = Area.findTopAreas(districtId, limit, areaId);
        assertEquals(4, areaList.size());
    }
}
