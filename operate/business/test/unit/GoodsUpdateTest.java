package unit;

import factory.FactoryBoy;
import models.sales.Goods;
import models.sales.SecKillGoods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.db.DB;
import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-23
 * Time: 下午5:48
 * To change this template use File | Settings | File Templates.
 */
public class GoodsUpdateTest extends UnitTest {
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testUpdate() throws Exception {
        // 全部门店
        goods.isAllShop = true;
        goods.shops.clear();
        goods.save();
        assertEquals(true, goods.isAllShop);
        assertEquals(0, getTableCount("goods_shops"));
        Goods.count();
        // 单个门店
        BigDecimal faceValue = new BigDecimal("9.00");
        goods.faceValue = faceValue;
        Shop testShop = FactoryBoy.create(Shop.class, "SupplierId");
        goods.shops.add(testShop);
        Goods.update(goods.id, goods, false);
        assertEquals(1, goods.shops.size());
        assertEquals(testShop.name, goods.shops.iterator().next().name);
        assertEquals(faceValue, goods.faceValue);
        assertEquals(1, getTableCount("goods_shops"));
    }

    public int getTableCount(String tableName) throws SQLException {


        Query q = JPA.em().createNativeQuery("select count(*) from \"" + tableName + "\"");
        BigInteger count = (BigInteger)q.getSingleResult();

        return count.intValue();
    }
}
