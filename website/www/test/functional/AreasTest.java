package functional;

import java.util.List;

import models.sales.Area;
import models.sales.AreaType;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;

/**
 * 商圈区域控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/28/12
 * Time: 2:39 PM
 */
public class AreasTest extends FunctionalTest {
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
    public void testShowTopCities() {
        Http.Response response = GET("/areas/cities/top/6");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertContentMatch(cityAreas.get(0).name, response);
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
        assertContentMatch(areaAreas.get(0).name, response);
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
        Http.Response response = GET("/areas/areas/" + areaAreas.get(0).id);
        assertIsOk(response);
        assertContentType("application/json", response);
        assertContentMatch(areaAreas.get(0).id, response);
    }
}
