package models.dangdang;

import models.order.ECoupon;
import play.db.jpa.Model;

import javax.persistence.Table;

/**
 * 当当电子券（消费码）.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 11:56 AM
 */
@Table(name="dangdang_ecoupon")
public class DangDangECoupon extends Model {

    /**
     * 查询当前券是否已在当当上退款了.
     *
     * @param eCoupon
     * @return
     */
    public static boolean isRefund(ECoupon eCoupon){
        //todo
        return false;
    }

    /**
     * 通知当当当前的券已经使用.
     *
     * @param eCoupon
     */
    public static void notifyVerified(ECoupon eCoupon){

    }

    /**
     * 发送券号短信.
     *
     * @param eCoupon
     */
    public static void sendSMS(ECoupon eCoupon){

    }
}
