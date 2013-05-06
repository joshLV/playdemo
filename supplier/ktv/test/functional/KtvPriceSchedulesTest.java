package functional;

import play.test.FunctionalTest;

/**
 * User: yan
 * Date: 13-4-22
 * Time: 下午1:57
 */
public class KtvPriceSchedulesTest extends FunctionalTest {
    /*
    Supplier supplier;
    SupplierUser supplierUser;
//    KtvRoomType ktvRoomType;
    Shop shop;
    SupplierProperty property;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
        property = FactoryBoy.create(SupplierProperty.class, "ktv");
        property.supplier = supplier;
        property.save();

        ktvRoomType = FactoryBoy.create(KtvRoomType.class);
        ktvRoomType.supplier = supplier;
        ktvRoomType.save();

        shop = FactoryBoy.create(Shop.class);
        shop.supplierId = supplier.id;
        shop.save();

        FactoryBoy.batchCreate(4, KtvRoom.class, new BuildCallback<KtvRoom>() {
            @Override
            public void build(KtvRoom target) {
                target.roomType = ktvRoomType;
                target.shop = shop;
            }
        });

        FactoryBoy.batchCreate(5, KtvPriceSchedule.class, new BuildCallback<KtvPriceSchedule>() {
            @Override
            public void build(KtvPriceSchedule target) {
                target.roomType = ktvRoomType;
                target.shops = new HashSet<>();
                target.shops.add(shop);
            }
        });
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("KtvPriceSchedules.index").url);
        assertIsOk(response);
        List<KtvRoomType> roomTypeList = (List) renderArgs("roomTypeList");
        assertEquals(1, roomTypeList.size());

        List<Shop> shops = (List) renderArgs("shops");
        assertEquals(2, shops.size());

    }

    @Test
    public void testAdd() {
        Http.Response response = GET(Router.reverse("KtvPriceSchedules.add").url);
        assertIsOk(response);
        assertContentMatch("价格策略", response);
    }

    @Test
    public void testEdit() {
        KtvPriceSchedule ktvPriceSchedule = (KtvPriceSchedule) KtvPriceSchedule.find("order by id desc").fetch().get(0);
        Http.Response response = GET(Router.reverse("KtvPriceSchedules.edit?id=" + ktvPriceSchedule.getId()).url);
        assertIsOk(response);

        KtvPriceSchedule ktvPriceSchedule1 = (KtvPriceSchedule) renderArgs("priceSchedule");
        assertEquals(ktvPriceSchedule1, ktvPriceSchedule);
    }

    @Test
    public void testJsonSearch() {
        Map<String, String> params = new HashMap<>();
        params.put("startDay", DateUtil.dateToString(DateHelper.beforeDays(4), 0));
        params.put("endDay", DateUtil.dateToString(DateHelper.afterDays(4), 0));
        params.put("shop.id", shop.id.toString());
        params.put("roomType.id", ktvRoomType.id.toString());
        Http.Response response = POST(Router.reverse("KtvPriceSchedules.jsonSearch").url, params);
        assertContentType("application/json", response);
        assertCharset("utf-8", response);
        assertContentMatch("\"useWeekDay\":\"1,2,3,4,5\",\"startTime\":\"01:00\",\"endTime\":\"24:00\",\"price\":10.00", response);
    }

    @Ignore
    @Test
    public void testCreate() {
        assertEquals(5, KtvPriceSchedule.count());
        Map<String, String> params = new HashMap<>();
        params.put("priceSchedule.shops.id", shop.id.toString());
        params.put("priceSchedule.roomType.id", ktvRoomType.id.toString());
        params.put("priceSchedule.startDay", "2113-04-22");
        params.put("priceSchedule.endDay", "2199-01-01");
        params.put("priceSchedule.startTime", "09:00");
        params.put("priceSchedule.endTime", "24:00");
        params.put("priceSchedule.price", "20");
        params.put("useWeekDays", "1");
        params.put("useWeekDays", "2");
        Http.Response response = POST(Router.reverse("KtvPriceSchedules.create").url, params);
        assertStatus(302, response);
        assertEquals(6, KtvPriceSchedule.count());
    }

    @Ignore
    @Test
    public void testCreate_PriceNULL() {
        Map<String, String> params = new HashMap<>();
        params.put("priceSchedule.shops.id", shop.id.toString());
        params.put("priceSchedule.roomType.id", ktvRoomType.id.toString());
        params.put("priceSchedule.startDay", "2113-04-22");
        params.put("priceSchedule.endDay", "2199-01-01");
        params.put("priceSchedule.startTime", "09:00");
        params.put("priceSchedule.endTime", "24:00");
        params.put("useWeekDays", "1");
        params.put("useWeekDays", "2");
        Http.Response response = POST(Router.reverse("KtvPriceSchedules.create").url, params);
        assertStatus(200, response);
        List<Error> errors = (List<play.data.validation.Error>) renderArgs("errors");
        assertEquals("请输入价格!", errors.get(0).message());
    }

    @Test
    public void testCreate_Time_Cross() {
        Map<String, String> params = new HashMap<>();
        params.put("priceSchedule.shops.id", shop.id.toString());
        params.put("priceSchedule.roomType.id", ktvRoomType.id.toString());
        params.put("priceSchedule.startDay", "2013-01-01");
        params.put("priceSchedule.endDay", "2199-01-01");
        params.put("priceSchedule.startTime", "19:00");
        params.put("priceSchedule.endTime", "22:00");
        params.put("useWeekDays", "1");
        params.put("useWeekDays", "2");
        params.put("useWeekDays", "3");
        params.put("useWeekDays", "4");
        params.put("useWeekDays", "5");
        params.put("priceSchedule.price", "20");
        Http.Response response = POST(Router.reverse("KtvPriceSchedules.create").url, params);
        assertStatus(200, response);
        List<Error> errors = (List<play.data.validation.Error>) renderArgs("errors");
        assertEquals("该时间段有交叉，请确认！", errors.get(0).message());
    }

    @Test
    public void testUpdate() {
        KtvPriceSchedule ktvPriceSchedule = FactoryBoy.create(KtvPriceSchedule.class);
        ktvPriceSchedule.startDay = new Date();
        ktvPriceSchedule.endDay = DateHelper.afterDays(4);
        ktvPriceSchedule.startTime = "09:00";
        ktvPriceSchedule.endTime = "18:00";
        ktvPriceSchedule.save();

        ktvPriceSchedule.refresh();
        Map<String, String> params = new HashMap<>();
        params.put("priceSchedule.shops.id", shop.id.toString());
        params.put("priceSchedule.roomType.id", ktvRoomType.id.toString());
        params.put("useWeekDays", "1");
        params.put("priceSchedule.startDay", DateUtil.dateToString(new Date(), 0));
        params.put("priceSchedule.endDay", "2198-01-01");
        params.put("priceSchedule.startTime", "19:00");
        params.put("priceSchedule.endTime", "23:00");
        params.put("priceSchedule.price", "20");
        params.put("id", ktvPriceSchedule.id.toString());
        Http.Response response = POST(Router.reverse("KtvPriceSchedules.update").url, params);
        assertStatus(302, response);

        ktvPriceSchedule.refresh();
        assertEquals("2198-01-01", DateUtil.dateToString(ktvPriceSchedule.endDay,0));
        assertEquals("23:00", ktvPriceSchedule.endTime);
    }
    */

}
