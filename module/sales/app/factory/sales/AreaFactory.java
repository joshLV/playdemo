package factory.sales;

import factory.FactoryBoy;
import models.sales.Area;
import factory.ModelFactory;
import models.sales.AreaType;
import models.sales.Goods;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;

import static util.DateHelper.afterDays;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-23
 * Time: 上午11:52
 * To change this template use File | Settings | File Templates.
 */
public class AreaFactory extends ModelFactory<Area> {
    @Override
    public Area define() {
        Area area = new Area();
        area.name = "浦东新区";
        area.displayOrder=100;
        area.areaType = AreaType.DISTRICT;
        return area;
    }


}
