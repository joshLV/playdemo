package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.resale.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.order.*;
import models.resale.Resaler;
import models.resale.ResalerCart;
import models.sales.*;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-21
 * Time: 上午11:24
 * To change this template use File | Settings | File Templates.
 */

@With(SecureCAS.class)
public class BatchExportCoupons extends Controller {
    private static final int PAGE_SIZE = 15;

    /**
     * 券号列表
     */
    public static void index(BatchCouponsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new BatchCouponsCondition();
        }

        JPAExtPaginator<BatchCoupons> couponPage = BatchCoupons.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        for (BatchCoupons coupon : couponPage) {
            if (coupon.operatorId != null) {
                Resaler user = Resaler.findById(coupon.operatorId);
                if (user != null) {
                    coupon.operatorName = user.loginName;
                }
            }
        }
        render(couponPage, condition);
        render();
    }


    public static void generator(String err, int count, String name, String prefix, Long goodsId) {
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(user.getId());
//        List<models.sales.Goods> goodsList = models.sales.Goods.find("deleted=?", DeletedStatus.UN_DELETED).fetch();

        Resaler resaler = SecureCAS.getResaler();
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        GoodsCondition goodsCond = new GoodsCondition();
        JPAExtPaginator<models.sales.Goods> goodsList = models.sales
                .Goods.findByResaleCondition(resaler, goodsCond, pageNumber, PAGE_SIZE);

        render(goodsList, account, err, count, name, prefix, goodsId);
    }


    public static void generate(int count, String name, String prefix, Long goodsId) throws NotEnoughInventoryException {
        Pattern pattern = Pattern.compile("^[0-9]*[1-9][0-9]*$");
        if (name == null || name.trim().equals("")) {
            generator("备注名称不能为空", count, name, prefix, goodsId);
        } else if (StringUtils.isBlank(prefix) || !pattern.matcher(prefix).matches() || prefix.length() > 2) {
            generator("前缀不符合规范", count, name, prefix, goodsId);
        } else if (count < 1 || count > 9999) {
            generator("数量不符合规范", count, name, prefix, goodsId);
        }
        //加载用户账户信息
        Resaler resaler = SecureCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        BatchCoupons batchCoupons = new BatchCoupons();
        batchCoupons.name = name;
        batchCoupons.goodsName = goods.shortName;
        batchCoupons.count = count;
        batchCoupons.operatorId = resaler.getId();
        batchCoupons.coupons = new LinkedList<>();
        batchCoupons.save();
        for (int i = 0; i < count; i++) {
            System.out.println("i>>>" + i);
            Order order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
            int number = 1;
            String phone = null;
            BigDecimal resalerPrice = goods.getResalePrice();
            order.addOrderItem(goods, number, phone,
                    resalerPrice, // 分销商成本价即成交价
                    resalerPrice  // 分销商成本价
            );
            order.deliveryType = DeliveryType.SMS;
            order.createAndUpdateInventory();

//            PaymentInfo.confirm(order.orderNumber, true, "alipay");
            if (order == null) {
                error(500, "no such order");
            }
            if (order.status != OrderStatus.UNPAID) {
                error("wrong order status");
            }
            Account account = AccountUtil.getResalerAccount(resaler.getId());

            if (Order.confirmPaymentInfo(order, account, true, "balance")) {
                ECoupon coupon = ECoupon.find("order=?", order).first();

                coupon.eCouponSn = prefix + coupon.eCouponSn;
                coupon.save();
                batchCoupons.coupons.add(coupon);
                batchCoupons.save();
            } else {
                error(500, "can no confirm the payment info");
            }
        }
        index(null);
    }

    public static void details(Long id) {
        System.out.println("id>>" + id);
        BatchCoupons batchCoupons = BatchCoupons.findById(id);
        System.out.println("batchCoupons>>>" + batchCoupons);
        List<ECoupon> couponsList = batchCoupons.coupons;
        System.out.println("couponsList??>>>" + couponsList);
        render(couponsList);
    }

}


