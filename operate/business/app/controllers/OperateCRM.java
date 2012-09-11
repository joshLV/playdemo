package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.WithdrawBill;
import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.sales.CallBind;
import models.sales.TelephoneMessage;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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


    public static void index(String phone, CRMCondition condition, Long userId) {
        User user = null;
        Address address = null;

        CallBind bind = new CallBind();
        List<User> userList = User.find("id in (select c.userId from CallBind c where c.phone=?)", phone).fetch();

        if (userList == null || userList.size() <= 0) {
            userList = User.find("mobile=?", phone).fetch();

            //for(User u:userList)
            for (int i = 0; i < userList.size(); i++) {
                bind.userId = userList.get(i).id;
                bind.phone = phone;
                bind.loginName = userList.get(i).loginName;
                bind.save();
            }

        } else {

        }


//        System.out.println("userList" + userList);


        String moreSearch = "";

        List<TelephoneMessage> consultContent = TelephoneMessage.find("deleted=? order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

        String currentOperator = OperateRbac.currentUser().loginName;

//        System.out.println("uid"+condition.userId);
        if (condition == null) {
            condition = new CRMCondition();
            user = userList.get(0);
            address = models.sales.Consult.findAddressByCondition(user);
            condition.userId = userList.get(0).getId();
//            user = User.find("id=?", condition.userId).first();
        } else {

//            user = User.find("id=?", condition.userId).first();
            user = User.find("id=?", userId).first();
            if (user == null)
                user = User.find("id=?", condition.userId).first();
            address = models.sales.Consult.findAddressByCondition(user);
        }


//        if (phone != null)
//            condition.searchUser = phone;

        if (StringUtils.isNotBlank(condition.searchOrderCoupon))
            moreSearch = condition.searchOrderCoupon;
        else if (StringUtils.isNotBlank(condition.searchUser))
            moreSearch = condition.searchUser;

//        if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {


//        Address address = models.sales.Consult.findAddressByCondition(condition);

        List<Order> orderList = models.sales.Consult.findOrderByCondition(condition);

        List<ECoupon> eCoupons = models.sales.Consult.findCouponByCondition(condition);

        List<WithdrawBill> withdrawBill = models.sales.Consult.findBillByCondition(condition);


        long orderListSize = models.sales.Consult.findOrderByConditionSize(condition);
        long eCouponsSize = models.sales.Consult.findCouponByConditionSize(condition);
        long withdrawBillSize = models.sales.Consult.findBillByConditionSize(condition);

        //address  user

        render(address, user, userList, orderList, condition, eCoupons, consultContent, phone, currentOperator, moreSearch, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize);
//        }
//
//
//        render(userList, consultContent, phone, currentOperator, moreSearch);

    }

    //User user
//    name = "user" value = "19"
//    name = "user.id"
    //Long userId
    public static void save(TelephoneMessage consult,Long userId, String phone) {

        CRMCondition condition = new CRMCondition();

        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

//            if (phone != null)
//                condition.searchUser = phone;


            String currentOperator = OperateRbac.currentUser().loginName;

            List<TelephoneMessage> consultContent = TelephoneMessage.find("deleted=? order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

            if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {

                User user = User.find("id=?", userId).first();
                Address address = models.sales.Consult.findAddressByCondition(user);
                List<Order> orderList = models.sales.Consult.findOrderByCondition(condition);
                List<ECoupon> eCoupons = models.sales.Consult.findCouponByCondition(condition);
                List<WithdrawBill> withdrawBill = models.sales.Consult.findBillByCondition(condition);

                long orderListSize = models.sales.Consult.findOrderByConditionSize(condition);
                long eCouponsSize = models.sales.Consult.findCouponByConditionSize(condition);
                long withdrawBillSize = models.sales.Consult.findBillByConditionSize(condition);

                //address user
                render("OperateCRM/index.html", user, address, consult, consultContent,
                        currentOperator, phone, orderList,
                        condition, eCoupons, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize
                );

            }
            render("OperateCRM/index.html", consult, consultContent, currentOperator, phone, condition);
        }

        consult.deleted = DeletedStatus.UN_DELETED;
        consult.createdBy = OperateRbac.currentUser().loginName;
        consult.userType = models.accounts.AccountType.CONSUMER;

        consult.userId = userId;
        consult.phone = phone;

        consult.create();
        consult.save();

        index(phone, null, null);


    }


    /**
     * 删除指定咨询
     *
     * @param id 商品ID
     */
    public static void delete(Long id, String phone) {

        TelephoneMessage consult = TelephoneMessage.findById(id);


        models.sales.TelephoneMessage.delete(id);

        index(phone, null, null);
    }

    public static void edit(Long id) {

        models.sales.TelephoneMessage consult = models.sales.TelephoneMessage.findById(id);
        render(consult, id);
    }


    public static void update(Long id, @Valid models.sales.TelephoneMessage consult, String phone) {


        TelephoneMessage oldConsult = TelephoneMessage.findById(id);


        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

            render("OperateCRM/edit.html", consult, id);
        }
        TelephoneMessage.update(id, consult);

        index(phone, null, null);
    }


}

