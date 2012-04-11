package controllers;

import java.util.Date;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.resale.Resaler;

import org.apache.commons.lang.StringUtils;

import controllers.modules.resale.cas.SecureCAS;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 券订单列表.
 * 
 * @author likang
 *
 */
@With(SecureCAS.class)
public class Coupons extends Controller{
    private static final int PAGE_SIZE = 20;
    
    public static void index(Date createdAtBegin, Date createdAtEnd, ECouponStatus status, String goodsName, String orderNumber, String phone){
        Resaler user = SecureCAS.getResaler();
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<ECoupon> couponsList = ECoupon.userCouponsQuery(user.getId(), AccountType.RESALER, createdAtBegin, createdAtEnd, 
                status, goodsName,orderNumber, phone, pageNumber,  PAGE_SIZE);
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");

        renderArgs.put("createdAtBegin", createdAtBegin);
        renderArgs.put("createdAtEnd", createdAtEnd);
        renderArgs.put("status", status);
        renderArgs.put("goodsName", goodsName);
        renderArgs.put("orderNumber", orderNumber);
        renderArgs.put("phone", phone);
       
        render(couponsList, breadcrumbs);
        
    }

}
