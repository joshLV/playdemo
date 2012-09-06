package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrdersCondition;
import models.sales.TelephoneMessage;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.hibernate.usertype.UserType;
import play.Logger;
import play.data.binding.As;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static play.Logger.warn;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-31
 * Time: 上午9:38
 * To change this template use File | Settings | File Templates.
 */

@With(OperateRbac.class)
@ActiveNavigation("crm_app")
public class OperateCRM extends Controller {


    public static void index(CRMCondition condition) {

        String phone = request.params.get("phone");
        String moreSearch = "";
        List<TelephoneMessage> consultContent = TelephoneMessage.find("deleted=? order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

        String currentOperator = OperateRbac.currentUser().loginName;


        if (condition == null) {
            condition = new CRMCondition();

        }



        if (phone != null)
            condition.searchUser = phone;

        if (StringUtils.isNotBlank(condition.searchOrderCoupon))
            moreSearch = condition.searchOrderCoupon;
        else if (StringUtils.isNotBlank(condition.searchUser))
            moreSearch = condition.searchUser;

        if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {

            User user = models.sales.Consult.findUserByCondition(condition);

            Address address = models.sales.Consult.findAddressByCondition(condition);

            List<Order> orderList = models.sales.Consult.findOrderByCondition(condition);
            Logger.debug("a3");


            List<ECoupon> eCoupons = models.sales.Consult.findCouponByCondition(condition);
            Logger.debug("a4");

//            long orderListSize = models.sales.Consult.findOrderByConditionSize(condition);
//            long eCouponsSize = models.sales.Consult.findCouponByConditionSize(condition);
//            , orderListSize, eCouponsSize
            render(user, orderList, address, condition, eCoupons, consultContent, phone, currentOperator, moreSearch);
        }


        render(consultContent, phone, currentOperator, moreSearch);

    }


    public static void save(TelephoneMessage consult, User user) {
        String getPhone = request.params.get("phone");
        CRMCondition condition = new CRMCondition();
        String phone = getPhone;

        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

            if (phone != null)
                condition.searchUser = phone;

            String currentOperator = OperateRbac.currentUser().loginName;


            List<TelephoneMessage> consultContent = TelephoneMessage.find("deleted=? order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

            if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {

                user = models.sales.Consult.findUserByCondition(condition);
                Address address = models.sales.Consult.findAddressByCondition(condition);
                List<Order> orderList = models.sales.Consult.findOrderByCondition(condition);
                List<ECoupon> eCoupons = models.sales.Consult.findCouponByCondition(condition);


//                long orderListSize = models.sales.Consult.findOrderByConditionSize(condition);
//                long eCouponsSize = models.sales.Consult.findCouponByConditionSize(condition);

                render("OperateCRM/index.html", consult, consultContent,
                        currentOperator, phone, user, orderList,
                        address, condition, eCoupons
                       );

            }
            render("OperateCRM/index.html", consult, consultContent, currentOperator, phone, condition);
        }

        consult.deleted = DeletedStatus.UN_DELETED;
        consult.createdBy = OperateRbac.currentUser().loginName;

        consult.phone = getPhone;
        consult.create();
        consult.save();

        index(null);


    }


    /**
     * 删除指定咨询
     *
     * @param id 商品ID
     */
    public static void delete(Long id) {

        TelephoneMessage consult = TelephoneMessage.findById(id);


        models.sales.TelephoneMessage.delete(id);

        index(null);
    }

    public static void edit(Long id) {

        models.sales.TelephoneMessage consult = models.sales.TelephoneMessage.findById(id);
        render(consult, id);
    }


    public static void update(Long id, @Valid models.sales.TelephoneMessage consult) {


        TelephoneMessage oldConsult = TelephoneMessage.findById(id);


        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

            render("OperateCRM/edit.html", consult, id);
        }
        TelephoneMessage.update(id, consult);

        index(null);
    }


}

