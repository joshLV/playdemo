package models.sales;

import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-6
 * Time: 上午11:44
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "send_sms")
public class SendSMS extends Model {

    /**
     * 任务号
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
    private String text;


    /**
     * 发送时间
     */
    @Column(name = "send_at")
    public Date sendAt;


}
