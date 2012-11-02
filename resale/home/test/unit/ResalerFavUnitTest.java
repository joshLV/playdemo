package unit;

import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
public class ResalerFavUnitTest extends UnitTest {

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(Shop.class);
		Fixtures.delete(Goods.class);
		Fixtures.delete(Category.class);
		Fixtures.delete(Brand.class);
		Fixtures.delete(Area.class);
		Fixtures.delete(Resaler.class);
		Fixtures.delete(ResalerFav.class);
		Fixtures.loadModels("fixture/areas_unit.yml");
		Fixtures.loadModels("fixture/categories_unit.yml");
		Fixtures.loadModels("fixture/brands_unit.yml");
		Fixtures.loadModels("fixture/shops_unit.yml");
		Fixtures.loadModels("fixture/goods_unit.yml");
		Fixtures.loadModels("fixture/level_price.yml");
		Fixtures.loadModels("fixture/resaler.yml");
		Fixtures.loadModels("fixture/resaler-fav.yml");
	}

	@Test
	public void testResalerFav() {
		List<ResalerFav> favList = ResalerFav.findAll();
		int favListSize= favList.size();
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);

		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_005");
		Goods goods =Goods.findById(goodsId);

		new ResalerFav(resaler,goods).save();
		List<ResalerFav> newfavList = ResalerFav.findAll();
		int newfavListSize= newfavList.size();

		assertEquals(favListSize+1, newfavListSize);
	}

	@Test
	public void testFindAll() {


		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);
		List<ResalerFav> favList = ResalerFav.findAll(resaler);

		assertEquals(4, favList.size());
	}

	@Test
	public void testfindFavs() {
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);

		Date createdAtBegin= null;
		Date createdAtEnd=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			createdAtBegin = sdf.parse("2012-03-01");
			createdAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String goodsName="券";

		List<ResalerFav> favList = ResalerFav.findFavs(resaler,createdAtBegin,createdAtEnd,goodsName);

		assertEquals(4, favList.size());
	}

	@Test
	public void testDel() {
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
		List<Long> goodsIds = new ArrayList();
		goodsIds.add(goodsId);

		int delfav = ResalerFav.delete(resaler,goodsIds);
		assertEquals(1, delfav);
	}


	@Test
	public void testCheckGoods() {
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
		Long[] goodsIds = new Long[1];
		goodsIds[0]=goodsId;
		Map<String, String> map = ResalerFav.checkGoods(resaler,goodsIds);

		assertEquals("1", map.get("isExist"));
		assertEquals(goodsId.toString(), map.get("goodsId"));
	}
}
