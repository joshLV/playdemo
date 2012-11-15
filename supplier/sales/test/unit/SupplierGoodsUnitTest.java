package unit;
import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.sales.Goods;
import models.sales.GoodsStatus;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.Date;


public class SupplierGoodsUnitTest extends UnitTest {
	
	@Before
	public void setUp() {
		FactoryBoy.deleteAll();
	}
	
	@Test
	public void add() {
		assertEquals(0l, Goods.count());
		Goods goods = new Goods();
		//默认商品下架状态
		goods.status = GoodsStatus.OFFSALE;
		goods.supplierId = 1l;
		goods.setPrompt("<script>jkjlkjlk</script>hello");
//		goods.details = "<a href='www.taobao.com'>detail</a>";
		goods.setDetails("<a href='www.taobao.com'>detail</a>hello detail");
		goods.imagePath = "/1/1/1/" + "111.jpg";
		goods.deleted = DeletedStatus.UN_DELETED;
		goods.createdAt = new Date();
		goods.createdBy = "yanjy";
		goods.originalPrice = new BigDecimal(100);
		goods.salePrice = new BigDecimal(0.01);
		
		
		goods.save();
		assertEquals(1, Goods.count());
		Goods result = Goods.findById(goods.id);

		assertNotNull(result);
		assertEquals(result.getDetails(),"<a>detail</a>hello detail");
		assertEquals(result.getPrompt(),"hello");
	}
}
