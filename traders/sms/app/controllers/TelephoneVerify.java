package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.apache.commons.codec.digest.DigestUtils;
import play.Play;
import play.mvc.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author likang
 */

/**
 */
public class TelephoneVerify extends Controller{
    private static final String APP_KEY = Play.configuration.getProperty("tel_verify.app_key", "exos8BHw");

    /**
     * 电话验证
     *
     * @param caller    主叫号码
     * @param employee  员工编号
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的毫秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void verify(String caller, String employee, String coupon, Long timestamp, String sign){
        if(caller == null || caller.trim().equals("")){
            renderText("主叫号码无效");
        }
        if(employee == null || employee.trim().equals("")){
            renderText("员工编号无效");
        }
        if(coupon == null || coupon.trim().equals("")){
            renderText("券号无效");
        }
        if(timestamp == null){
            renderText("时间戳无效");
        }
        if(sign == null || sign.trim().equals("")){
            renderText("签名无效");
        }

        long t = System.currentTimeMillis();
        //5分钟的浮动
        if(Math.abs(timestamp - t) > 300000){
            renderText("请求超时");
        }
        //验证密码
        if(!DigestUtils.md5Hex(APP_KEY + timestamp).equals(sign)){
            renderText("签名错误");
        }

        //开始验证
        ECoupon ecoupon = ECoupon.query(coupon, null);

        if (ecoupon == null) {
            renderText("对不起，未找到此券");
        }

        Long supplierId = ecoupon.goods.supplierId;

        Supplier supplier = Supplier.findById(supplierId);

        if (supplier == null || supplier.deleted == DeletedStatus.DELETED || supplier.status == SupplierStatus.FREEZE) {
            renderText("对不起，商户不存在");
        }

        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where supplier.id=? and jobNumber=?", supplierId, employee).first();
        if(supplierUser == null){
            renderText("未找到该店员");
        }

        Long shopId = supplierUser.shop.id;
        Shop shop = Shop.findById(shopId);
        String shopName = shop.name;

        if (ecoupon.expireAt.before(new Date())) {
            renderText("对不起，该券已过期");
        } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            ecoupon.consumeAndPayCommission(supplierUser.shop.id, supplierUser, VerifyCouponType.CLERK_MESSAGE);
            String eEoupon = ecoupon.getMaskedEcouponSn();
            eEoupon = eEoupon.substring(eEoupon.lastIndexOf("*") + 1);

            String dateTime = DateUtil.getNowTime();

            // 发给消费者
            SMSUtil.send("【券市场】您尾号" + eEoupon + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", ecoupon.orderItems.phone, ecoupon.replyCode);

            renderText("消费成功，价值" + ecoupon.faceValue + "元");
        } else if (ecoupon.status == ECouponStatus.CONSUMED) {
            renderText("该券无法重复消费。消费时间为" + new SimpleDateFormat("yyyy年MM月dd日hh点mm分").format(ecoupon.consumedAt));
        }
    }
}
