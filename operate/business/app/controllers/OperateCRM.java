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

import java.util.ArrayList;
import java.util.HashMap;
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


    public static void index(String phone, CRMCondition condition, Long userId, Long consultId, String consultStatus) {
//        System.out.println("Index phone"+phone);
//        if (userId == null)
//            if (condition != null)
//                if (condition.userId != null)
//                    userId = condition.userId;

        if (condition != null)
            if (condition.userId != null)
                userId = condition.userId;
//        System.out.println("userId" + userId);


        int times = 0;
//        System.out.println("consultStatus11111"+consultStatus);
//        System.out.println("consultconsultconsultconsult"+consult);
        User user = null;
        Address address = null;
        String loginName = null;
        ConsultRecord consult = null;

        MemberCallBind bind = new MemberCallBind();

        List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
//        System.out.println("userListuserList" + userList);

//        System.out.println("userList"+userList);
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
//        System.out.println("consultStatus"+consultStatus);
        consultContent = ConsultRecord.find("deleted=? and phone=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();
        if (StringUtils.isNotBlank(consultStatus)) {
            if (consultStatus.equals("finish"))
                consultContent = ConsultRecord.find("deleted=? and phone=? and text!=null order by createdAt desc", DeletedStatus.UN_DELETED, phone).fetch();
            if (consultStatus.equals("tempSave")) {
                consultContent = null;
                consult = ConsultRecord.find("id=?", consultId).first();
            }
        }
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
//            System.out.println("userId" + userId);
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


//        if (phone != null)
//            condition.searchUser = phone;

        if (StringUtils.isNotBlank(condition.searchOrderCoupon))
            moreSearch = condition.searchOrderCoupon;
        else if (StringUtils.isNotBlank(condition.searchUser))
            moreSearch = condition.searchUser;

//        if (StringUtils.isNotBlank(condition.searchUser) || StringUtils.isNotBlank(condition.searchOrderCoupon)) {


//        Address address = models.sales.ConsultCondition.findAddressByCondition(condition);
        if (condition.searchUser == null)
            if (phone != null)
                condition.searchUser = phone;


        System.out.println("condition.phone" + condition.searchUser);
        List<Order> orderList = ConsultCondition.findOrderByCondition(condition);

        List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);

        List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);
//        System.out.println("userId" + userId);
//        System.out.println("userId" + userId);
        if (userId != null) {
            user = User.find("id=?", userId).first();
            loginName = user.loginName;
        }
        long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
        long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
        long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

        //address  user
        times++;
        if (condition.searchUser == null && user != null) {
            userId = user.id;
        }


        List<User> searchUserList = ConsultCondition.findSearchUserByCondition(condition);
        HashMap<Long, Address> addressMap = new HashMap();

        for (User u : searchUserList) {
            addressMap.put(u.id, ConsultCondition.findAddressByCondition(u));
        }

//        System.out.println("userList" + userList);
        List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();

        if (userId != null)
            condition.userId = userId;

        render(couponCallBindList, addressMap, searchUserList, userId, address, user, userList, orderList, condition, eCoupons, consultContent, phone,
                currentOperator, moreSearch, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize, consultId, consult);
    }

    //User user
//    name = "user" value = "19"
//    name = "user.id"
    //Long userId
    public static void tempSave(Long consultId, ConsultRecord consult, User user, String phone, Long userId, String consultStatus) {
//        System.out.println("tempSave phone" + phone);
//        System.out.println("tempSave consultId" + consultId);
//        System.out.println("userId"+userId);
//        System.out.println("consultStatus"+consultStatus);
//        System.out.println("userId"+userId);
//        System.out.println("tempsave userId"+userId);
//        System.out.println("user  id"+user.id);
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
        CRMCondition condition = new CRMCondition();
        if (consult != null)
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
//                render("OpeateCRM/index.html", user, address, consult, consultContent,
//                        currentOperator, phone, orderList,
//                        condition, eCoupons, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize,consultId
//                );

//                redirect("/callcenter/phone/" + phone + "/record/" + consultId);
                user = User.find("id=?", userId).first();
//                List<User> userList = User.find("id=?", userId).fetch();
                List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
                List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
//                System.out.println("user11111" + user);
                render("OperateCRM/index.html", couponCallBindList, consult, consultContent, user, currentOperator, phone, userList, condition, consultId);


            }
            // index(phone,null,user.id.toString(),consultId,null);
            user = User.find("id=?", userId).first();
//            List<User> userList = User.find("id=?", userId).fetch();
            List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
            consultStatus = "tempSave";
            condition.userId = userId;
            List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
            List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
            List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

            long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
            long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
            long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);

//            System.out.println("consultStatus"+consultStatus);
            consultContent = null;
            List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
            render("OperateCRM/index.html", couponCallBindList, userId, orderList, eCoupons, withdrawBill, orderListSize, eCouponsSize, withdrawBillSize, consultStatus, consult, consultContent, currentOperator, phone, condition, consultId, userList, user);
        }
        if (consult != null) {
            consult.deleted = DeletedStatus.UN_DELETED;
            consult.createdBy = OperateRbac.currentUser().loginName;
            consult.userType = models.accounts.AccountType.CONSUMER;
            if (user.id != null)
                consult.userId = user.id;
            consult.phone = phone;
            consult.loginName = user.loginName;

            consult.create();
            consult.save();
        }
        consultStatus = "tempSave";
