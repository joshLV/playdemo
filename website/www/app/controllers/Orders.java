package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.*;
import models.sales.Goods;
import models.sales.MaterialType;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.*;

import static play.Logger.warn;

/**
 * 用户订单确认控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 11:31 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class Orders extends Controller {
    public static String WWW_URL = Play.configuration.getProperty("application.baseUrl", "");
    public static final String PROMOTER_COOKIE = "promoter_track";

    private static String getQueryStringWithoutDdiscountSN() {
        List<String> kvs = new ArrayList<>();
        for (String key : request.params.all().keySet()) {
            if (!"discountSN".equals(key) && !"body".equals(key)) {
                String[] values = request.params.getAll(key);
                for (String value : values) {
                    kvs.add(key + "=" + value);
                }
            }
        }
        return StringUtils.join(kvs, "&");
    }

    /**
     * 预览订单.
     *
     * @param gid=37&gid=102&g37=2&g102=3&discountSN=xxx
     *
     */
    public static void index(List<Long> gid) {

        if (gid == null || gid.size() == 0) {
            error("未选择商品!");
            return;
        }
        Map<String, String[]> params = request.params.all();
        String items = "";
        for (Long goodsId : gid) {
            String[] numberStr = params.get("g" + goodsId);
            int number = 1;
            if (numberStr != null && numberStr.length > 0) {
                number = Integer.parseInt(numberStr[0]);
            }
            items += goodsId + "-" + number + ",";
        }

        DiscountCode discountCode = getDiscountCode();

        showOrder(items, discountCode);

        User user = SecureCAS.getUser();
        List<String> orderItems_mobiles = OrderItems.getMobiles(user);
        //用于重新刷新整个页面
        renderArgs.put("querystring", getQueryStringWithoutDdiscountSN());
        render(user, orderItems_mobiles);
    }

    protected static DiscountCode getDiscountCode() {
        //这里用于判断是否是通过推荐过来的用户，是则取得推荐码
        Http.Cookie cookie = request.cookies.get(PROMOTER_COOKIE);
        if (cookie != null) {
            System.out.println(cookie.value+">>>>>>>>>>>>>");
            renderArgs.put("userPromoterCode", cookie.value);
        }

        // 折扣券
        String discountSN = request.params.get("discountSN");

        if (discountSN == null) {
            renderArgs.put("discountErrorInfo", "");
        } else {
            renderArgs.put("discountErrorInfo", "无效的优惠码，请重新输入");
        }

        if (StringUtils.isEmpty(discountSN) && WebsiteInjector.getUserWebIdentification() != null) {
            // 访问使用的推荐码尝试作为折扣券号
            discountSN = WebsiteInjector.getUserWebIdentification().referCode;
        } else {
            renderArgs.put("discountSN", discountSN);
        }
        DiscountCode discountCode = DiscountCode.findAvailableSN(discountSN);
        renderArgs.put("discountCode", discountCode);

        return discountCode;

    }

    private static void showOrder(String items, DiscountCode discountCode) {
        //解析提交的商品及数量
        List<Long> goodsIds = new ArrayList<>();
        Map<Long, Integer> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);

        //计算电子商品列表和非电子商品列表
        List<Cart> eCartList = new ArrayList<>();
        BigDecimal eCartAmount = BigDecimal.ZERO;
        List<Cart> rCartList = new ArrayList<>();
        BigDecimal rCartAmount = BigDecimal.ZERO;

        List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
        for (models.sales.Goods g : goods) {

            Integer number = itemsMap.get(g.getId());
            Cart cart = new Cart(g, number);
            //这里用于判断是否是通过推荐过来的用户
            Http.Cookie cookie = request.cookies.get(PROMOTER_COOKIE);
            if (cookie != null) {
                System.out.println("+++++++++++++="+cookie.value);
                cart.rebateValue = Order.getPromoteRebateOfGoodsAmount(g, number);
            } else {
                cart.rebateValue = Order.getDiscountValueOfGoodsAmount(g, number, discountCode);
            }
            if (g.materialType == models.sales.MaterialType.REAL) {
                rCartList.add(cart);
                rCartAmount = rCartAmount.add(cart.getLineValue());
            } else if (g.materialType == models.sales.MaterialType.ELECTRONIC) {
                eCartList.add(cart);
                eCartAmount = eCartAmount.add(cart.getLineValue());
            }
        }

        if (rCartList.size() == 0 && eCartList.size() == 0) {
            error("no goods specified");
            return;
        }

        List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
        Address defaultAddress = Address.findDefault(SecureCAS.getUser());

        //如果有实物商品，加上运费
        if (rCartList.size() > 0) {
            rCartAmount = rCartAmount.add(Order.FREIGHT);
        }

        // 整单折扣，注意只折扣电子券产品，实物券不参与折扣.
        BigDecimal eCartRebate = Order.getDiscountValueOfTotalECartAmount(eCartAmount, discountCode);

        BigDecimal totalAmount = eCartAmount.add(rCartAmount);   // 总金额
        BigDecimal needPay = totalAmount.subtract(eCartRebate);  // 应付金额
        if (needPay.compareTo(BigDecimal.ZERO) <= 0) {
            needPay = BigDecimal.ZERO;
        }
        BigDecimal goodsAmount = rCartList.size() == 0 ? eCartAmount : totalAmount.subtract(Order.FREIGHT);

        renderArgs.put("goodsAmount", goodsAmount);
        renderArgs.put("totalAmount", totalAmount);
        renderArgs.put("needPay", needPay);
        renderArgs.put("eCartRebate", eCartRebate);
        renderArgs.put("addressList", addressList);
        renderArgs.put("address", defaultAddress);
        renderArgs.put("eCartList", eCartList);
        renderArgs.put("eCartAmount", eCartAmount);
        renderArgs.put("rCartList", rCartList);
        renderArgs.put("rCartAmount", rCartAmount);
        renderArgs.put("items", items);
        renderArgs.put("querystring", request.querystring);
    }

    /**
     * 提交订单.
     */
    public static void create(String items, String mobile, String remark) {
        //如果订单中有电子券，则必须填写手机号
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        User user = SecureCAS.getUser();

        //解析提交的商品及数量
        List<Long> goodsIds = new ArrayList<>();
        Map<Long, Integer> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);
        List<models.sales.Goods> goodsList = models.sales.Goods.findInIdList(goodsIds);
        boolean containsElectronic = containsMaterialType(goodsList, MaterialType.ELECTRONIC);
        boolean containsReal = containsMaterialType(goodsList, MaterialType.REAL);

        //电子券必须校验手机号
        if (containsElectronic) {
            Validation.required("mobile", mobile);
            Validation.match("mobile", mobile, "^1[3|4|5|8][0-9]\\d{4,8}$");
        }

        //实物券必须校验收货地址信息
        Address defaultAddress = null;
        String receiverMobile = "";
        if (containsReal) {
            defaultAddress = Address.findDefault(SecureCAS.getUser());
            if (defaultAddress == null) {
                Validation.addError("address", "validation.required");
            } else {
                receiverMobile = defaultAddress.mobile;
            }
        }

        DiscountCode discountCode = getDiscountCode();

        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            List<String> orderItems_mobiles = OrderItems.getMobiles(user);

            showOrder(items, discountCode);
            render("Orders/index.html", user, orderItems_mobiles);
        }

        //创建订单
        Order order = Order.createConsumeOrder(user.getId(), AccountType.CONSUMER);
        if (containsElectronic) {
            order.deliveryType = DeliveryType.SMS;
        } else if (containsReal) {
            order.deliveryType = DeliveryType.LOGISTICS;
        }

        //记录来源跟踪ID
        if (WebsiteInjector.getUserWebIdentification() != null) {
            order.webIdentificationId = WebsiteInjector.getUserWebIdentification().id;
        }

        if (defaultAddress != null) {
            order.setAddress(defaultAddress);
        }
        order.save();  //为了保存OrderDiscount，需要先保存order.

        //添加订单条目
        try {
            //计算电子商品列表和非电子商品列表
            BigDecimal eCartAmount = BigDecimal.ZERO;
            BigDecimal rCartAmount = BigDecimal.ZERO;
            //取得cookie中的推荐码
            Http.Cookie tj_cookie = request.cookies.get(PROMOTER_COOKIE);
            for (models.sales.Goods goodsItem : goodsList) {
                Integer number = itemsMap.get(goodsItem.getId());
                String tj_cookieValue = tj_cookie == null ? "" : tj_cookie.value;

                if (goodsItem.materialType == models.sales.MaterialType.REAL) {
                    if ("".equals(tj_cookieValue)) {
                        rCartAmount = rCartAmount.add(Order.getDiscountGoodsAmount(goodsItem, number, discountCode));
                    } else {
                        rCartAmount = rCartAmount.add(Order.getPromoteRebateOfTotalGoodsAmount(goodsItem, number));
                    }
                } else if (goodsItem.materialType == models.sales.MaterialType.ELECTRONIC) {
                    if ("".equals(tj_cookieValue)) {
                        eCartAmount = eCartAmount.add(Order.getDiscountGoodsAmount(goodsItem, number, discountCode));
                    } else {
                        eCartAmount = eCartAmount.add(Order.getPromoteRebateOfTotalGoodsAmount(goodsItem, number));
                    }
                }
                OrderItems orderItem = null;

                if (goodsItem.materialType == MaterialType.REAL) {
                    orderItem = order.addOrderItem(goodsItem, number, receiverMobile,
                            goodsItem.salePrice, //最终成交价
                            goodsItem.getResalerPriceOfUhuila(), //一百券作为分销商的成本价
                            discountCode, tj_cookieValue
                    );
                } else {
                    orderItem = order.addOrderItem(goodsItem, number, mobile,
                            goodsItem.salePrice, //最终成交价
                            goodsItem.getResalerPriceOfUhuila(), //一百券作为分销商的成本价
                            discountCode, tj_cookieValue
                    );
                }
                orderItem.save();

                // 保存商品折扣
                if (discountCode != null && discountCode.goods != null && discountCode.goods.id == goodsItem.id) {
                    OrderDiscount orderDiscount = new OrderDiscount();
                    orderDiscount.discountCode = discountCode;
                    orderDiscount.order = order;
                    orderDiscount.orderItem = orderItem;
                    orderDiscount.discountAmount = Order.getDiscountValueOfGoodsAmount(goodsItem, number, discountCode);
                    orderDiscount.save();
                }
            }

            // 整单折扣，注意只折扣电子券产品，实物券不参与折扣.
            if (discountCode != null && discountCode.goods == null) {
                order.rebateValue = Order.getDiscountValueOfTotalECartAmount(eCartAmount, discountCode);
                order.amount = eCartAmount.add(rCartAmount);
                order.needPay = order.amount.subtract(order.rebateValue);
                if (order.needPay.compareTo(BigDecimal.ZERO) <= 0) {
                    order.needPay = BigDecimal.ZERO;
                }

                OrderDiscount orderDiscount = new OrderDiscount();
                orderDiscount.discountCode = discountCode;
                orderDiscount.order = order;
                orderDiscount.discountAmount = Order.getDiscountValueOfTotalECartAmount(eCartAmount, discountCode);
                orderDiscount.save();
            }
            //判断cookie中的推荐码是否存在; 只折扣电子券产品，实物券不参与折扣.
            if (tj_cookie != null) {
                User promoterUser = User.getUserByPromoterCode(tj_cookie.value);
                if (promoterUser != user) {
                    //保存推荐人的用户ID
                    order.promoteUserId = promoterUser.id;
                    order.rebateValue = Order.getPromoteRebateOfTotalECartAmount(eCartAmount, order);
                    order.amount = eCartAmount.add(rCartAmount);
//                    order.needPay = order.amount;
//                    if (order.needPay.compareTo(BigDecimal.ZERO) <= 0) {
//                        order.needPay = BigDecimal.ZERO;
//                    }
                    //如果通过注册的，则更新推荐关系
                    PromoteRebate promoteRebate = PromoteRebate.find("promoteUser=? and invitedUser=? and registerFlag=true", promoterUser, user).first();
                    if (promoteRebate != null) {
                        promoteRebate.order = order;
                        promoteRebate.rebateAmount = Order.getPromoteRebateAmount(order);
                        promoteRebate.save();
                        response.removeCookie(PROMOTER_COOKIE);
                    } else {
                        //记录推荐人和被推荐人的关系
                        new PromoteRebate(promoterUser, user, order, Order.getPromoteRebateAmount(order), false).save();
                    }
                }
            }

        } catch (NotEnoughInventoryException e) {
            //todo 缺少库存
            Logger.error(e, "inventory not enough");
            error("商品库存不足！");
        }
        order.remark = remark;

        //确认订单
        order.createAndUpdateInventory();


        //删除购物车中相应物品
        Cart.delete(user, cookieValue, goodsIds);


        redirect("/payment_info/" + order.orderNumber);
    }

    private static boolean containsMaterialType(List<Goods> goods, MaterialType type) {
        boolean containsMaterialType = false;
        for (Goods good : goods) {
            if (type.equals(good.materialType)) {
                containsMaterialType = true;
                break;
            }
        }
        return containsMaterialType;
    }

    private static void parseItems(String items, List<Long> goodsIds, Map<Long, Integer> itemsMap) {
        User user = SecureCAS.getUser();
        String[] itemSplits = items.split(",");
        for (String split : itemSplits) {
            String[] goodsItem = split.split("-");
            if (goodsItem.length == 2) {
                Integer number = Integer.parseInt(goodsItem[1]);
                if (number > 0) {
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Long boughtNumber = OrderItems.itemsNumber(user, goodsId);
                    boolean isBuyFlag = Order.checkLimitNumber(user, goodsId, boughtNumber, number);
                    if (isBuyFlag) {
                        redirect(WWW_URL + "/g/" + goodsId);
                        return;
                    }
                    //取出商品的限购数量
                    Goods goods = Goods.findById(goodsId);
                    int limitNumber = 0;
                    if (goods.limitNumber != null) {
                        limitNumber = goods.limitNumber;
                    }
                    if ((limitNumber > boughtNumber) && number > (limitNumber - boughtNumber.intValue())) {
                        number = limitNumber - boughtNumber.intValue();
                    }
                    goodsIds.add(goodsId);
                    itemsMap.put(goodsId, number);
                }
            }
        }
    }

    /**
     * 判断限购数量
     *
     * @param items 商品列表
     */

    public static void checkLimitNumber(String items) {
        User user = SecureCAS.getUser();
        String[] itemSplits = items.split(",");
        for (String split : itemSplits) {
            String[] goodsItem = split.split("-");
            if (goodsItem.length == 2) {
                Integer number = Integer.parseInt(goodsItem[1]);
                if (number > 0) {
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Long boughtNumber = OrderItems.itemsNumber(user, goodsId);
                    boolean isCanBuyFlag = Order.checkLimitNumber(user, goodsId, boughtNumber, number);
                    if (isCanBuyFlag) {
                        renderJSON("1");
                    }
                }
            }
        }
        renderJSON("0");
    }
}
