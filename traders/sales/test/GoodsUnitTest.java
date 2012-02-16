import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.sales.Goods;

import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;


public class GoodsUnitTest extends UnitTest {
	@Test
	public void add(){
		Goods goods = new Goods();
		//默认商品下架状态
		goods.status="1";
		goods.companyId ="1";
		goods.prompt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		goods.details="aaaaaaaaaasssssssssssssssssss";
		goods.imagePath ="/1/1/1/"+"111.jpg";
		goods.deleted="0";
		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		String datestr = sdf.format( new  Date());  
		goods.createdAt =datestr;
		goods.createdBy ="yanjy";
		goods.save();		
		
		List<Goods> list = Goods.findAll(); 

		Assert.assertNotNull(list);  
		Assert.assertEquals(Goods.count(),list.size());  
	}
}
