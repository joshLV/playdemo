package unit;

import factory.FactoryBoy;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.UnitTest;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * User: wangjia
 * Date: 12-10-23
 * Time: 下午5:48
 */
public class GoodsUpdateTest extends UnitTest {
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        Query q = JPA.em().createNativeQuery("delete from \"goods_shops\"");
        q.executeUpdate();
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

        // 单个门店
        BigDecimal faceValue = new BigDecimal("9.00");
        goods.faceValue = faceValue;
        Shop testShop = FactoryBoy.create(Shop.class, "SupplierId");
        goods.shops.add(testShop);
        Goods.update(goods.id, goods);
        assertEquals(1, goods.shops.size());
        assertEquals(testShop.name, goods.shops.iterator().next().name);
        assertEquals(faceValue, goods.faceValue);
        assertEquals(1, getTableCount("goods_shops"));
    }

    public int getTableCount(String tableName) throws SQLException {

        Query q = JPA.em().createNativeQuery("select count(*) from \"" + tableName + "\"");
        BigInteger count = (BigInteger) q.getSingleResult();

        return count.intValue();
    }
}
