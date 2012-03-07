package unit;
import com.uhuila.common.constants.DeletedStatus;
import models.sales.Goods;
import models.sales.GoodsStatus;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class GoodsUnitTest extends UnitTest {
	@Test
	public void add() {
		Goods goods = new Goods();
		//默认商品下架状态
		goods.status = GoodsStatus.OFFSALE;
		goods.companyId = 1l;
		goods.prompt = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		goods.details = "aaaaaaaaaasssssssssssssssssss";
		goods.imagePath = "/1/1/1/" + "111.jpg";
		goods.deleted = DeletedStatus.UN_DELETED;
		goods.createdAt = new Date();
		goods.createdBy = "yanjy";
		goods.originalPrice = new BigDecimal(100);
		goods.salePrice = new BigDecimal(0.01);
		goods.save();
		List<Goods> list = Goods.findAll();

		assertNotNull(list);
		assertEquals(Goods.count(), list.size());
	}
}
