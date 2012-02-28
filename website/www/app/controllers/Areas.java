package controllers;

import models.sales.Area;
import play.mvc.Controller;

import java.util.List;

/**
 * 商圈区域控制器.
 * <p/>
 * User: sujie
 * Date: 2/28/12
 * Time: 2:21 PM
 */
public class Areas extends Controller {

    public static void showTopCities(int limit) {
        List<Area> cities = Area.findTopCities(limit);
        renderJSON(cities);
    }

    public static void showTopDistricts(int limit) {
        List<Area> areas = Area.findTopDistricts(limit);
        renderJSON(areas);
    }

    public static void showTopAreas(int limit) {
        List<Area> businessAreas = Area.findTopAreas(limit);
        renderJSON(businessAreas);
    }

    public static void showArea(String areaId) {
        Area area = Area.findById(areaId);
        renderJSON(area);
    }

    public static void showTopSubAreas(String id, int limit) {
        List<Area> areas = Area.findTopAreas(id, limit);
        renderJSON(areas);
    }

    public static void showAllSubAreas(String id) {
        List<Area> areas = Area.findAllSubAreas(id);
        renderJSON(areas);
    }
}