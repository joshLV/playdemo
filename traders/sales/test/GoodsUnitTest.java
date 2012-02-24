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
        goods.companyId = "1";
        goods.prompt = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        goods.details = "aaaaaaaaaasssssssssssssssssss";
        goods.imagePath = "/1/1/1/" + "111.jpg";
        goods.deleted = Goods.UNDELETED;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datestr = sdf.format(new Date());
        goods.createdAt = datestr;
        goods.createdBy = "yanjy";
        goods.originalPrice = new BigDecimal(100);
        goods.salePrice = new BigDecimal(0.01);
        goods.save();

        List<Goods> list = Goods.findAll();

        assertNotNull(list);
        assertEquals(Goods.count(), list.size());
    }
}
