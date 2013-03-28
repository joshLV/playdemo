package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.DeliveryType;
import models.order.DiscountCode;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderDiscount;
import models.order.OrderItems;
import models.order.PromoteRebate;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static String getQueryStringWithoutDiscountSN() {
        List<String> kvs = new ArrayList<>();
        for (String key : request.params.all().keySet()) {
            if (!"discountSN".equals(key) && !"body".equals(key) && !"mobile".equals(key)) {
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
        List<String> paramsList = new ArrayList<>();
        for (Long goodsId : gid) {
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            if (goods == null) {
                continue;
            }
            String[] numberStr = params.get("g" + goodsId);
            int number = 1;
            if (numberStr != null && numberStr.length > 0) {
                number = Integer.parseInt(numberStr[0]);
                if (number > 999) {
                    number = 999;
                }
                long realStock = goods.getRealStocks();
                if (number > realStock) {
                    number = (int) realStock;
                }
            }
            paramsList.add(goodsId + "-" + number);
        }

        items = StringUtils.join(paramsList, ",");

        DiscountCode discountCode = getDiscountCode();

        showOrder(items, discountCode);

        User user = SecureCAS.getUser();
        List<String> orderItems_mobiles = OrderItems.getMobiles(user);
        renderArgs.put("querystring", getQueryStringWithoutDiscountSN());

        String mobile = request.params.get("mobile");
        if (StringUtils.isBlank(mobile)) {
            mobile = user.mobile;
        }
        //用于重新刷新整个页面
        render(mobile, orderItems_mobiles);
    }

    protected static DiscountCode getDiscountCode() {
        // 折扣券
        String discountSN = request.params.get("discountSN");
        //这里用于判断是否是通过推荐过来的用户，是则取得推荐码
        Http.Cookie cookie = request.cookies.get(PROMOTER_COOKIE);
        User promoteUser = null;
        if (StringUtils.isBlank(discountSN)) {
            if (cookie != null) {
                discountSN = cookie.value;
            }
            renderArgs.put("discountErrorInfo", "");
        }

        if (StringUtils.isNotBlank(discountSN)) {
            promoteUser = User.getUserByPromoterCode(discountSN);
            if (promoteUser == null) {
                renderArgs.put("discountErrorInfo", "无效的优惠码，请重新输入");
            } else {
                renderArgs.put("userPromoterCode", discountSN);
            }
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
        Map<Long, Long> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);

        //计算电子商品列表和非电子商品列表
        List<Cart> eCartList = new ArrayList<>();
        BigDecimal eCartAmount = BigDecimal.ZERO;
        List<Cart> rCartList = new ArrayList<>();
        BigDecimal rCartAmount = BigDecimal.ZERO;
        List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
        for (models.sales.Goods g : goods) {

            Long number = itemsMap.get(g.getId());
            Http.Cookie cookie = request.cookies.get(PROMOTER_COOKIE);
            Cart cart = new Cart(g, number);
            //这里用于判断是否是通过推荐过来的用户
            String discountSN = request.params.get("discountSN");
            User user = SecureCAS.getUser();
            //判断是不是推荐返利的情况
            boolean isPromoteFlag = isByPromoteUser(user, discountSN, cookie);

            if (isPromoteFlag) {
                cart.rebateValue = Order.getPromoteRebateOfGoodsAmount(g, number);
            } else {
                cart.rebateValue = Order.getDiscountValueOfGoodsAmount(g, number, discountCode);
            }
            if (g.materialType != null) {
                if (g.materialType == models.sales.MaterialType.REAL) {
                    rCartList.add(cart);
                    rCartAmount = rCartAmount.add(cart.getLineValue());
                } else if (g.materialType == models.sales.MaterialType.ELECTRONIC) {
                    eCartList.add(cart);
                    eCartAmount = eCartAmount.add(cart.getLineValue());
                }
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
            for (Cart c : rCartList) {
                if (c.goods.freeShipping != true) {
                    rCartAmount = rCartAmount.add(Order.FREIGHT);
                    break;
                }
            }
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
        Map<Long, Long> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);
        List<models.sales.Goods> goodsList = models.sales.Goods.findInIdList(goodsIds);
        boolean containsElectronic = containsMaterialType(goodsList, MaterialType.ELECTRONIC);
        boolean containsReal = containsMaterialType(goodsList, MaterialType.REAL);

        //电子券必须校验手机号
        if (containsElectronic) {
            Validation.required("mobile", mobile);
            Validation.match("mobile", mobile, "^1[3|4|5|8][0-9]\\d{4,8}$");
        }

        if (checkLimitNumber(items, mobile)) {
            Validation.addError("mobile_limit_num", "该手机号已超过限购数量，请确认！");
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
            render("Orders/index.html", user, orderItems_mobiles, mobile);
        }

        //创建订单
        Order order = Order.createYbqConsumeOrder(user.getId(), AccountType.CONSUMER);
        if (containsElectronic) {
            order.deliveryType = DeliveryType.SMS;
        } else if (containsReal) {
            order.deliveryType = DeliveryType.LOGISTICS;
        }

        //记录来源跟踪ID
        if (Play.mode != Play.Mode.DEV && WebsiteInjector.getUserWebIdentification() != null) {
            order.webIdentificationId = WebsiteInjector.getUserWebIdentification().getSavedId();
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
            String discountSN = request.params.get("discountSN");

            //判断是不是推荐返利的情况
            boolean isPromoteFlag = isByPromoteUser(user, discountSN, tj_cookie);

            for (models.sales.Goods goodsItem : goodsList) {
                Long number = itemsMap.get(goodsItem.getId());
                if (goodsItem.materialType == models.sales.MaterialType.REAL) {
                    if (isPromoteFlag) {
                        rCartAmount = rCartAmount.add(Order.getPromoteRebateOfTotalGoodsAmount(goodsItem, number));
                    } else {
                        rCartAmount = rCartAmount.add(Order.getDiscountGoodsAmount(goodsItem, number, discountCode));

                    }
                } else if (goodsItem.materialType == models.sales.MaterialType.ELECTRONIC) {
                    if (isPromoteFlag) {
                        eCartAmount = eCartAmount.add(Order.getPromoteRebateOfTotalGoodsAmount(goodsItem, number));
                    } else {
                        eCartAmount = eCartAmount.add(Order.getDiscountGoodsAmount(goodsItem, number, discountCode));
                    }
                }
                OrderItems orderItem = null;

                if (goodsItem.materialType == MaterialType.REAL) {
                    orderItem = order.addOrderItem(goodsItem, number, receiverMobile,
                            goodsItem.salePrice, //最终成交价
                            goodsItem.getResalerPriceOfUhuila(), //一百券作为分销商的成本价
                            discountCode, isPromoteFlag
                    );
                } else {
                    orderItem = order.addOrderItem(goodsItem, number, mobile,
                            goodsItem.salePrice, //最终成交价
                            goodsItem.getResalerPriceOfUhuila(), //一百券作为分销商的成本价
                            discountCode, isPromoteFlag
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
            //判断cookie中的推荐码是否存在;
            if (isPromoteFlag) {
                String tj_cookieValue = tj_cookie == null ? "" : tj_cookie.value;
                if ("".equals(tj_cookieValue)) tj_cookieValue = discountSN;
                User promoterUser = User.getUserByPromoterCode(tj_cookieValue);
                if (promoterUser != null && promoterUser != user) {
                    //保存推荐人的用户ID
                    order.promoteUserId = promoterUser.id;
                    // 不需要有rebateValue
                    // order.rebateValue = Order.getPromoteRebateOfTotalECartAmount(order);
                    order.amount = eCartAmount.add(rCartAmount);
                    //如果通过注册的，则更新推荐关系
                    PromoteRebate promoteRebate = PromoteRebate.find("promoteUser=? and invitedUser=? and registerFlag=true", promoterUser, user).first();
                    if (promoteRebate != null) {
                        promoteRebate.promoteUser = promoterUser;
                        promoteRebate.order = order;
                        promoteRebate.rebateAmount = Order.getPromoteRebateAmount(order);
                        promoteRebate.registerFlag = false;
                        promoteRebate.save();
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

    private static boolean isByPromoteUser(User user, String discountSN, Http.Cookie cookie) {

        boolean isPromoteFlag = false;
        User promoteUser = null;
        //手动输入tj码XXXX的方式
        if (StringUtils.isNotBlank(discountSN)) {
            promoteUser = User.getUserByPromoterCode(discountSN);
        } else if (cookie != null) {
            //接地址用tj=XXXX的方式请求
            promoteUser = User.getUserByPromoterCode(cookie.value);
        }
        //推荐人不能是自己
        if (user != promoteUser && promoteUser != null) isPromoteFlag = true;
        return isPromoteFlag;
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

    private static void parseItems(String items, List<Long> goodsIds, Map<Long, Long> itemsMap) {
        User user = SecureCAS.getUser();
        String[] itemSplits = items.split(",");
        for (String split : itemSplits) {

            String[] goodsItem = split.split("-");
            if (goodsItem.length == 2) {
                long number = Long.parseLong(goodsItem[1]);
                if (number > 0) {
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Long boughtNumber = OrderItems.itemsNumber(user, goodsId);
                    boolean canNotBuy = Order.checkLimitNumber(goodsId, boughtNumber, number);
                    if (canNotBuy) {
                        redirect(WWW_URL + "/p/" + goodsId);
                        return;
                    }
                    //取出商品的限购数量
                    Goods goods = Goods.findById(goodsId);
                    long limitNumber = 0L;
                    if (goods.limitNumber != null) {
                        limitNumber = goods.limitNumber;
                    }
                    if ((limitNumber > boughtNumber) && number > (limitNumber - boughtNumber.intValue())) {
                        number = limitNumber - boughtNumber;
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
     * @param phone 手机
     */

    private static boolean checkLimitNumber(String items, String phone) {
        String[] itemSplits = items.split(",");
        for (String split : itemSplits) {
            String[] goodsItem = split.split("-");
            if (goodsItem.length == 2) {
                Integer number = Integer.parseInt(goodsItem[1]);
                if (number > 0) {
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Long boughtNumber = OrderItems.getBuyNumberByPhone(phone, goodsId);
                    return Order.checkLimitNumber(goodsId, boughtNumber, number);
                }
            }
        }
        return false;
    }
}
