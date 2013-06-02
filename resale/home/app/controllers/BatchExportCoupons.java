package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.order.BatchCoupons;
import models.order.BatchCouponsCondition;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.resale.ResalerCreditable;
import models.resale.ResalerFav;
import models.sales.Goods;
import models.sales.GoodsCondition;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 批量导出券
 * <p/>
 * User: wangjia
 * Date: 12-11-21
 * Time: 上午11:24
 */

@With(SecureCAS.class)
public class BatchExportCoupons extends Controller {
    private static final int PAGE_SIZE = 15;
    public static String noPermissionError = null;


    /**
     * 券号列表
     */
    public static void index(BatchCouponsCondition condition) {
        Resaler user = SecureCAS.getResaler();
        String noPermissionError = null;
        if (user.isBatchExportCoupons() == true) {
            String page = request.params.get("page");
            int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
            if (condition == null) {
                condition = new BatchCouponsCondition();
            }
            condition.operatorId = user.getId();
            JPAExtPaginator<BatchCoupons> couponPage = BatchCoupons.findByCondition(condition,
                    pageNumber, PAGE_SIZE);

            for (BatchCoupons coupon : couponPage.getCurrentPage()) {
                if (coupon.operatorId != null) {
                    Resaler resaler = Resaler.findById(coupon.operatorId);
                    if (user != null) {
                        coupon.operatorName = resaler.loginName;
                    }
                }
            }
            render(couponPage, condition, noPermissionError);
        } else {
            noPermissionError = "此账户没有批量发券的权限";
            render(noPermissionError);
        }
    }


    public static void generator(String err, int count, String name, Long goodsId, BigDecimal consumed) {
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(user);
        if (consumed == null) {
            consumed = BigDecimal.ZERO;
        }
        if (user.isBatchExportCoupons() == true) {
            String page = params.get("page");
            int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
            GoodsCondition goodsCond = new GoodsCondition();
//            JPAExtPaginator<models.sales.Goods> goodsList = models.sales
//                    .Goods.findByResaleCondition(user, goodsCond, pageNumber, PAGE_SIZE);
            Goods goods = null;

            List<Goods> goodsList = new ArrayList<>();
            String goodsName = null;
            Resaler resaler = SecureCAS.getResaler();
            if (goodsId != null) {
                goods = Goods.findById(goodsId);
                goodsName = goods.shortName;
            }
            List<ResalerFav> favs = ResalerFav.findFavs(resaler, null, null,
                    null, null);
            for (ResalerFav r : favs) {
                goodsList.add(r.goods);
            }
            System.out.println(goods + "《=========goods:");
            render(goodsList, account, noPermissionError, count, name, goodsId, err, consumed, goods);
        } else {
            noPermissionError = "此账户没有批量发券的权限";
            render(noPermissionError);
        }
    }


    public static void generate(int count, String name, Long goodsId, BigDecimal consumed) throws NotEnoughInventoryException {
        Pattern pattern = Pattern.compile("^[0-9]*[1-9][0-9]*$");
        Resaler resaler = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(resaler);

        if (name == null || name.trim().equals("")) {

            generator("备注名称不能为空", count, name, goodsId, consumed);
//        } else if (StringUtils.isBlank(prefix) || !pattern.matcher(prefix).matches() || prefix.length() < 2) {
//            generator("前缀不符合规范", count, name, prefix, goodsId, consumed);
        } else if (count < 1 || count > 9999) {
            generator("数量不符合规范", count, name, goodsId, consumed);
        } else if (resaler.creditable == ResalerCreditable.NO && consumed.compareTo(account.amount) > 0) {
            generator("账户余额不够，请先充值", count, name, goodsId, consumed);
        }
        //加载用户账户信息
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        BatchCoupons batchCoupons = new BatchCoupons();
        batchCoupons.name = name;
        batchCoupons.goodsName = goods.shortName;
        batchCoupons.count = count;
        batchCoupons.operatorId = resaler.getId();
        batchCoupons.coupons = new LinkedList<>();
        batchCoupons.save();
        for (int i = 0; i < count; i++) {
            Order order = Order.createResaleOrder(resaler);
            Long number = 1l;
            String phone = null;
            BigDecimal resalerPrice = goods.getResalePrice();
            order.addOrderItem(goods, number, phone,
                    resalerPrice, // 分销商成本价即成交价
                    resalerPrice  // 分销商成本价
            );
            order.deliveryType = DeliveryType.SMS;
            order.createAndUpdateInventory();

            if (order == null) {
                error(500, "no such order");
            }
            if (order.status != OrderStatus.UNPAID) {
                error("wrong order status");
            }

            order.save();
            boolean paidSuccess = false;
            if (resaler.isCreditable()) {
                order.accountPay = order.needPay;
                order.discountPay = BigDecimal.ZERO;
                order.payMethod = PaymentSource.getBalanceSource().code;
                order.payAndSendECoupon();
                order.save();
                paidSuccess = true;
            } else {
                paidSuccess = Order.confirmPaymentInfo(order, account, true, "balance");
            }
            if (paidSuccess) {
                ECoupon coupon = ECoupon.find("order=?", order).first();
                coupon.batchCoupons = batchCoupons;
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
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        Resaler user = SecureCAS.getResaler();
        if (user.isBatchExportCoupons() == true) {
            Map<String, Object> qparams = new HashMap<>();
            qparams.put("batchCouponId", id);
            JPAExtPaginator<ECoupon> couponPage = new JPAExtPaginator("ECoupon e", "e", ECoupon.class,
                    "e.batchCoupons.id = :batchCouponId", qparams);
            couponPage.setPageNumber(pageNumber);
            couponPage.setPageSize(PAGE_SIZE);
            couponPage.setBoundaryControlsEnabled(true);
            render(couponPage, id, noPermissionError);
        } else {
            noPermissionError = "此账户没有批量发券的权限";
            render(noPermissionError);
        }
    }

    private static boolean isNotUniqueEcouponSn(String randomNumber) {
        return ECoupon.find("from ECoupon where eCouponSn=?", randomNumber)
                .fetch().size() > 0;
    }


    public static void batchCouponsExcelOut(Long id) {
        BatchCoupons batchCoupons = BatchCoupons.findById(id);
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "券号列表_" + System.currentTimeMillis() + "备注:" + batchCoupons.name + ".xls");
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("batchCouponId", id);
        JPAExtPaginator<ECoupon> couponList = new JPAExtPaginator("ECoupon e", "e", ECoupon.class,
                "e.batchCoupons.id = :batchCouponId", qparams);
        couponList.setPageNumber(1);
        couponList.setPageSize(PAGE_SIZE);
        couponList.setBoundaryControlsEnabled(true);
        render(couponList);

    }


}


