import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sales.Goods;

import org.junit.Assert;
import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class GoodsTest extends FunctionalTest {

	/**
	 * 添加商品
	 */
	@Test
	public void create() {
		Map<String, String> goodsParams = new HashMap<String,String>();
		goodsParams.put("goods.name", "laiyifen");
		goodsParams.put("goods.no", "10000000");
		goodsParams.put("goods.companyId", "0");
		goodsParams.put("goods.status", "0");
		goodsParams.put("goods.prompt", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		goodsParams.put("goods.details", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		goodsParams.put("goods.imagePath", "/opt/3.jpg");
		goodsParams.put("goods.deleted", "0");
		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		String datestr = sdf.format( new  Date()); 
		goodsParams.put("goods.createdAt", datestr);
		goodsParams.put("goods.createdBy", "yanjy");
		goodsParams.put("radios", "1");
		goodsParams.put("status", "1");
		goodsParams.put("checkoption", "1");
		Response response = POST("/goods", goodsParams);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(302,response);

		goodsParams.put("goods.name", "laiyifen1");
		goodsParams.put("goods.no", "20000000");
		goodsParams.put("goods.companyId", "0");
		goodsParams.put("goods.status", "1");
		goodsParams.put("goods.prompt", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		goodsParams.put("goods.details", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		goodsParams.put("goods.imagePath", "/opt/3.jpg");
		goodsParams.put("goods.deleted", "0");
		goodsParams.put("goods.createdAt", datestr);
		goodsParams.put("goods.createdBy", "yanjy");
		goodsParams.put("radios", "1");
		goodsParams.put("status", "1");
		goodsParams.put("checkoption", "1");
		response = POST("/goods", goodsParams);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(302,response);

		List<Goods> list = Goods.findAll();
		Assert.assertNotNull(list);  
		Assert.assertTrue(list.size() >=0);  

	}

	/**
	 * 修改商品上下架
	 */
	@Test
	public void updateStatus() {
		Map<String, String> goodsParams = new HashMap<String,String>();
		//把id=83的状态改为上架
		goodsParams.put("status", "1");
		goodsParams.put("checkoption[]", "1");
		Response response = POST("/updatestatus", goodsParams);
		assertStatus(302,response);
		Goods goods = Goods.findById(Long.parseLong("1"));
		//	Assert.assertEquals(goods.status,goodsParams.get("status"));  

		//把id=8的状态改为下架
		goodsParams.put("status", "0");
		goodsParams.put("checkoption[]", "2");
		response = POST("/updatestatus", goodsParams);
		assertStatus(302,response);
		goods = Goods.findById(Long.parseLong("2"));
		//	Assert.assertEquals(goods.status,"0");  

	}


	/**
	 * 修改商品信息
	 */
	@Test
	public void update() {
		Map<String, String> goodsParams = new HashMap<String,String>();
		goodsParams.put("goods.name", "test");
		goodsParams.put("goods.no", "30000");
		goodsParams.put("goods.prompt", "CCCCCCCCCCCCC");
		goodsParams.put("goods.details", "DDDDDDDDDDDDDDd");
		goodsParams.put("imagePath", "/opt/3.jpg");
		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		String datestr = sdf.format( new  Date()); 
		goodsParams.put("goods.updatedAt", datestr);
		goodsParams.put("goods.updatedBy", "yyyy");
		goodsParams.put("id", "1");
		Response response = POST("/update", goodsParams);
		assertStatus(302,response);
		Goods goods = Goods.findById(Long.parseLong("1"));
		Assert.assertEquals(goods.name,"test");  
	}


	/**
	 * 修改商品上下架
	 */
	@Test
	public void delete() {
		Map<String, String> goodsParams = new HashMap<String,String>();
		Response response = DELETE("/goods/{id}/delete?checkoption[]=8");
		assertStatus(302,response);
	}
}