package factory.sales;

import java.math.BigDecimal;

import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.PointGoods;
import util.DateHelper;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.ModelFactory;

public class PointGoodsFactory extends ModelFactory<PointGoods> {
    @Override
    public PointGoods define() {
        PointGoods poingGoods = new PointGoods();
        int seq = FactoryBoy.sequence(PointGoods.class);
        poingGoods.no = "00" + seq;
        poingGoods.name = "积分商品" + seq;
        poingGoods.imagePath = "/0/0/" + seq + "/origin.jpg";
        poingGoods.effectiveAt = DateHelper.beforeDays(1);
        poingGoods.expireAt = DateHelper.afterDays(1);
        poingGoods.faceValue = BigDecimal.TEN;
        poingGoods.pointPrice = 100l;
        poingGoods.setDetails("积分商品描述" + seq);
        poingGoods.baseSale = 8l;
        poingGoods.status = GoodsStatus.ONSALE;
        poingGoods.deleted = DeletedStatus.UN_DELETED;
        poingGoods.limitNumber = 5;
        poingGoods.materialType = MaterialType.ELECTRONIC;
        
        return poingGoods;
    }

}