//        System.out.println("consultconsultconsultconsult"+consult);
//        System.out.println("tempSave userId" + userId);
        index(phone, null, userId, consultId, consultStatus);

    }

    public static void save(Long consultId, ConsultRecord consult, User user, String phone, Long userId, String consultStatus) {
        //        System.out.println("phonephone" + phone);
//        System.out.println("phone"+phone);
//        System.out.println("qqqqqqq");
//        System.out.println("consult.phone" + consult.phone);
//        System.out.println("save userId" + userId);

        user = User.findById(userId);
//        System.out.println("user  id" + user.id);
//        System.out.println("consultId" + consultId);

        String tempText = consult.text;
        ConsultType tempConsultType = consult.consultType;
//        System.out.println("consultIdconsultIdconsultId" + consultId);
        consult = ConsultRecord.findById(consultId);
        consult.text = tempText;

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
                user = User.find("id=?", userId).first();
//                List<User> userList = User.find("id=?", userId).fetch();
                List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
                //address user
//                render("OpeateCRM/index.html", user, address, consult, consultContent,
//                        currentOperator, phone, orderList,
//                        condition, eCoupons, orderListSize, eCouponsSize, withdrawBill, withdrawBillSize,consultId
//                );
//                System.out.println("qqqqqqqqqq");
//                redirect("/callcenter/phone/" + phone + "/record/" + consultId);
                List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
                render("OperateCRM/index.html", couponCallBindList, consult, consultContent, user, currentOperator, phone, userList, condition, consultId);
            }
            // index(phone,null,user.id.toString(),consultId,null);

            user = User.find("id=?", userId).first();
//            List<User> userList = User.find("id=?", userId).fetch();
            List<User> userList = User.find("id in (select c.userId from MemberCallBind c where c.phone=?)", phone).fetch();
            condition.userId = userId;
            List<Order> orderList = ConsultCondition.findOrderByCondition(condition);
            List<ECoupon> eCoupons = ConsultCondition.findCouponByCondition(condition);
            List<WithdrawBill> withdrawBill = ConsultCondition.findBillByCondition(condition);

            long orderListSize = ConsultCondition.findOrderByConditionSize(condition);
            long eCouponsSize = ConsultCondition.findCouponByConditionSize(condition);
            long withdrawBillSize = ConsultCondition.findBillByConditionSize(condition);
//            System.out.println("user11111" + user);
            List<CouponCallBind> couponCallBindList = CouponCallBind.findAll();
            render("OperateCRM/index.html", couponCallBindList, consult, userId, orderList, eCoupons, withdrawBill, orderListSize, eCouponsSize, withdrawBillSize, consult, consultContent, user, currentOperator, phone, userList, condition, consultId);

//            index(phone,null,userId,consultId,consultStatus);
//            index(phone,null,null,consultId,null);
//            redirect("/callcenter/phone/"+phone+"/record/"+consultId);
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
        consultStatus = "finish";


        getPhone();

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
        index(phone, null, null, null, null);
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
        index(phone, null, null, null, null);
    }

    public static void getPhone() {
        render();
    }


    public static void jumpIndex(String phone, CRMCondition condition, Long userId, Long consultId, String consultStatus) {
        ConsultRecord consult = new ConsultRecord();
        consult.deleted = DeletedStatus.UN_DELETED;
        consult.save();
        consultId = consult.id;
        consultStatus = "finish";
        index(phone, condition, userId, consultId, consultStatus);
    }

    public static void bind(String phone, Long couponId, Long userId, Long consultId, ConsultRecord consult) {
//        System.out.println("consultId"+consultId);
        ECoupon coupon = ECoupon.find("id=?", couponId).first();

        render(phone, coupon, userId, consultId, consult);
    }

    public static void saveBind(String phone, Long couponId, Long userId, Long consultId, ConsultRecord consult) {
//        System.out.println("consultId" + consultId);
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
//        System.out.println("consultId" + consultId);
        if (consult != null) {
            if (consult.couponCallBindList == null) {
                consult.couponCallBindList = new ArrayList<>();
            }
//        consult.couponCallBindList.add(couponBind);

            consult.couponCallBindList.add(couponBind);
            consult.save();
        }
//        System.out.println("couponBind" + couponBind);
//        System.out.println("consult" + consult.couponCallBindList);

        render(consult);

    }

//    public static void finish(String phone) {
//        String consultStatus = "finish";
//        index(phone, null, null, null, consultStatus);
//    }

    public static void abandon(Long consultId, String phone) {
        ConsultRecord consult = ConsultRecord.findById(consultId);
        if (consult != null) {
            consult.deleted = DeletedStatus.DELETED;
            consult.save();

        }
        //String consultStatus="tempSave";
        getPhone();
    }

    public static void bindCouponDetails(String phone, Long couponId, Long userId, Long consultId) {
        ECoupon coupon = ECoupon.find("id=?", couponId).first();
        render(phone, coupon, userId, consultId);
    }

    public static void callCenter(String phone) {
        jumpIndex(phone, null, null, null, null);

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

