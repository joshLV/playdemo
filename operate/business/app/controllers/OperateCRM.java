package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.WithdrawBill;
import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.sales.ConsultCondition;
import models.sales.ConsultRecord;
import models.sales.CouponCallBind;
import models.sales.MemberCallBind;
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


        MemberCallBind bind = new MemberCallBind();
        List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();

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

        List<ConsultRecord> consultContent = ConsultRecord.find("deleted=? and phone=? order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();

        String currentOperator = OperateRbac.currentUser().loginName;

//        System.out.println("uid"+condition.userId);
        if (condition == null) {
            condition = new CRMCondition();
            if (userList.size() > 0) {
                user = userList.get(0);
                address = ConsultCondition.findAddressByCondition(user);
                condition.userId = userList.get(0).getId();
            }
//            user = User.find("id=?", condition.userId).first();
        } else {

//            user = User.find("id=?", condition.userId).first();
            user = User.find("id=?", userId).first();
            if (user == null)
                user = User.find("id=?", condition.userId).first();
            address = ConsultCondition.findAddressByCondition(user);
        }


//        if (phone != null)
//            condition.searchUser = phone;

        if (StringUtils.isNotBlank(condition.searchOrderCoupon))
            moreSearch = condition.searchOrderCoupon;
        else if (StringUtils.isNotBlank(condition.searchUser))
            moreSearch = condition.searchUser;

//        if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {


//        Address address = models.sales.ConsultCondition.findAddressByCondition(condition);

        List<Order> orderList = ConsultCondition.findOrderByCondition(condition);

        List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);

        List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

        String loginName = User.find("id=?", userId).first();

        long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
        long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
        long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

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
    public static void save(ConsultRecord consult, User user, String phone) {

        CRMCondition condition = new CRMCondition();

        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {
//            if (phone != null)
//                condition.searchUser = phone;


            String currentOperator = OperateRbac.currentUser().loginName;

            List<ConsultRecord> consultContent = ConsultRecord.find("deleted=? order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

            if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {

                user = User.find("id=?", user.id).first();
                Address address = ConsultCondition.findAddressByCondition(user);
                List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
                List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
                List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

                long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
                long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
                long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

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

        consult.userId = user.id;
        consult.phone = phone;
        consult.loginName = user.loginName;

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

        ConsultRecord consult = ConsultRecord.findById(id);


        ConsultRecord.delete(id);

        index(phone, null, null);
    }

    public static void edit(Long id) {

        ConsultRecord consult = ConsultRecord.findById(id);
        render(consult, id);
    }


    public static void update(Long id, @Valid ConsultRecord consult, String phone) {


        ConsultRecord oldConsult = ConsultRecord.findById(id);


        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {

            render("OperateCRM/edit.html", consult, id);
        }
        ConsultRecord.update(id, consult);

        index(phone, null, null);
    }

    public static void getPhone() {
        render();
    }

    public static void jumpIndex(String phone) {
        index(phone, null, null);
    }

    public static void bind(String phone, Long couponId, Long userId) {
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        render(phone, coupon, userId);
    }

    public static void saveBind(String phone, Long couponId, Long userId) {
        CouponCallBind couponBind = new CouponCallBind();
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        couponBind.eCouponSn = coupon.eCouponSn;
        couponBind.phone = phone;
        couponBind.userId = userId;
        couponBind.save();
        render();

    }


}

