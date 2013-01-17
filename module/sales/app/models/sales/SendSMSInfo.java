package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.Date;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-9-6
 * Time: 上午11:44
 */

@Entity
@Table(name = "send_sms_info")
public class SendSMSInfo extends Model {

    /**
     * 任务名称
     */
    @Column(name = "task_no")
    public String taskNo;

    /**
     * 手机
     */
    @Mobile
    public String mobile;

    /**
     * 券号
     */
    @Column(name = "e_coupon_sn")
    public String eCouponSn;

    /**
     * 短信内容
     */
    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    public String text;


    /**
     * 发送时间
     */
    @Column(name = "send_at")
    public Date sendAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    @Version
    public int lockVersion;

//    @Override
//    public boolean create() {
//
//        createdAt = new Date();
//        return super.create();
//    }


    public static JPAExtPaginator<SendSMSInfo> findByCondition(SendSMSInfoCondition condition,
                                                               int pageNumber, int pageSize) {

        JPAExtPaginator<SendSMSInfo> smsList = new JPAExtPaginator<>
                ("SendSMSInfo s", "s", SendSMSInfo.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        smsList.setPageNumber(pageNumber);
        smsList.setPageSize(pageSize);
        smsList.setBoundaryControlsEnabled(false);
        return smsList;
    }

    public static List<SendSMSInfo> findUnDeleted(String taskNo) {
        return find("sendAt=null and deleted=com.uhuila.common.constants.DeletedStatus.UN_DELETED and taskNo=? ", taskNo).fetch();
    }
}
