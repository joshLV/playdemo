package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.order.ExpressCompany;
import models.order.Freight;
import models.supplier.Supplier;

import java.math.BigDecimal;

/**
 * User: wangjia
 * Date: 13-5-15
 * Time: 下午5:37
 */
public class FreightFactory extends ModelFactory<Freight> {
    @Override
    public Freight define() {
        Freight freight = new Freight();
        freight.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        freight.express = FactoryBoy.lastOrCreate(ExpressCompany.class);
        freight.province = "上海";
        freight.price = BigDecimal.ONE;
        return freight;
    }

    @Factory(name = "chongqing2")
    public void defineChongqingFreight(Freight freight) {
        freight.province = "重庆";
        freight.price = BigDecimal.valueOf(2);
    }

    @Factory(name = "suzhou3")
    public void defineSuzhouFreight(Freight freight) {
        freight.province = "苏州";
        freight.price = BigDecimal.valueOf(3);
    }

    @Factory(name = "beijing4")
    public void defineBeijingFreight(Freight freight) {
        freight.province = "北京";
        freight.price = BigDecimal.valueOf(4);
    }

    @Factory(name = "nanjing5")
    public void defineNanjingFreight(Freight freight) {
        freight.province = "南京";
        freight.price = BigDecimal.valueOf(5);
    }

    @Factory(name = "other10")
    public void defineOthergFreight(Freight freight) {
        freight.province = Freight.OTHER_PROVICE;
        freight.price = BigDecimal.valueOf(10);
    }
}
