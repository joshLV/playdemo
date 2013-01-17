package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.OrdersCondition;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-9-4
 * Time: 上午10:33
 */
@Entity
@Table(name = "consult_record")
public class ConsultRecord extends Model {

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    @Transient
    public String createdAtInfo;


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

    @Transient
    public String consultTypeInfo;

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
    public Long userId;                     //下单用户ID，可能是一百券用户，也可能是分销商

    @Column(name = "call_no")
    public String callNo;

    @Column(name = "called_no")
    public String calledNo;

    @Column(name = "agent_name")
    public String agentName;

    public String ivrkey;

    @Column(name = "call_sheet_id")
    public String callsheetId;

    public String province;

    public String city;

    @Column(name = "created_by")
    public String createdBy;

//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "consultRecord")
//    public List<CouponCallBind> couponCallBindList;
//

    @OneToMany
    @JoinColumn(name = "coupon_call_bind_id")
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


    public static JPAExtPaginator<ConsultRecord> query(ConsultResultCondition condition, Long supplierId, int pageNumber, int pageSize) {
        JPAExtPaginator<ConsultRecord> orderPage = new JPAExtPaginator<>
                ("ConsultRecord c", "c", ConsultRecord.class, condition.getFilter(),
                        condition.paramsMap)
                .orderBy(condition.getOrderByExpress());
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);
        orderPage.setBoundaryControlsEnabled(true);
        return orderPage;
    }

    //=================================================== 数据库操作 ====================================================

    @Override
    public boolean create() {

        createdAt = new Date();

        return super.create();
    }
}
