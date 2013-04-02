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
import play.Logger;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-8-31
 * Time: 上午9:38
 */

@With(OperateRbac.class)
@ActiveNavigation("crm_app_consumers")
public class OperateCRM extends Controller {

    public static void index(String phone, CRMCondition condition, Long userId, Long consultId, ConsultRecord consult) {
        if (condition != null && condition.userId != null) {
            userId = condition.userId;
        }
        int times = 0;
        User user = null;
        Address address = null;
        String loginName = null;
        MemberCallBind bind = new MemberCallBind();
        List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
        if (userList == null || userList.size() <= 0) {
            userList = User.find("mobile=?", phone).fetch();
            for (int i = 0; i < userList.size(); i++) {
                bind.userId = userList.get(i).id;
                bind.phone = phone;
                bind.loginName = userList.get(i).loginName;
                bind.save();
            }
        } else {
        }
        String currentOperator = OperateRbac.currentUser().loginName;
        if (condition == null) {
            condition = new CRMCondition();
            if (userList.size() > 0) {
                user = userList.get(0);
                address = ConsultCondition.findAddressByCondition(user);
                condition.userId = userList.get(0).getId();
            }
        } else {
            if (userId != null)
                user = User.find("id=?", userId).first();
            if (user == null) {
                if (condition.userId != null)
                    user = User.find("id=?", condition.userId).first();
                else if (userList.size() > 0)
                    user = userList.get(0);
            }
            address = ConsultCondition.findAddressByCondition(user);
        }
        String moreSearch = null;
        if (StringUtils.isBlank(condition.searchOrderCoupon) && StringUtils.isBlank(condition.searchUser))
            moreSearch = phone;
        if (StringUtils.isNotBlank(condition.searchOrderCoupon))
            moreSearch = condition.searchOrderCoupon;
        if (StringUtils.isNotBlank(condition.searchUser))
            moreSearch = condition.searchUser;
        List<User> searchUserList = null;
        HashMap<Long, Address> addressMap = null;
        if (condition.searchUser != null || condition.searchOrderCoupon != null) {
            searchUserList = ConsultCondition.findSearchUserByCondition(condition);
            addressMap = new HashMap<>();
            for (User u : searchUserList) {
                addressMap.put(u.id, ConsultCondition.findAddressByCondition(u));
            }
        }
        if (condition.searchUser == null)
            if (phone != null) {
                condition.searchUser = phone;
            }
        List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
        List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
        List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);
        if (userId != null) {
            user = User.find("id=?", userId).first();
            loginName = user.loginName;
        }
        long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
        long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
        long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);
        times++;
        if (condition.searchUser == null && user != null) {
            userId = user.id;
        }
        List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
        if (userId != null)
            condition.userId = userId;
        List<ConsultRecord> consultContent = null;
        //org.apache.commons.lang.StringUtils.isNotBlank(text)=1
