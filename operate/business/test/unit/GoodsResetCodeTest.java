package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.sales.Goods;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-1-23
 * Time: 下午1:58
 */
public class GoodsResetCodeTest extends UnitTest {
    Goods goods;
    Supplier supplier;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier s) {
                s.sequenceCode = "0001";
                s.code = "020001";
            }
        });
        goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testResetCode() {
        Goods testGoods = new Goods();
        Goods goods2 = new Goods();
        goods2.supplierId = goods.supplierId;
        goods2.resetCode();
        goods2.save();
        for (int i = 0; i < 1200; i++) {
            testGoods.supplierId = goods.supplierId;
            testGoods.resetCode();
            testGoods.save();
        }
        assertEquals(testGoods.code, "0200011201");
    }

    @Test
    public void testOrderBySequenceCode() {
        Goods goods2 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.sequenceCode = "99";
                g.supplierId = goods.supplierId;
            }
        });
        Goods goods3 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.sequenceCode = "100";
                g.supplierId = goods.supplierId;
            }
        });

        Goods goods4 = Goods.find("supplierId=? and sequenceCode is not null order by cast(sequenceCode as int) desc", goods.supplierId).first();
        assertEquals(goods4.sequenceCode, "100");
    }
}
