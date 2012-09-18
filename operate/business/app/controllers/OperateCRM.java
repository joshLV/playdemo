package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.WithdrawBill;
import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.sales.*;
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


    public static void index(String phone, CRMCondition condition, String userId, Long consultId, String consultStatus,ConsultRecord consult) {
        int times = 0;
        User user = null;
        Address address = null;
        String loginName = null;

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
        List<ConsultRecord> consultContent = null;
        if (StringUtils.isNotBlank(consultStatus))
            if (consultStatus.equals("finish"))
                consultContent = ConsultRecord.find("deleted=? and phone=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();

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
            if (StringUtils.isNotBlank(userId))
                user = User.find("id=?", Long.parseLong(userId)).first();
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

        if (StringUtils.isNotBlank(userId)) {
            user = User.find("id=?", Long.parseLong(userId)).first();
            loginName = user.loginName;
        }
        long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
        long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
        long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

        //address  user
        times++;
        render(address, user, userList, orderList, condition, eCoupons, consultContent, phone, currentOperator, moreSearch, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize, consultId);
//        }
//
//
//        render(userList, consultContent, phone, currentOperator, moreSearch);

    }

    //User user
//    name = "user" value = "19"
//    name = "user.id"
    //Long userId
    public static void tempSave(Long consultId, ConsultRecord consult, User user, String phone) {
        System.out.println("consultId" + consultId);
        String tempPhone = consult.phone;
        String tempText = consult.text;
        ConsultType tempConsultType = consult.consultType;
        consult = ConsultRecord.findById(consultId);
        consult.text = tempText;
        consult.phone = tempPhone;
        consult.consultType = tempConsultType;
        CRMCondition condition = new CRMCondition();

        if (StringUtils.isBlank(consult.text))
            Validation.addError("consult.text", "validation.required");

        if (Validation.hasErrors()) {
//            if (phone != null)
//                condition.searchUser = phone;

            String currentOperator = OperateRbac.currentUser().loginName;

            List<ConsultRecord> consultContent = ConsultRecord.find("deleted=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED).fetch();

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
                render("OpeateCRM/index.html", user, address, consult, consultContent,
                        currentOperator, phone, orderList,
                        condition, eCoupons, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize,consultId
                );

            }
            // index(phone,null,user.id.toString(),consultId,null);
            render("OperateCRM/index.html", consult, consultContent, currentOperator, phone, condition,consultId);
        }

        consult.deleted = DeletedStatus.UN_DELETED;
        consult.createdBy = OperateRbac.currentUser().loginName;
        consult.userType = models.accounts.AccountType.CONSUMER;
        if (user.id != null)
            consult.userId = user.id;
        consult.phone = phone;
        consult.loginName = user.loginName;

        consult.create();
        consult.save();
        String consultStatus = "tempSave";
        index(phone, null, null, consultId, consultStatus,consult);

    }


    /**
     * 删除指定咨询
     *
     * @param id 商品ID
     */
    public static void delete(Long id, String phone) {

        ConsultRecord consult = ConsultRecord.findById(id);


        ConsultRecord.delete(id);
        //String consultStatus="tempSave";  consult
        index(phone, null, null, null, null,null);
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
        //String consultStatus="tempSave";     consult
        index(phone, null, null, null, null,null);
    }

    public static void getPhone() {
        render();
    }

    public static void jumpIndex(String phone) {
        ConsultRecord consult = new ConsultRecord();
        consult.deleted = DeletedStatus.UN_DELETED;
        consult.save();
        Long consultId = consult.id;
        String consultStatus = "finish";
        index(phone, null, null, consultId, consultStatus,null);
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
        couponBind.couponId = coupon.id;
        couponBind.save();
        render();

    }

    public static void finish(String phone) {
        String consultStatus = "finish";
        index(phone, null, null, null, consultStatus,null);
    }

    public static void abandon(Long consultId, String phone) {
        ConsultRecord consult = ConsultRecord.findById(consultId);
        if (consult != null) {
            consult.deleted = DeletedStatus.DELETED;
            consult.save();

        }
        //String consultStatus="tempSave";
        getPhone();
    }


}

