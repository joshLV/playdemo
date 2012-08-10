package unit;

import models.sales.PointGoodsOrder;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.NotEnoughInventoryException;
import models.sales.PointGoods;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-9
 * Time: 下午1:16
 * To change this template use File | Settings | File Templates.
 */
public class PointGoodsOrderUnitTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.delete(PointGoodsOrder.class);
        Fixtures.delete(PointGoods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(UserInfo.class);
        Fixtures.loadModels("Fixture/user.yml");
        Fixtures.loadModels("Fixture/pointgoods.yml");
        Fixtures.loadModels("Fixture/userInfo.yml");
        Fixtures.loadModels("Fixture/userPointsConfig.yml");
        Fixtures.loadModels("Fixture/addresses.yml");
        Fixtures.loadModels("Fixture/pointgoodsorder.yml");
    }

    @Test
    public void testOrderInit(){
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        User user = User.findById(userId);
        Long pointGoodsId = (Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        PointGoods pointGoods = PointGoods.findById(pointGoodsId);
        Long buyNumber = 2L;
        try{
        PointGoodsOrder pointGoodsOrder = new PointGoodsOrder(userId,pointGoods,buyNumber);
        assertEquals("2",pointGoodsOrder.buyNumber.toString());
        assertEquals(pointGoods.name,pointGoodsOrder.pointGoodsName);
        }
        catch (NotEnoughInventoryException e){
            System.out.println("error");
        }
    }

    @Test
    public void testSetAddress(){
        Long pointGoodsOrderId = (Long) Fixtures.idCache.get("models.PointGoodsOrder-order1");
        assertNotNull(pointGoodsOrderId);
        PointGoodsOrder pointGoodsOrder = PointGoodsOrder.findById(pointGoodsOrderId);

        Long addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test3");
        assertNotNull(addressId);
        Address address = Address.findById(addressId);
        pointGoodsOrder.setAddress(address);

        assertEquals("13764081569",pointGoodsOrder.receiverMobile);
        assertEquals("sujie1",pointGoodsOrder.receiverName);
        assertEquals("123456",pointGoodsOrder.postcode);

    }

    @Test
    public void testCheckLimitNumber(){

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        User user = User.findById(userId);

        Long pointGoodsId = (Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        PointGoods pointGoods = PointGoods.findById(pointGoodsId);

        Long pointGoodsOrderId = (Long) Fixtures.idCache.get("models.PointGoodsOrder-order1");
        assertNotNull(pointGoodsOrderId);
        PointGoodsOrder pointGoodsOrder = PointGoodsOrder.findById(pointGoodsOrderId);

        boolean b = pointGoodsOrder.checkLimitNumber(pointGoodsId,pointGoodsOrder.buyNumber);
        assertTrue(b);

    }

//    @Test
//    public void testCreateAndUpdateInventory(){
//
//    }



}