//        String textEmpty=" ";
        String hqlConsult = "deleted=? and text!=null and ((phone=? and userId is null) ";
        if (user != null) {
            hqlConsult += " or (userId=" + user.id + "))";
        }
        if (user == null)
            hqlConsult += ")";
        if (consultId != null) {
            hqlConsult += " and id != " + consultId;
        }


        consultContent = ConsultRecord.find(hqlConsult + "order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();
        render(couponCallBindList, addressMap, searchUserList, userId, address, user, userList, orderList, condition, eCoupons, consultContent, phone,
                currentOperator, moreSearch, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize, consultId, consult);
    }

    public static void tempSave(CRMCondition condition, Long consultId, ConsultRecord consult, User user, String phone, Long userId) {
//        flash.clear();
//        validation.clear();
        validation.required(consult.text);
        String tempPhone = consult.phone;
        String tempText = consult.text;
        ConsultType tempConsultType = consult.consultType;
        if (consultId != null)
            consult = ConsultRecord.findById(consultId);
        if (consult != null) {
            consult.text = tempText;
            consult.phone = tempPhone;
            consult.consultType = tempConsultType;
        }
//        if (consult != null)
//            if (StringUtils.isBlank(consult.text))
//                Validation.addError("consult.text", "validation.required");
        if (Validation.hasErrors()) {
            params.flash();
            validation.keep();
            index(phone, null, userId, consultId, consult);
        }
        if (consult != null) {
            consult.deleted = DeletedStatus.UN_DELETED;
            consult.createdBy = OperateRbac.currentUser().loginName;
            consult.userType = models.accounts.AccountType.CONSUMER;
            if (user != null)
                if (user.id != null)
                    consult.userId = user.id;
            consult.phone = phone;
            if (user != null)
                consult.loginName = user.loginName;
            consult.create();
            consult.save();
        }
        index(phone, condition, userId, consultId, consult);
    }

    public static void save(CRMCondition condition, Long consultId, ConsultRecord consult, User user, String phone, Long userId) {
        validation.required(consult.text);
        if (userId != null)
            user = User.findById(userId);
        String tempText = consult.text;
        ConsultType tempConsultType = consult.consultType;
        consult = ConsultRecord.findById(consultId);
        consult.text = tempText;
        consult.consultType = tempConsultType;
//        if (StringUtils.isBlank(consult.text))
//            Validation.addError("consult.text", "validation.required");
        if (Validation.hasErrors()) {
            params.flash();
            validation.keep();
            index(phone, null, userId, consultId, consult);
        }
        consult.deleted = DeletedStatus.UN_DELETED;
        consult.createdBy = OperateRbac.currentUser().loginName;
        consult.userType = models.accounts.AccountType.CONSUMER;
        if (user != null)
            if (user.id != null)
                consult.userId = user.id;
        consult.phone = phone;
        if (user != null)
            consult.loginName = user.loginName;
        consult.create();
        consult.save();
        getPhone();
    }

    public static void getPhone() {
        render();
    }

    public static void jumpIndex(String phone, CRMCondition condition, Long userId, Long consultId, String callNo, String calledNo, String agentName, String ivrkey, String callsheetId, String province, String city) {
        ConsultRecord consult = new ConsultRecord();
        consult.deleted = DeletedStatus.UN_DELETED;
        consult.callNo = callNo;
        consult.calledNo = calledNo;
        consult.agentName = agentName;
        consult.ivrkey = ivrkey;
        consult.callsheetId = callsheetId;
        consult.province = province;
        consult.city = city;
        consult.save();
        consultId = consult.id;
        if (StringUtils.isNotBlank(callNo)) {
            phone = callNo;
        }
        index(phone, condition, userId, consultId, null);
    }

    public static void jumpPrevIndex(String phone, CRMCondition condition, Long userId, Long consultId) {
        index(phone, condition, userId, consultId, null);
    }

    public static void bind(List<CouponCallBind> couponCallBindList, CRMCondition condition, String phone, Long couponId, Long userId, Long consultId, ConsultRecord consult) {
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        render(couponCallBindList, condition, phone, coupon, userId, consultId, consult);
    }

    public static void saveBind(List<CouponCallBind> couponCallBindList, String phone, Long couponId, Long userId, Long consultId, ConsultRecord consult) {
        CouponCallBind couponBind = new CouponCallBind();
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        couponBind.eCouponSn = coupon.eCouponSn;
        couponBind.phone = phone;
        couponBind.userId = userId;
        couponBind.couponId = coupon.id;
        couponBind.consultId = consultId;
        couponBind.save();
        couponBind = CouponCallBind.find("couponId=?", couponId).first();
        consult = ConsultRecord.find("id=?", consultId).first();
        if (consult != null) {
            if (consult.couponCallBindList == null || consult.couponCallBindList.size() == 0) {
                consult.couponCallBindList = new ArrayList<>();
            }
            if (couponBind != null)
                consult.couponCallBindList.add(couponBind);
            consult.save();
        }
        render(consult, couponCallBindList);
    }

    public static void abandon(Long consultId, String phone) {
        ConsultRecord consult = ConsultRecord.findById(consultId);
        if (consult != null) {
            consult.deleted = DeletedStatus.DELETED;
            consult.save();
        }
        getPhone();
    }

    public static void bindCouponDetails(String phone, Long couponId, Long userId, Long consultId) {
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        render(phone, coupon, userId, consultId);
    }

    public static void callCenter(String phone) {
        for (String key : params.all().keySet()) {
            Logger.info("callCenter key(" + key + "):" + params.get(key));
        }

        if (phone == null) {
            phone = params.get("callNo");
        }

        String callNo = params.get("callNo");
        String calledNo = params.get("calledNo");
        String agentName = params.get("agentName");
        String ivrkey = params.get("ivrkey");
        String callsheetId = params.get("callsheetId");
        String province = params.get("province");
        String city = params.get("city");
        jumpIndex(phone, null, null, null, callNo, calledNo, agentName, ivrkey, callsheetId, province, city);
    }

    public static void bindSearchUser(String phone, Long userId) {
        MemberCallBind bind = new MemberCallBind();
        User user = User.findById(userId);
        bind.phone = phone;
        bind.userId = userId;
        bind.loginName = user.loginName;
        bind.save();
        render();
    }
}

