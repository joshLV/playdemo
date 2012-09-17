package models.sales;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-17
 * Time: 下午2:48
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "coupon_call_bind")
public class CouponCallBind  extends Model {

    @Required
    @Column(name = "e_coupon_sn")
    public String eCouponSn;

    public String phone;

    @Column(name = "user_id")
    public long userId;                     //下单用户ID，可能是一百券用户，也可能是分销商
}
