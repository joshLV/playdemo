package unit;

import java.util.List;

import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.MaterialType;

import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

/**
 * 商品Model的单元测试.
 * <p/>
 * User: yanjy
 * Date: 3/26/12
 * Time: 5:59 PM
 */
public class GoodsUnitTest extends UnitTest {
	Resaler resaler;
	
    @Before
    public void setup() {
    	FactoryBoy.deleteAll();
    	resaler = FactoryBoy.create(Resaler.class);
    }

    /**
     * 测试各种查询条件都指定的情况.
     */
    @Test
    public void testFindByResaleCondition() {
    	FactoryBoy.batchCreate(14, Goods.class, new SequenceCallback<Goods>() {
			@Override
			public void sequence(Goods g, int seq) {
				g.materialType = MaterialType.ELECTRONIC;
			}
		});
    	System.out.println("goods.count=" + Goods.count());
    	List<Goods> all = Goods.all().fetch();
    	Goods g1 = all.get(3);
    	System.out.println("status=" + g1.status + ", deleted=" + g1.deleted + ", isLottory=" + g1.isLottery + ", type=" + g1.materialType);
    	
        String condition = "0-0-0-0-1-0-0";
        GoodsCondition goodsCond = new GoodsCondition(true,condition);
        System.out.println(goodsCond.materialType);
        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByResaleCondition
                (resaler,goodsCond, 1, 50);
        assertEquals(14, goodsPage.size());
    }
    
    
    @Test
    public void testSavePromptAndSafeDetails() {
    	Goods g = FactoryBoy.create(Goods.class);
        g.title = "更加婀娜。 </span> </p> ";
    	g.setPrompt("更加婀娜。 </span> </p> ");
    	assertEquals("更加婀娜。</span> </p> ", g.title.replaceAll(" ", ""));
    }
}
