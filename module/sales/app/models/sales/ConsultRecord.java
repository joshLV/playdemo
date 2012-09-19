package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.order.ECoupon;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-4
 * Time: 上午10:33
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "consult_record")
public class ConsultRecord extends Model {


    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;

    /**
     * 咨询类型
     */
    @Column(name = "consult_type")
    @Enumerated(EnumType.STRING)
    public ConsultType consultType;

    /**
     * 内容
     */
    @Required
    @MaxSize(65000)
    @Lob
    public String text;


    public String phone;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "user_id")
    public long userId;                     //下单用户ID，可能是一百券用户，也可能是分销商


//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "consultRecord")
//    public List<CouponCallBind> couponCallBindList;
//
    @OneToMany
    @JoinColumn(name ="coupon_call_bind_list")
    public List<CouponCallBind> couponCallBindList;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    public AccountType userType;            //用户类型，个人/分销商

    @Column(name = "email")
    @Required
    @Email
    public String loginName;


//    public static void delete(Long... ids) {
//        for (Long id : ids) {
//            models.sales.ConsultRecord consult = models.sales.ConsultRecord.findById(id);
//            if (consult != null) {
//                consult.deleted = DeletedStatus.DELETED;
//                consult.save();
//            }
//        }
//    }


    public static void delete(Long id) {

        ConsultRecord consult = ConsultRecord.findById(id);
        if (consult != null) {
            consult.deleted = DeletedStatus.DELETED;
            consult.save();
        }

    }


    public static void update(Long id, ConsultRecord consult) {
        ConsultRecord updateConsult = ConsultRecord.findById(id);
        if (updateConsult == null) {
            return;
        }
        updateConsult.updatedAt = new Date();
        updateConsult.consultType = consult.consultType;
        updateConsult.text = consult.text;

        updateConsult.save();
    }

    //=================================================== 数据库操作 ====================================================

    @Override
    public boolean create() {

        createdAt = new Date();

        return super.create();
    }
}
