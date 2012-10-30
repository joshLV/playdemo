package unit;

import models.sales.Area;
import models.sales.AreaType;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import factory.FactoryBoy;

import java.util.List;

import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;


/**
 * 商圈区域单元测试.
 * <p/>
 * User: sujie
 * Date: 2/28/12
 * Time: 11:29 AM
 */
public class AreaUnitTest extends UnitTest {
    List<Area> cityAreas;
    List<Area> distrcitAreas;
    List<Area> areaAreas;
    Area area10;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        cityAreas = FactoryBoy.batchCreate(7, Area.class,
                new SequenceCallback<Area>() {
                    @Override
                    public void sequence(Area target, int seq) {
                        target.id = "CITY" + seq;
                        target.name = "City#" + seq;
                        target.displayOrder = seq;
                        target.areaType = AreaType.CITY;
                    }
                });

        distrcitAreas = FactoryBoy.batchCreate(7, Area.class,
                new SequenceCallback<Area>() {
                    @Override
                    public void sequence(Area target, int seq) {
                        target.parent = cityAreas.get(0);
                        target.id = target.parent.id + seq;
                        target.name = "District#" + seq;
                        target.displayOrder = seq;
                        target.areaType = AreaType.DISTRICT;
                    }
                });

        areaAreas = FactoryBoy.batchCreate(3, Area.class,
                new SequenceCallback<Area>() {
                    @Override
                    public void sequence(Area target, int seq) {
                        target.name = "Area#" + seq;
                        target.displayOrder = seq;
                        target.areaType = AreaType.AREA;
                        target.parent = distrcitAreas.get(0);
                        target.id = target.parent.id + seq;
                    }
                });

        area10 = FactoryBoy.create(Area.class,
                new BuildCallback<Area>() {
                    @Override
                    public void build(Area target) {
                        target.name = "area10";
                        target.displayOrder = -1;
                        target.areaType = AreaType.AREA;
                        target.parent = distrcitAreas.get(1);
                        target.id = target.parent.id + FactoryBoy.sequence(Area.class);
                    }
                });


    }

    @Test
    public void testFindTopAreas() {
        List<Area> areaList = Area.findTopAreas(2);
        assertEquals(2, areaList.size());
        assertEquals(area10.name, areaList.get(0).name);
    }

    @Test
    public void testFindTopCities() {
        List<Area> areaList = Area.findTopCities(6);
        assertEquals(6, areaList.size());
        assertEquals("上海市", areaList.get(0).name);
    }

    @Test
    public void testFindTopDistricts() {
        List<Area> areaList = Area.findTopDistricts(cityAreas.get(0).id, 6);
        assertEquals(6, areaList.size());
        assertEquals(distrcitAreas.get(0).name, areaList.get(0).name);
    }

    @Test
    public void testFindAllSubAreas() {
        List<Area> areaList = Area.findAllSubAreas(cityAreas.get(0).id);
        assertEquals(7, areaList.size());
        assertEquals(distrcitAreas.get(0).name, areaList.get(0).name);
    }

    @Test
    public void testFindTopAreasByDistrict() {
        int limit = 8;
        List<Area> areaList = Area.findTopAreas(distrcitAreas.get(0).id, limit, areaAreas.get(0).id);
        assertEquals(3, areaList.size());
        areaList = Area.findTopAreas(distrcitAreas.get(0).id, limit, area10.id);
        assertEquals(4, areaList.size());
    }


}
